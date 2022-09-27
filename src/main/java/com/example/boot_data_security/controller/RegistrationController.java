package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger(RegistrationController.class);

    @GetMapping("/registration")
    public String registration(User user) {
        LOGGER.info("Getting view the registration page");
        return "registration";
    }

    @PostMapping("/registration")
    public String checkUserInfo(@Valid User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Data is not valid");
            return "registration";
        }
        if (user.getPassword() != null && !user.getPassword().equals(user.getPassword2())) {
            LOGGER.debug("Password are not the same");
            model.addAttribute("passwordError", "Різні паролі");
            return "registration";
        }
        LOGGER.info("Calling the 'addUser' method");
        if(userService.addUser(user)) {
            return "redirect:/login";
        }else {
            model.addAttribute("message", "Користувач з таким іменем або email вже існує");
            return "registration";
        }

    }

    @GetMapping("/activate_for_registration/{code}")
    public String activate(Model model, @PathVariable String code) {
        LOGGER.info("'activate' method for registration is running");
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            LOGGER.info("User is successfully activated");
            model.addAttribute("message", "Користувач успішно активований");
        } else {
            LOGGER.debug("Activation code is not found");
            model.addAttribute("message", "Код активації не знайдено");
        }
        return "login";
    }


}
