package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.repos.UserRepo;
import com.example.boot_data_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public String userList(Model model){
        model.addAttribute("users", userService.allUsers());
        return "all_users";
    }

//    @GetMapping("{user}")
//    public String userEditForm(@PathVariable int id, Model model){
//        model.addAttribute("user", id);
//        model.addAttribute("roles", Role.values());
//        return "user_edit";
//    }

    @GetMapping("{user}")
    public String userEditForm(@PathVariable User user, Model model){
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user_edit";
    }
}
