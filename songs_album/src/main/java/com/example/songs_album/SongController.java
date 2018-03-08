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
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
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

    //現在表示される５つの曲の配列
    Song[] presentSongsList=new Song[5];
    //次巡で表示される５つの曲の配列
    Song[] nextSongsList=new Song[5];

    //選択されている曲の名前
    String _selectedSongName;

    int storedSongsNum;


    @PostConstruct
    //ダミーデータ(10個)の作成
    public void init(){

        ArrayList<String> storedSongNames=new ArrayList<>();

        File store_dir=new File(storePath);
        File[]storedFiles=store_dir.listFiles();
        storedSongsNum=storedFiles.length;

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

        setSongs(peekQueue());
    }



    @RequestMapping(path = "/",method = RequestMethod.GET)
    ModelAndView index(ModelAndView mav){
        mav.setViewName("index");

        _selectedSongName=presentSongsList[selectedSongIndex].getFile_name();
        mav.addObject("selectedSong",_selectedSongName);
        mav.addObject("songs",presentSongsList);

        return mav;
    }


    @RequestMapping(path = "/shuffle",method = RequestMethod.GET)
    String shuffleSongs(RedirectAttributes attributes){

        selectedSongIndex=0;
        setSongs(nextSongsList);
        attributes.addFlashAttribute("songs",presentSongsList);
        return "redirect:/";
    }


    //曲を指定するメソッド。再生中の曲を表示する処理は、index()メソッドに記述。
    @RequestMapping(path = "/play",method = RequestMethod.GET)
    String playSong(RedirectAttributes attributes){
        selectedSongIndex=selectedSongIndex;

        return "redirect:/";
    }

    @RequestMapping(path = "/play/next",method = RequestMethod.GET)
    String playNextSong(RedirectAttributes attributes){
        getNextSong();
        selectedSongIndex= (int) (selectedSongIndex%min(storedSongs.size(),PEEKMAX));

        return "redirect:/";
    }


    //ランダム処理で選ばれる５個の曲を設定する
    public void setSongs(Song[] songs){
        presentSongsList=songs;

        //現在の曲リストと次巡の曲リストが異なるように設定
        //曲が１つしかない場合は、次巡の曲に単にpeekQueue()を設定するだけ
        if(storedSongs.size()!=1) {
            do {
                nextSongsList = peekQueue();
            } while (presentSongsList == nextSongsList);
        }
        else {
            nextSongsList=peekQueue();
        }
    }


    //次に再生する曲(Song)を返す。次に返す曲が更新される。再生ボタン押されたとき(playSong()の中で？)呼ばれる。
    public Song getNextSong(){
        Song nextSong=presentSongsList[selectedSongIndex];
        selectedSongIndex++;

        return nextSong;
    }

    //次に再生する予定の曲を先読み(PEEKMAXを上限)して配列として返す。
    public Song[]peekQueue(){

        storedSongs.clear();
        Collections.addAll(this.storedSongs,repository.findAll().toArray(new Song[repository.findAll().size()]));

        Collections.shuffle(storedSongs);

        //TODO
        int num= (int) min(storedSongsNum,PEEKMAX);
        Song[]queueArray=new Song[num];
        for(int i=0;i<min(storedSongs.size(),PEEKMAX);i++){
            queueArray[i]=storedSongs.get(i);
        }
        return queueArray;
    }




    //ファイルをアップロードするメソッド
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @Transactional(readOnly = false)
    ModelAndView upload(@ModelAttribute @Valid Song mydata, BindingResult result, UploadForm uploadForm) {

        if (uploadForm.getFile().isEmpty()) {
            return new ModelAndView("/");
        }

        Path path = Paths.get(storePath);  //TODO
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
                .get(storePath+filename);  //TODO


        try (OutputStream os = Files.newOutputStream(uploadfile, StandardOpenOption.CREATE)) {
            byte[] bytes = uploadForm.getFile().getBytes();

            os.write(bytes);
            os.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }


        mydata.setFile_path("/"+filename);
        mydata.setFile_name(filename);

        //保存される曲の数が１増える
        storedSongsNum++;

        repository.saveAndFlush(mydata);

        //アップロード前に保存されたnextSongsListは、現在のアップロードの影響を受けないため、
        //nextSongsListを更新する必要がある。
        nextSongsList=peekQueue();

        return new ModelAndView("redirect:/");
    }
}