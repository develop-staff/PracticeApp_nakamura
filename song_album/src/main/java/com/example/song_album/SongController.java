package com.example.song_album;

import org.springframework.stereotype.Controller;

@Controller
public class SongController implements ShuffleEngine{




    //TODO
    public void setSongs(Song[]songs){
        //シャッフル対象の曲(Song)の配列をインスタンスに設定する
    }

    //TODO
    public Song getNextSong(){
        //次に再生する曲(Song)を返す。次に返す曲が更新される
        //カウント作って、それをどんどん更新していけばよさそう
        return new Song();
    }

    //TODO
    public Song[]peekQueue(){
        //次に再生する予定の曲を先読み(PEEKMAXを上限)して配列として返す。
        //最初に１巡分の曲順を決める場合も、たぶん普通に先読みしていって
        //カウントをずらしていけばいける。
        //別のメソッドで、この戻り値の配列の０番目を取得する、という処理をすれば良さそう。
        return new Song[PEEKMAX];
    }
}
