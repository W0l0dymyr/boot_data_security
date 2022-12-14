package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.City;
import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.service.CityService;
import com.example.boot_data_security.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.IntStream;

@Controller
public class MainController {
    @Autowired
    private CityService cityService;
    @Autowired
    private UserService userService;
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);
    private int count;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String main() {
        LOGGER.info("Getting view the start page");
        return "main";
    }

    @GetMapping("/menu")
    public String menu(@AuthenticationPrincipal User user) {
        if (user.getUsedCities().size() > 0) {
            user.getUsedCities().clear();
        }
        LOGGER.info("Getting view the menu page");
        return "menu";
    }

    @GetMapping("/add_city")
    public String addCity() {
        LOGGER.info("Getting view the add city page");
        return "add_city";
    }

    @PostMapping("/save_new_city")
    public String saveCity(@ModelAttribute City city, Model model, @RequestParam("file") MultipartFile multipartFile,
                           @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) throws IOException {
        LOGGER.info("Saving new city");
        City cityFromDB = cityService.findByTitle(city.getTitle());
        if (cityFromDB != null) {
            LOGGER.debug("This city " + cityFromDB.getTitle() + " is already known to the bot");
            model.addAttribute("message", "?????? ?????? ???????? ???????? ??????????");
            return "add_city";
        }
        if (multipartFile != null) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + ".jpg";
            multipartFile.transferTo(new File(uploadPath + "/" + resultFilename));
            city.setFilename(resultFilename);
        }
        cityService.save(city);
        LOGGER.info(city.getTitle() + " is saved");
        model.addAttribute("message", "?????????? ?????????????? ????????????");
        Page<City> pageCities = cityService.findAll(page);
        model.addAttribute("pageCities", pageCities);
        model.addAttribute("numbers", IntStream.range(0, pageCities.getTotalPages()).toArray());
        return "all_cities";
    }

    @GetMapping("/all_cities")
    public String allCities(Model model, @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {
        LOGGER.info("Getting view with the list of known cities");
        Page<City> pageCities = cityService.findAll(page);
        model.addAttribute("pageCities", pageCities);
        model.addAttribute("numbers", IntStream.range(0, pageCities.getTotalPages()).toArray());
        return "all_cities";
    }

    @GetMapping("/city/{id}")
    public String showCity(@PathVariable("id") Long id, Model model) {
        model.addAttribute("city", cityService.findById(id));
        LOGGER.info("Getting view the city");
        return "city";
    }


    @GetMapping("/play")
    public String enterCity(@ModelAttribute("city") City city, @AuthenticationPrincipal User user, Model model) {
        LOGGER.info("Getting view the game");
        if (user.getUsedCities().size() == 0) {  //???????? ?? 0 ???????????????????????? ????????, ???? ???? ??????????????, ????
            LOGGER.debug("'usedCities' is empty. That's start of game");  //???????? ?? 0 ???????????????????????? ????????, ???? ???? ??????????????, ????
            count = 0;
            City nullCity = new City();
            model.addAttribute("botsCity", nullCity);
            return "play";
        } else {
            count++;
            City botsCity = cityService.bot(user);
            if (botsCity == null) {
                LOGGER.debug("Bot doesn't know city, it lose");
                user.getUsedCities().clear();
                model.addAttribute("count", "???? ???????????????????? " + (count) + " ????????(??)");
                userService.setRecord(count);
                return "you_win";
            } else {
                LOGGER.info("Bot named city " + botsCity.getTitle());
                System.out.println(botsCity.getTitle());
                model.addAttribute("botsCity", botsCity);

                return "play";
            }
        }

    }

    @GetMapping("/save_used")
    public String saveCity(@ModelAttribute("city") @Valid City city,
                               @AuthenticationPrincipal User user, BindingResult bindingResult, Model model) throws IOException {
        LOGGER.info("Saving the name of the city entered by player");

        if (bindingResult.hasErrors()) {
            LOGGER.debug("Error during entering the name of the city");
            return "play";
        }
        if (cityService.checkIfCityExists(city.getTitle(), "https://uk.wikipedia.org/wiki/")) {

            if (user.getUsedCities().size() == 0) {
                userService.makeUsedCity(user, city);
                cityService.save(city);

                return "redirect:/play";
            } else {
               return cityService.checkingIfCityMatches(user, model, city);
            }
        } else {
            return "tryagain";

        }
    }
    @GetMapping("/tryagain")
    public String tryagain(@ModelAttribute("city") City city) {
        LOGGER.info("Getting view 'try again'");
        return "tryagain";
    }



    @GetMapping("/best_results")
    public String bestResults(Model model) {
        LOGGER.info("Getting view the best results");
        model.addAttribute("users", userService.allUsers());
        return "/best_results";
    }

    @GetMapping("/to_lose")
    public String toLose(@AuthenticationPrincipal User user, Model model) {
        LOGGER.info("Getting view the losing");
        user.getUsedCities().clear();
        model.addAttribute("count", "???? ???????? ?????????????? "+count+" ?????????????????? ??????????????");
        Integer bestResult=userService.allUsers().stream().mapToInt(User::getRecord).max().getAsInt();
         model.addAttribute("bestResult", "???? ???????? ?????????????????? ?????????????????? "+bestResult);
        return "/to_lose";
    }

}
