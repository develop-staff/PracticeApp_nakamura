package com.example.song_album;

import com.example.song_album.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Float.min;

@Controller
public class SongController implements ShuffleEngine{

    @Autowired
    SongRepository repository;

    ArrayList<Song>songsList=new ArrayList<>(repository.findAll());

    int count=0;



    //ダミーデータの作成

    //音楽ファイルのパスを取得し、返すメソッド
    //@RequestMapping(path="/",method = RequestMethod.GET)
    //index()

    //画像をアップロードし、パスをデータベースに保存するメソッド
    //@RequestMapping(path = "/upload",method = RequestMethod.POST)
    //update()

    //TODO
    public void setSongs(Song[]songs){  //引数には、「シャッフル対象の曲」

        //peakQueueは次の順番の曲リスト。これは「今回の」曲の配列
        //ただ、次回以降の配列はpeakQueueで取得するわけだし、どうするんやろ。


        //シャッフル対象の曲(Song)の配列をインスタンスに設定する

        //ページ更新の際、画像アップロードの際には呼ばれない？ TODO
        //「シャッフルして曲を再生」ボタンを押したときに呼ばれる？
        //ていうか、「シャッフル」と「曲を再生」はボタン、メソッドを分けるべきな気もする

        //引数 Song[]songを、何にいれるのか…。

    }

    //TODO
    // 引数なしなのか。
    public Song getNextSong(){
        //次に再生する曲(Song)を返す。次に返す曲が更新される

        //カウント作って、それをどんどん更新していけばよさそう(カウントはメソッドの外)


        //どこで呼ばれるか
        //再生ボタン押して、順次曲が再生されていくなかで呼ばれていく TODO

        Song nextSong= songsList.get(count+1);
        count++;

        return nextSong;
    }


    //TODO
    public Song[]peekQueue(){
        //次に再生する予定の曲を先読み(PEEKMAXを上限)して配列として返す。

        //どこで呼ばれるか　//TODO
        //次に再生する５曲をランダムにとってきたものを配列に保存し
        //「シャッフルをして曲を再生」ボタンを押したときにそれを受け取る(さらにその戻り値はindex() or showSongList()が受け取る)
        //ので、peekQueue()メソッドはsetSongs()メソッドで呼び出される?

        Collections.shuffle(songsList);

        ArrayList<Song>queueList=new ArrayList<>();

        for(int i=0;i<min(songsList.size(),PEEKMAX);i++){  //ここsizeじゃなくてlengthかも
            queueList.add(songsList.get(i));
        }

        Song[]queueArray=queueList.toArray(new Song[queueList.size()]);

        return queueArray;
    }
}

