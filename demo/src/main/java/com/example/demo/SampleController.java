package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.io.FileNotFoundException;

import java.lang.reflect.Array;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

@Controller
public class SampleController {


//    @RequestMapping(path = "/upload", method = RequestMethod.GET)
//    String uploadview(Model model) {
//        return "/upload";
//    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    String upload(Model model, UploadForm uploadForm) {
        if (uploadForm.getFile().isEmpty()) {
            return "/upload";
        }

        //Path path = Paths.get("/Users/nakamura/PracticeApp_nakamura/demo/image");  TODO
        Path path = Paths.get("/Users/nakamura/PracticeApp_nakamura/demo/src/main/resources/static");
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
                .get("/Users/nakamura/PracticeApp_nakamura/demo/src/main/resources/static/"+filename);
                //.get("/Users/nakamura/PracticeApp_nakamura/demo/image/" + filename);  TODO

        try (OutputStream os = Files.newOutputStream(uploadfile, StandardOpenOption.CREATE)) {
            byte[] bytes = uploadForm.getFile().getBytes();

            os.write(bytes);
            os.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }

        return "redirect:/upload";
    }


    @RequestMapping(path = "/upload",method = RequestMethod.GET)
    ModelAndView show(ModelAndView mav){
        mav.setViewName("upload");

        ArrayList<String>img=new ArrayList<>();

        //File dir=new File("/Users/nakamura/PracticeApp_nakamura/demo/image");  TODO
        File dir=new File("/Users/nakamura/PracticeApp_nakamura/demo/src/main/resources/static");
        File[]files=dir.listFiles();

        for(int i=0;i<files.length;i++){
            //img.add(new File("/Users/nakamura/PracticeApp_nakamura/demo/image/"+files[i]).getName());  TODO
            img.add(new File("/Users/nakamura/PracticeApp_nakamura/demo/src/main/resources/static/"+files[i]).getName());
        }

        Collections.shuffle(img);
        ArrayList<String>result=img;

        mav.addObject("files",img);
        return mav;
    }
}
