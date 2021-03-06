package com.example.songs_album;

import com.example.songs_album.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Float.min;

@Controller
public class SongController implements ShuffleEngine{

    @Autowired
    SongRepository repository;

    private String storePath="/Users/nakamura/PracticeApp/songs_album/target/classes/static/";

    //サーバーに保存されている曲全体
    private ArrayList<Song>storedSongs=new ArrayList<>();

    //５つの曲のうち、何番目を選択しているか
    private int selectedSongIndex=0;

    //現在選択されている曲
    private Song selectedSong;

    //現在表示される５つの曲の配列
    private Song[] presentSongsArray=new Song[5];
    //次巡で表示される５つの曲の配列
    private Song[] nextSongsArray=new Song[5];

    private Song presentLastSong;

    private ArrayList<Song>presentSongsList=new ArrayList<>(Arrays.asList(presentSongsArray));

    /**
     * target/classes/static 下のファイルを元にダミーデータを作成し、リポジトリに保存する
     * また、storedSongsのインスタンスを生成し、以降このインスタンスを元にpresentSongsArray, nextSongsArrayの取得、アップロード等の処理を行う。
     */
    @PostConstruct
    public void init(){

        ArrayList<String> storedSongNames=new ArrayList<>();

        File store_dir=new File(storePath);
        File[]storedFiles=store_dir.listFiles();

        int storedSongsNum=storedFiles.length;

        File file[]=new File[storedSongsNum];
        Song song[]=new Song[storedSongsNum];

        //指定ファイルに格納されている音楽ファイルを一つずつ、名前とパスをDBに格納するために繰り返しを行う
        for(int i=0;i<storedFiles.length;i++){
            storedSongNames.add(new File(storePath+storedFiles[i]).getName());

            file[i]=new File(storePath+storedSongNames.get(i));
            song[i]=new Song();
            song[i].setFile_name(file[i].getName());
            song[i].setFile_path(file[i].getName());
            repository.saveAndFlush(song[i]);
        }

        storedSongs=new ArrayList<>(repository.findAll());
        setSongs(peekQueue());

        selectedSong=presentSongsArray[selectedSongIndex];
    }


    /**
     * 現在選択中の曲と現在の曲配列をthymeleafに埋め込んで表示する
     * @param mav 他のメソッドで更新した値と、thymeleafを橋渡しする
     * @return "/redirect:/"
     */
    @RequestMapping(path = "/",method = RequestMethod.GET)
    ModelAndView index(ModelAndView mav){
        mav.setViewName("index");

        mav.addObject("presentSong",selectedSong);
        mav.addObject("songs",presentSongsArray);

        return mav;
    }

    /**
     * setSongs()を呼び出してpresentSongsArray, nextSongsArrayを更新する。
     * また、「選択中の曲」を曲配列の０番目に指定する。
     * @return "redirect:/"
     */
    @RequestMapping(path = "/shuffle",method = RequestMethod.GET)
    String shuffleSongs(){

        selectedSongIndex=0;

        setSongs(nextSongsArray);
        selectedSong=presentSongsArray[selectedSongIndex];
        return "redirect:/";
    }


    /**
     * 現在選択されている曲を指定する。
     * @return "redirect:/"
     */
    @RequestMapping(path = "/play",method = RequestMethod.GET)
    String playSong(){
        selectedSong=presentSongsArray[selectedSongIndex];
        return "redirect:/";
    }

    /**
     * 現在選択中の曲を、次の曲に更新する。
     * @return "redirect:/"
     */
    @RequestMapping(path = "/play/next",method = RequestMethod.GET)
    String playNextSong(){
        selectedSong=getNextSong();

        return "redirect:/";
    }


    /**
     * 今巡の曲配列、次巡の曲配列を更新する。
     * @param songs presentSongsArrayに設定する配列
     */
    public void setSongs(Song[] songs){
        presentSongsArray=songs;
        nextSongsArray=peekQueue();
    }


    /**
     * 次に再生する曲を返す。現在選択中の曲が「最後の次」までいったら、次に選択する曲は「最初の曲」とする。
     * @return 次に再生する曲
     */
    public Song getNextSong(){

        selectedSongIndex++;
        //「次を再生」が最後までいったら、一巡するようにする
        selectedSongIndex= (int) (selectedSongIndex%min(storedSongs.size(),presentSongsArray.length));

        return presentSongsArray[selectedSongIndex];
    }


