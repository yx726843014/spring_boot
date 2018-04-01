package com.youxiong.web;

import com.youxiong.Application;
import com.youxiong.domain.User;
import com.youxiong.repository.UserRepository;
import javafx.beans.binding.ObjectExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @RequestMapping(value = "/mongoTest")
    public String test(){
        System.out.println("111");
        User user = new User();
        user.setId("1");
        user.setUsername("aaa");
        user.setPassword("aaa");
        User save = userRepository.save(user);
        return save.toString();
    }
}
