package com.example.songs_album;

import javax.persistence.*;

//Songクラスがリポジトリにあたり、個々のインスタンスがDBに格納される。
@Entity
public class Song {

    public final Integer PEEKMAX=5;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = true)
    private String file_name;

    @Column(nullable = true)
    private String file_path;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }
}
