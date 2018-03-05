package com.example.songs_album;

import com.example.songs_album.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Float.min;

@Controller
public class SongController implements ShuffleEngine{

    @Autowired
    SongRepository repository;

    //全体の
    ArrayList<Song>songsList=new ArrayList<>();

    String storePath="/Users/meisei/Documents/abc_app/songs_album/target/classes/static";

    //表示されている５つの曲のうち、何番目か
    int selectedSongIndex=0;

    //表示されている５つの曲の配列
    Song[]songs=new Song[5];

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
    }

    //TODO
    @RequestMapping(path = "/",method = RequestMethod.GET)
    ModelAndView index(ModelAndView mav){
        mav.setViewName("index");
        setSongs(repository.findAll().toArray(new Song[repository.findAll().size()]));
        mav.addObject("files",this.songsList);
        //mav.addObject("songs",peekQueue());

        songs=peekQueue();
        return mav;
    }


    @RequestMapping(path = "/shuffle",method = RequestMethod.GET)
    String shuffleSongs(RedirectAttributes attributes){
        attributes.addFlashAttribute("songs",peekQueue());
        return "redirect:/";
    }


    @RequestMapping(path = "/play",method = RequestMethod.GET)
    String playSong(RedirectAttributes attributes){
        String _selectedSongName=songs[selectedSongIndex].getFile_name();
        attributes.addFlashAttribute("selectedSong",_selectedSongName);
        return "redirect:/";
    }


    //シャッフル対象の曲(Song)の配列をインスタンス(何の？)に設定
    //再生する５曲というよりは、対象となる曲全体のことを指すはず。
    //peakQueueとの兼ね合いはどうするのか
    //↓ここもうちょっと綺麗にかける
    //呼び出し側で、setSongs(repository.findAll().toArray(new Song[repository.findAll().size()]));
    public void setSongs(Song[] songs){  //引数には、「シャッフル対象の曲」(たぶんrepository.findAll())
        this.songsList.clear();
        Collections.addAll(this.songsList,songs);  //これ単体だと、呼び出されるたびにsongsListに(前の情報を残したまま)「追加」され続けてしまう。
    }


    //次に再生する曲(Song)を返す。次に返す曲が更新される。再生ボタン押されたとき(playSong()の中で？)呼ばれる。
    public Song getNextSong(){

        Song nextSong= songsList.get(selectedSongIndex+1);
        selectedSongIndex++;

        return nextSong;
    }


    //次に再生する予定の曲を先読み(PEEKMAXを上限)して配列として返す。
    public Song[]peekQueue(){

        Collections.shuffle(songsList);

        ArrayList<Song>queueList=new ArrayList<>();
        for(int i=0;i<min(songsList.size(),PEEKMAX);i++){
            queueList.add(songsList.get(i));
        }

        Song[]queueArray=queueList.toArray(new Song[queueList.size()]);

        return queueArray;
    }
}