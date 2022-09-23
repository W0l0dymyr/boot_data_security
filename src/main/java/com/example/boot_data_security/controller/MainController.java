package com.example.boot_data_security.controller;

import com.example.boot_data_security.entities.City;
import com.example.boot_data_security.service.CityService;
import com.example.boot_data_security.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
    public String menu() {
        LOGGER.info("Getting view the menu page");
        return "menu";
    }

    @GetMapping("/add_city")
    public String addCity(){
        LOGGER.info("Getting view the add city page");
        return "add_city";
    }

    @PostMapping("/save_new_city")
    public String saveCity(@ModelAttribute City city, Model model, @RequestParam ("file")MultipartFile multipartFile) throws IOException {
        LOGGER.info("Saving new city");
        City cityFromDB = cityService.findByTitle(city.getTitle());
        if(cityFromDB!=null){
            LOGGER.debug("This city " + cityFromDB.getTitle()+" is already known to the bot");
         model.addAttribute("message", "Бот вже знає таке місто");
         return "add_city";
        }
        if(multipartFile!=null){
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + ".jpg";
            multipartFile.transferTo(new File(uploadPath+"/" +resultFilename));


            city.setFilename(resultFilename);
        }
        cityService.save(city);
        LOGGER.info(city.getTitle()+" is saved");
        model.addAttribute("message", "Місто успішно додано");
        Iterable<City> cities = cityService.findAll();
        model.addAttribute("cities", cities);
        return "all_cities";
    }

    @GetMapping("/all_cities")
    public String allCities(Model model) {
        LOGGER.info("Getting view with the list of known cities");
        Iterable<City> cities = cityService.findAll();
        model.addAttribute("cities", cities);
        return "all_cities";
    }

    @GetMapping("/city/{id}")
    public String showCity(@PathVariable ("id") Long id, Model model){
        model.addAttribute("city", cityService.findById(id));
        LOGGER.info("Getting view the city");
        return "city";
    }



    @GetMapping("/play")
    public String enterCity(@ModelAttribute("city") City city, Model model) {
        LOGGER.info("Getting view the game");
        if (cityService.getUsedCities().size() == 0) {  //якщо є 0 викорастаних міст, то це означає, що
            LOGGER.debug("'usedCities' is empty. That's start of game");
            count = 0;
            City nullCity = new City();
            model.addAttribute("botsCity", nullCity);
            return "play";
        } else {
            count++;
            City botsCity = cityService.bot();
            if (botsCity == null) {
                LOGGER.debug("Bot doesn't know city, it lose");
                cityService.getUsedCities().clear();
                model.addAttribute("count", "Ти використав " + (count) + " міст(а)");
                userService.setRecord(count);
                return "you_win";
            } else {
                LOGGER.info("Bot named city "+ botsCity.getTitle());
                System.out.println(botsCity.getTitle());
                model.addAttribute("botsCity", botsCity);
                return "play";
            }
        }

    }

    @GetMapping("/save_used")
    public String saveInstance(@ModelAttribute("city") @Valid City city, BindingResult bindingResult, Model model) {
        LOGGER.info("Saving the name of the city entered by player");
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Error during entering the name of the city");
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
                LOGGER.debug(city.getTitle()+" does not start with "+cityService.getLastLetter());
                City botsCity = cityService.getUsedCities().get(cityService.getUsedCities().size() - 1);
                model.addAttribute("botsCity", botsCity.getTitle());
                return "tryagain";
            }
        }
    }

    @GetMapping("/tryagain")
    public String tryagain(@ModelAttribute("city") City city) {
        LOGGER.info("Getting view 'try again'");
        return "tryagain";
    }

    private String checkIfCityIsUnused(Model model, City city) {
        if (cityService.getUsedCities().stream().anyMatch(c -> c.getTitle().equals(city.getTitle()))) {
            LOGGER.debug(city.getTitle()+" is used");
            City botsCity = cityService.getUsedCities().get(cityService.getUsedCities().size() - 1);
            model.addAttribute("botsCity", botsCity.getTitle());
            return "tryagain";
        } else {
            LOGGER.info(city.getTitle() + " is not used");
            cityService.makeUsedCities(city);
            cityService.save(city);
            return "redirect:/play";
        }
    }

    @GetMapping("/best_results")
    public String bestResults(Model model){
        LOGGER.info("Getting view the best results");
        model.addAttribute("users", userService.allUsers());
        return "/best_results";
    }

}