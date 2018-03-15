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

    String storePath="/Users/meisei/Documents/abc_app/songs_album/target/classes/static/";

    //サーバーに保存されている曲全体
    ArrayList<Song>storedSongs=new ArrayList<>();

    //５つの曲のうち、何番目を選択しているか
    int selectedSongIndex=0;

    //現在選択されている曲
    Song selectedSong;

    //現在表示される５つの曲の配列
    Song[] presentSongsArray=new Song[5];
    //次巡で表示される５つの曲の配列
    Song[] nextSongsArray=new Song[5];

    Song presentLastSong;

    ArrayList<Song>presentSongsList=new ArrayList<>(Arrays.asList(presentSongsArray));

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
     * @param mav
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
     * @param attributes
     * @return "redirect:/"
     */
    @RequestMapping(path = "/shuffle",method = RequestMethod.GET)
    String shuffleSongs(RedirectAttributes attributes){

        selectedSongIndex=0;

        setSongs(nextSongsArray);
        selectedSong=presentSongsArray[selectedSongIndex];
        return "redirect:/";
    }


    /**
     * 現在選択されている曲を指定する。
     * @param attributes
     * @return "redirect:/"
     */
    @RequestMapping(path = "/play",method = RequestMethod.GET)
    String playSong(RedirectAttributes attributes){
        selectedSong=presentSongsArray[selectedSongIndex];
        return "redirect:/";
    }

    /**
     * 現在選択中の曲を、次の曲に更新する。
     * @param attributes
     * @return "redirect:/"
     */
    @RequestMapping(path = "/play/next",method = RequestMethod.GET)
    String playNextSong(RedirectAttributes attributes){
        selectedSong=getNextSong();

        return "redirect:/";
    }


    /**
     * presentSongsArray, nextSongsArrayを更新する。
     * @param songs
     */
    public void setSongs(Song[] songs){
        presentSongsArray=songs;
        nextSongsArray=peekQueue();
    }


    /**
     * 次に再生する曲を返す。現在選択中の曲が「最後の次」までいったら、次に選択する曲は「最初の曲」とする。
     * @return
     */
    public Song getNextSong(){

        selectedSongIndex++;
        selectedSongIndex= (int) (selectedSongIndex%min(storedSongs.size(),presentSongsArray.length));

        return presentSongsArray[selectedSongIndex];
    }


    /**
     *
     * @return
     */
    public Song[]peekQueue(){

        prioritizeNotPresentSongs();

        int num = (int) min(storedSongs.size(), PEEKMAX);
        Song[] queueArray = new Song[num];
        for (int i = 0; i < num; i++) {
            queueArray[i] = storedSongs.get(i);
        }

        getShuffledArray(queueArray);

        return queueArray;
    }


    /**
     * presentSongsArrayに含まれない曲が優先的に、storeSongsリストの先頭にくる様にする
     */
    void prioritizeNotPresentSongs(){

        storedSongs.removeAll(Arrays.asList(presentSongsArray));

        presentSongsList.clear();
        for(int i=0;i<presentSongsArray.length;i++){
            presentSongsList.add(presentSongsArray[i]);
        }

        Collections.shuffle(presentSongsList);
        storedSongs.addAll(presentSongsList);
        storedSongs.removeAll(Collections.singleton(null));
    }


    /**
     * presentSongsArrayと同一でない配列を得る。
     * また、可能な限り「現在の最後の曲」と「得られる配列の最初の曲」が一致しない配列を得る。
     * @param queueArray
     * @return
     */
    Song[]getShuffledArray(Song[] queueArray){

        if(storedSongs.size()>=3){
            do{
                presentLastSong=presentSongsArray[presentSongsArray.length-1];
                Collections.shuffle(Arrays.asList(queueArray));
            }while (Arrays.equals(presentSongsArray,queueArray)||presentLastSong==queueArray[0]);
        }
        else if(storedSongs.size()>=2){
            do{
                Collections.shuffle(Arrays.asList(queueArray));
            }while (Arrays.equals(presentSongsArray,queueArray));
        }
        return queueArray;
    }


    /**
     * ファイルをアップロードし、指定パスに格納する。
     * この際、パスと名前をリポジトリ、storedSongsに追加する。
     * @param mydata
     * @param result
     * @param uploadForm
     * @return
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