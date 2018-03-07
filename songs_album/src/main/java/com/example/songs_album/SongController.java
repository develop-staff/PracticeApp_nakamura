package com.example.songs_album;

import com.example.songs_album.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
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
import java.util.List;

import static java.lang.Float.min;

@Controller
public class SongController implements ShuffleEngine{

    @Autowired
    SongRepository repository;

    String storePath="/Users/meisei/Documents/abc_app/songs_album/target/classes/static";

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

    @PostConstruct
    //ダミーデータ(10個)の作成
    public void init(){

        File file[]=new File[10];
        Song song[]=new Song[10];
        for(int i=0;i<10;i++){
            file[i]=new File(storePath+"/song_"+(i)+".mp3");
            song[i]=new Song();
            song[i].setFile_name(file[i].getName());
            song[i].setFile_path("song_"+(i)+".mp3");
            repository.saveAndFlush(song[i]);
        }

        //TODO
        setSongs(peekQueue());
        //setSongs(nextSongsList);
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
        //setSongs(peekQueue());
        attributes.addFlashAttribute("songs",presentSongsList);
        return "redirect:/";
    }


    //曲の指定するメソッド。再生中の曲を表示する処理は、index()メソッドに記述。
    @RequestMapping(path = "/play",method = RequestMethod.GET)
    String playSong(RedirectAttributes attributes){
        selectedSongIndex=selectedSongIndex;

        return "redirect:/";
    }

    @RequestMapping(path = "/play/next",method = RequestMethod.GET)
    String playNextSong(RedirectAttributes attributes){
        getNextSong();
        selectedSongIndex=(selectedSongIndex%5);

        return "redirect:/";
    }


    //ランダム処理で選ばれる５個の曲を設定する
    public void setSongs(Song[] songs){
        presentSongsList=songs;

        //現在の曲リストと次巡の曲リストが異なるように設定
        do{
            nextSongsList=peekQueue();
        }while (presentSongsList==nextSongsList);
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

        Song[]queueArray=new Song[5];
        for(int i=0;i<min(storedSongs.size(),PEEKMAX);i++){
            queueArray[i]=storedSongs.get(i);
        }
        return queueArray;
    }






    //この段階で、ファイルへのパスをデータベースに保存してやる
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
                .get(storePath+"/"+filename);  //TODO


        try (OutputStream os = Files.newOutputStream(uploadfile, StandardOpenOption.CREATE)) {
            byte[] bytes = uploadForm.getFile().getBytes();

            os.write(bytes);
            os.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }

        File file=new File(path+"/"+filename);

        mydata.setFile_path("/"+filename);  //ここでファイルへのパスをレポジトリに保存する
        mydata.setFile_name(filename);

        repository.saveAndFlush(mydata);

        return new ModelAndView("redirect:/");
    }





}