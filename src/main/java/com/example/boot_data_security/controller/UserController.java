package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.UserService;
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


    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public String userList(Model model) {
        model.addAttribute("users", userService.allUsers());
        return "all_users";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{user}")
    public String userEditForm(@PathVariable("user") User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user_edit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("{id}")
    public String userSave(@ModelAttribute("user") User user, @PathVariable Long id, @RequestParam Map<String, String> form) {
        userService.update(user, id, form);
        return "redirect:/user";
    }


    @GetMapping("profile")
    public String getProfile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        return "profile";
    }

    @GetMapping("/update_username")
    public String getFormToUpdateUsername() {
        return "update_username";
    }

    @PostMapping("/update_username")
    public String updateUsername(@AuthenticationPrincipal User user, @RequestParam String username, Model model) {
        //boolean isEmailChanged =userService.updateProfile(user, password, email);

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
    public String updateEmail() {
        return "update_email";
    }

    @PostMapping("update_email")
    public String getFormToUpdateEmail(@AuthenticationPrincipal User user, @RequestParam String email, Model model) {
        if (userService.isEmailChanged(user, email)) {
            userService.updateEmail(user, email);
//            model.addAttribute("message", "Email змінено");
//            return "profile";
        }
        return "redirect:/user/update_email";
    }

    @GetMapping("/update_password")
    public String updatePassword() {
        return "update_password";
    }

    @PostMapping("/update_password")
    public String getFormToUpdatePassword(@AuthenticationPrincipal User user, @RequestParam String password, Model model) {
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
        boolean isActivated = userService.activateUser(code);
        if (isActivated) {
            user.setEmail(email);
          userService.save(user);
            model.addAttribute("message", "Пошту змінено");
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
        } else {
            model.addAttribute("message", "Код активації не знайдено");
        }
        return "profile";
    }
}