    /**
     * @return 先読みして得られる、シャッフルされた曲の配列
     */
    public Song[]peekQueue(){

        int num = (int) min(storedSongs.size(), PEEKMAX);
        Song[] queueArray = new Song[num];

        queueArray=getPrioritizeNotPresentSongs(queueArray,num);
        queueArray=getShuffledArray(queueArray);

        return queueArray;
    }


    /**
     * 今巡の配列に含まれない曲が優先的に取得する
     * @return 今巡の曲をなるべく含まない曲配列
     */
    private Song[] getPrioritizeNotPresentSongs(Song[]queueArray,int num){

        storedSongs.removeAll(Arrays.asList(presentSongsArray));

        presentSongsList.clear();
        //presentSongsArrayに影響を与えずにシャッフル処理をするために、「今巡の曲配列」の要素を「リスト」の中に移す
        for(int i=0;i<presentSongsArray.length;i++){
            presentSongsList.add(presentSongsArray[i]);
        }

        Collections.shuffle(presentSongsList);
        storedSongs.addAll(presentSongsList);
        storedSongs.removeAll(Collections.singleton(null));

        //最初の５つの曲をqueueArrayに含めることで、「可能な限り今巡とは違う曲」を得る
        for (int i = 0; i < num; i++) {
            queueArray[i] = storedSongs.get(i);
        }

        return queueArray;
    }


    /**
     * presentSongsArrayと同一でない配列を得る。
     * また、可能な限り「現在の最後の曲」と「得られる配列の最初の曲」が一致しない配列を得る。
     * @param queueArray シャッフル処理をする前段階の配列
     * @return 引数の配列を、工夫してシャッフルした配列
     */
    private Song[]getShuffledArray(Song[] queueArray){

        if(storedSongs.size()>=3){
            //最初の曲が、「今巡の最後」と異なる配列を得る、かつ「今巡の配列」と異なる配列を得るまで繰り返す
            do{
                presentLastSong=presentSongsArray[presentSongsArray.length-1];
                Collections.shuffle(Arrays.asList(queueArray));
            }while (Arrays.equals(presentSongsArray,queueArray)||presentLastSong==queueArray[0]);
        }
        // 曲数が２個の時には、「今巡と異なる配列を得る」という条件下では「今巡の最後 != 次巡の最初」となり得ない
        // ので、処理を分ける必要がある。
        else if(storedSongs.size()>=2){
            //「今巡の配列」と異なる配列を得るまで繰り返す
            do{
                Collections.shuffle(Arrays.asList(queueArray));
            }while (Arrays.equals(presentSongsArray,queueArray));
        }
        return queueArray;
    }


    /**
     * ファイルをアップロードし、指定パスに格納する。
     * この際、パスと名前をリポジトリ、storedSongsに追加する。
     * @param mydata post送信で受け取ったファイルデータ
     * @param result 入力値を検証した結果
     * @param uploadForm アップ時に必要な、MultipartFileインターフェイスを受け取る
     * @return new ModelAndView("redirect:/")
     */
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @Transactional(readOnly = false)
    ModelAndView upload(@ModelAttribute @Valid Song mydata, BindingResult result, UploadForm uploadForm) {

        if (uploadForm.getFile().isEmpty()) {
            return new ModelAndView("redirect:/");
        }

        Path path = Paths.get(storePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (NoSuchFileException ex) {
                System.err.println(ex);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
        String filename=uploadForm.getFile().getOriginalFilename();
        Path uploadfile = Paths
                .get(storePath+filename);


        //ディレクトリにファイルを書き込む
        try (OutputStream os = Files.newOutputStream(uploadfile, StandardOpenOption.CREATE)) {
            byte[] bytes = uploadForm.getFile().getBytes();

            os.write(bytes);
            os.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }


        mydata.setFile_path("/"+filename);
        mydata.setFile_name(filename);

        repository.saveAndFlush(mydata);
        storedSongs.add(mydata);

        //アップロード前に保存されたnextSongsListは、現在のアップロードの影響を受けないため、
        //nextSongsListを更新する必要がある。
        nextSongsArray=peekQueue();


        return new ModelAndView("redirect:/");
    }
}