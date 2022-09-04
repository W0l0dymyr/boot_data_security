package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.repos.UserRepo;
import com.example.boot_data_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;




    @GetMapping
    public String userList(Model model){
        model.addAttribute("users", userService.allUsers());
        return "all_users";
    }

    @GetMapping("{user}")
    public String userEditForm(@PathVariable ("user") User user, Model model){
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user_edit";
    }

@PostMapping("{id}")
    public String userSave(@ModelAttribute("user") User user, @PathVariable Long id, @RequestParam Map<String, String> form ){
        userService.update(user, id, form);
    return "redirect:/user";
}
}
