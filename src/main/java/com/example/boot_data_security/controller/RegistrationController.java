package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registration(User user) {
        return "registration";
    }

    @PostMapping("/registration")
    public String checkUserInfo(@Valid User user, BindingResult bindingResult, Model model) {
        if (user.getPassword() != null && !user.getPassword().equals(user.getPassword2())) {
            model.addAttribute("passwordError", "Різні паролі");
            return "registration";
        }
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        userService.addUser(user);
        return "redirect:/login";

    }

    @GetMapping("/activate_for_registration/{code}")
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
