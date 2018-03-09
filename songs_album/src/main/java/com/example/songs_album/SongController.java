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
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Float.intBitsToFloat;
import static java.lang.Float.min;

@Controller
public class SongController implements ShuffleEngine{

    @Autowired
    SongRepository repository;

    String storePath="/Users/nakamura/PracticeApp_nakamura/songs_album/target/classes/static/";

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






    @PostConstruct
    //ダミーデータ(10個)の作成
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

        //シャッフルメソッドでも以下の処理をする
        selectedSong=presentSongsArray[selectedSongIndex];
    }



    @RequestMapping(path = "/",method = RequestMethod.GET)
    ModelAndView index(ModelAndView mav){
        mav.setViewName("index");

        mav.addObject("presentSong",selectedSong);
        mav.addObject("songs",presentSongsArray);

        return mav;
    }


    @RequestMapping(path = "/shuffle",method = RequestMethod.GET)
    String shuffleSongs(RedirectAttributes attributes){

        selectedSongIndex=0;

        setSongs(nextSongsArray);
        selectedSong=presentSongsArray[selectedSongIndex];
        attributes.addFlashAttribute("songs",presentSongsArray);
        return "redirect:/";
    }


    //曲を指定するメソッド。再生中の曲を表示する処理は、index()メソッドに記述。
    @RequestMapping(path = "/play",method = RequestMethod.GET)
    String playSong(RedirectAttributes attributes){
        selectedSong=presentSongsArray[selectedSongIndex];
        return "redirect:/";
    }

    @RequestMapping(path = "/play/next",method = RequestMethod.GET)
    String playNextSong(RedirectAttributes attributes){
        selectedSong=getNextSong();

        return "redirect:/";
    }


    //ランダム処理で選ばれる５個の曲を設定する
    public void setSongs(Song[] songs){
        presentSongsArray=songs;

        //現在の曲リストと次巡の曲リストが異なるように設定
        //曲が１つしかない場合は、次巡の曲に単にpeekQueue()を設定するだけ
        if(storedSongs.size()!= 1) {
            do {
                nextSongsArray = peekQueue();
            } while (Arrays.equals(presentSongsArray,nextSongsArray));
        }
        else {
            nextSongsArray=peekQueue();
        }
    }


    //次に再生する曲(Song)を返す。
    public Song getNextSong(){

        selectedSongIndex++;
        selectedSongIndex= (int) (selectedSongIndex%min(storedSongs.size(),presentSongsArray.length));

        return presentSongsArray[selectedSongIndex];
    }


    //次に再生する予定の曲を先読み(PEEKMAXを上限)して配列として返す。
    public Song[]peekQueue(){

        Collections.shuffle(storedSongs);

        int num= (int) min(storedSongs.size(),PEEKMAX);
        Song[]queueArray=new Song[num];
        for(int i=0;i<num;i++){
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