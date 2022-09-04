package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Model model) {  // реєстрація
        User userFromDb = userService.findByUsername(user.getUsername());    //шукаємо в бд чи є корстувач з таким іменем
        if (!userService.addUser(user)) {
            model.addAttribute("message", "Користувач з таким іменем або поштою вже існує"); //якщо вже є, то вертає назад
            return "registration";
        }
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            model.addAttribute("message", "Користувач успішно активований");
        } else {
            model.addAttribute("message", "Код активації не знайдено");
        }
        return "login";
    }


}
