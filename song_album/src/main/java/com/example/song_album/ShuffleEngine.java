package com.example.song_album;


//曲の再生が１巡する間に、なるべく同じ曲がかかりにくくする工夫をする
//最初に１巡分の曲順を決定する場合、２巡目になったら１巡目と違う順番になるようにする
public interface ShuffleEngine {
    public final Integer PEEKMAX=5;

    //シャッフル対象の曲(Song)の配列をインスタンスに設定する
    void setSongs(Song[]songs);

    //次に再生する曲(Song)を返す。次に返す曲が更新される。
    Song getNextSong();

    //次に再生する予定の曲を先読み(PEEKMAXが上限)して配列として返す
    Song[]peekQueue();
}