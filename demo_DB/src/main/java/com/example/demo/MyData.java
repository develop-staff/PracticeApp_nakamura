package com.example.demo;


import javax.persistence.*;
import java.io.File;
import java.sql.Blob;

@Entity

public class MyData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = true)
    private File file;

    @Column(nullable = true)
    private String file_path;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String memo;



    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }
    //    @Column(nullable = true)
//    private Blob img;
//
//    public Blob getImg() {
//        return img;
//    }
//
//    public void setImg(Blob img) {
//        this.img = img;
//    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
