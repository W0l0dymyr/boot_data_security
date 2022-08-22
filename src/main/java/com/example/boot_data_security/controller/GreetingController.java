package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.City;
import com.example.boot_data_security.entities.Role;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.CityService;
import com.example.boot_data_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring5.expression.Fields;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class GreetingController {
    @Autowired
    private CityService cityService;
    @Autowired
    private UserService userService;
    private int count;

    @GetMapping("/")
    public String main() {
        return "main";
    }

    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }

    @GetMapping("/all_cities")
    public String allCities(Model model) {
        Iterable<City> cities = cityService.findAll();
        model.addAttribute("cities", cities);
        return "all_cities";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Model model) {  // реєстрація
        User userFromDb = userService.findByUsername(user.getUsername());    //шукаємо в бд чи є корстувач з таким іменем

        if (userFromDb != null) {
            model.addAttribute("message", "Такий користувач вже існує"); //якщо вже є, то вертає назад
            return "registration";
        }
        user.setActive(true);    // якщо користувача з таки іменем немає, то сетимо йому поля і зберігаємо
        user.setRoles(Collections.singleton(Role.USER));
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping("/play")
    public String enterCity(@ModelAttribute("city") City city, Model model) {
        if (cityService.getUsedCities().size() == 0) {  //якщо є 0 викорастаних міст, то це означає, що
            count = 0;                                 //
            City nullCity = new City();
            model.addAttribute("botsCity", nullCity);
            return "play";
        } else {
            count++;
            City botsCity = cityService.bot();
            if (botsCity == null) {
                cityService.getUsedCities().clear();
                model.addAttribute("count", "Ти використав " + (count) + " міст(а)");
                //if(count>userService.getRecord()){

                userService.setRecord(count);
                //  }
                return "you_win";
            } else {
                System.out.println(botsCity.getTitle());
                model.addAttribute("botsCity", botsCity);
                return "play";
            }
        }

    }

    @GetMapping("/save_used")
    public String saveInstance(@ModelAttribute("city") @Valid City city, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "play";
        }
        if (cityService.getUsedCities().size() == 0) {

            cityService.makeUsedCities(city);
            cityService.save(city);
            return "redirect:/play";
        } else {
            if (city.getTitle().startsWith(cityService.getLastLetter().toUpperCase())) {
                return checkIfCityIsUnused(model, city);
            } else {

                City botsCity = cityService.getUsedCities().get(cityService.getUsedCities().size() - 1);
                System.out.println(botsCity.getTitle() + " last bots city");
                model.addAttribute("botsCity", botsCity.getTitle());
                return "tryagain";
            }
        }
    }

    @GetMapping("/tryagain")
    public String tryagain(@ModelAttribute("city") City city) {
        return "tryagain";
    }

    private String checkIfCityIsUnused(Model model, City city) {
        if (cityService.getUsedCities().stream().anyMatch(c -> c.getTitle().equals(city.getTitle()))) {
            City botsCity = cityService.getUsedCities().get(cityService.getUsedCities().size() - 1);
            model.addAttribute("botsCity", botsCity.getTitle());
            System.out.println(city.getTitle()+ " checking");
            return "tryagain";
        } else {
            cityService.makeUsedCities(city);
            cityService.save(city);
            return "redirect:/play";
        }
    }

    @GetMapping("/best_results")
    public String bestResults(Model model){
        model.addAttribute("users", userService.allUsers());
        return "/best_results";
    }

}