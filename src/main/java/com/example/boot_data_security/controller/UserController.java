package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/user")

public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger LOGGER = LogManager.getLogger(RegistrationController.class);


    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public String userList(Model model) {
        LOGGER.info("Getting view the list of all users");
        model.addAttribute("users", userService.allUsers());
        return "all_users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{user}")
    public String userEditForm(@PathVariable("user") User user, Model model) {
        LOGGER.info("Getting view the user edit");
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user_edit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("{id}")
    public String userSave(@ModelAttribute("user") User user, @PathVariable Long id, @RequestParam Map<String, String> form) {
        LOGGER.info("Saving a user after editing in controller");
        userService.update(user, id, form);
        return "redirect:/user";
    }


    @GetMapping("profile")
    public String getProfile(@AuthenticationPrincipal User user, Model model) {
        LOGGER.info("Getting view the profile");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        return "profile";
    }

    @GetMapping("/update_username")
    public String getFormToUpdateUsername() {
        LOGGER.info("Getting view the update username");
        return "update_username";
    }

    @PostMapping("/update_username")
    public String updateUsername(@AuthenticationPrincipal User user, @RequestParam String username, Model model) {
        LOGGER.info("Saving an updated username in controller");
        if (username.length() < 2 || username.length() > 30) {
            model.addAttribute("message", "ім'я повинно складатися від 2 до 30 символів");
            return "update_username";
        }
        if (userService.isUsernameChanged(user, username)) {

            userService.updateUsername(user, username);
            model.addAttribute("message", "Ім'я змінено");
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
            return "profile";
        }
        return "redirect:/user/update_username";
    }

    @GetMapping("/update_email")
    public String getFormToUpdateEmail() {
        LOGGER.info("Getting view the update email");
        return "update_email";
    }

    @PostMapping("update_email")
    public String updateEmail(@AuthenticationPrincipal User user, @RequestParam String email, Model model) {
        LOGGER.info("Saving an updated email in controller");
        if (email.length() < 5 || !email.contains("@")) {
            model.addAttribute("message", "Email має містити в собі @");
            return "update_email";
        }
        if (userService.isEmailChanged(user, email)) {
            userService.updateEmail(user, email);
        }
        return "redirect:/user/update_email";
    }

    @GetMapping("/update_password")
    public String getFormToUpdatePassword() {
        LOGGER.info("Getting view the update password");
        return "update_password";
    }

    @PostMapping("/update_password")
    public String updatePassword(@AuthenticationPrincipal User user, @RequestParam String password, Model model) {
        LOGGER.info("Saving an updated password in controller");
        if (password.length() < 6) {
            model.addAttribute("message", "пароль повинен складатися мінімум із 6 символів");
            return "update_password";
        }
        if (userService.isPasswordChanged(user, password)) {
            userService.updatePassword(user, password);
            model.addAttribute("message", "Пароль змінено");
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
            return "profile";
        }
        return "redirect:/user/update_password";
    }

    @GetMapping("/activate_for_update/{code}")
    public String activate(@AuthenticationPrincipal User user, Model model, @PathVariable String code, @RequestParam("email") String email) {
        LOGGER.info("'activate' method for updating is running");
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            user.setEmail(email);
            user.setActive(true);
            userService.save(user);
            LOGGER.info("User is successfully activated");
            model.addAttribute("message", "Пошту змінено");
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
        } else {
            LOGGER.info("Activation code is not found");
            model.addAttribute("message", "Код активації не знайдено");
        }
        return "profile";
    }
}
