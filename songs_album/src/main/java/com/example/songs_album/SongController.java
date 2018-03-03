package com.example.songs_album;

import com.example.songs_album.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Float.min;

@Controller
public class SongController implements ShuffleEngine{

    @Autowired
    SongRepository repository;

    ArrayList<Song>songsList;
    //ArrayList<Song>songsList=new ArrayList<>(repository.findAll());

    int playCount=0;


    //TODO
    //シャッフル対象の曲(Song)の配列をインスタンス(何の？)に設定
    //再生する５曲というよりは、対象となる曲全体のことを指すはず。
    //peakQueueとの兼ね合いはどうするのか
    public void setSongs(Song[]songs){  //引数には、「シャッフル対象の曲」(たぶんrepository.findAll())
        Collections.addAll(this.songsList,songs);
    }

    //TODO
    //次に再生する曲(Song)を返す。次に返す曲が更新される。再生ボタン押されたとき(playSong()の中で？)呼ばれる。
    public Song getNextSong(){

        Song nextSong= songsList.get(playCount+1);
        playCount++;

        return nextSong;
    }

    //TODO
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