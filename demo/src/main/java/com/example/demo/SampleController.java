package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
public class SampleController {


    @RequestMapping(path = "/upload", method = RequestMethod.GET)
    String uploadview(Model model) {
        return "/upload";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    String upload(Model model, UploadForm uploadForm) {
        if (uploadForm.getFile().isEmpty()) {
            return "/upload";
        }

        Path path = Paths.get("/Users/meisei/IdeaProjects/demo/src/main/resources/static");
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
                .get("/Users/meisei/IdeaProjects/demo/src/main/resources/static/" + filename);

        try (OutputStream os = Files.newOutputStream(uploadfile, StandardOpenOption.CREATE)) {
            byte[] bytes = uploadForm.getFile().getBytes();

            os.write(bytes);
            os.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }

        return "redirect:/upload";
    }

    //たぶん呼び出されていない。
    //value, methodを指定すると、なぜかエラーが生じる。
    //@RequestMapping(value = "/upload",method = RequestMethod.GET)
    @RequestMapping("/upload")
    ModelAndView show(ModelAndView mav){
        mav.setViewName("upload");
        ArrayList<File>files=new ArrayList<File>();
        files.add(new File("/*.jpg"));

        String imgs=files.toString();
        mav.addObject("files",imgs);
        return mav;
    }
}