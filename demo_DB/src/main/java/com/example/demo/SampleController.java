package com.example.demo;

import com.example.demo.repositories.MyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;

@Controller
public class SampleController {

    @Autowired
    MyDataRepository repository;

    String storePath="/Users/meisei/Documents/abc_app/demo_DB/target/classes/static";

    //インスタンス生成時に初期化されるデータ
    @PostConstruct
    public void init(){

        File file_1=new File(storePath+"/linux.jpg");
        File file_2=new File(storePath+"/penguin.jpg");

        //ダミーデータ
        MyData d1=new MyData();
        d1.setName(file_1.getName());
        d1.setFile_path("/linux.jpg");
        d1.setFile(file_1);  //ここでファイル自体を保存しても、使わない…？。
        //d1.setImg();

        MyData d2=new MyData();
        d2.setName(file_2.getName());
        d2.setFile_path("/penguin.jpg");
        d2.setFile(file_2);

        repository.saveAndFlush(d1);
        repository.saveAndFlush(d2);
    }



    //この段階で、ファイルへのパスをデータベースに保存してやる
    //ファイルをアップロードするメソッド
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    @Transactional(readOnly = false)
    ModelAndView upload(@ModelAttribute @Valid MyData mydata, BindingResult result, UploadForm uploadForm) {

        if (uploadForm.getFile().isEmpty()) {
            return new ModelAndView("/upload");
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
        mydata.setFile(file);  //一応、ファイル自体もリポジトリに保存した

        repository.saveAndFlush(mydata);

        return new ModelAndView("redirect:/upload");
    }




    //ファイル一覧のパスを取得するメソッド(データベースの場合はこうじゃないかも)
    @RequestMapping(path = "/upload",method = RequestMethod.GET)
    ModelAndView show(ModelAndView mav){
        mav.setViewName("upload");

        //DBに保存されているファイルのパスを取得
        //Iterable<MyData>list=repository.findAll();
        ArrayList<MyData>ls=new ArrayList<>(repository.findAll());

        Collections.shuffle(ls);
        ArrayList<MyData>result=ls;

        mav.addObject("files",result);

        return mav;
    }
}
