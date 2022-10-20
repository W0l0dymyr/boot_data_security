package com.example.boot_data_security.service;

import com.example.boot_data_security.entities.City;

import com.example.boot_data_security.entities.User;
import com.example.boot_data_security.repos.CityRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CityService {
    private static final Logger LOGGER = LogManager.getLogger(CityService.class);
    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserService userService;

    public Page<City> findAll(Integer page){
      return cityRepository.findAll(PageRequest.of(page,17, Sort.by("title")));
    }

    public City findByTitle(String title){
       return cityRepository.findByTitle(title);
    }

    public City findById(Long id){
        return cityRepository.findById(id).get();
    }

    public void save(City city) {
        if (cityRepository.getCity(city.getTitle()) == null) {
            cityRepository.save(city);
        }
    }

    public City bot(User user) {
        LOGGER.info("Bot is thinking");
        String letter = getLastLetter(user);     //дістаємо останню літеру назви міста
        List<City> citiesFromDB = cityRepository.searchByLetterStartWith(letter);  //шукаємо в базі міста на потрібну літеру
        if (citiesFromDB.size() == 0) {
            LOGGER.debug("There are no more known cities");
            return null;      //якщо такого міста нема в базі, то вертаємо нал, який означатиме, що користувач виграв
        } else {
            LOGGER.info("Choosing a city from DB");
            City city = getRandomCity(citiesFromDB);  //рандомно зі списку міст, які починаються на одну і ту ж літеру вибираємо місто
            return checksAndReturnsUnusedCity(citiesFromDB, user, city); // і вертаємо його
        }
    }
    private City checksAndReturnsUnusedCity(List<City> citiesFromDB, User user, City cityToCheck) {
        LOGGER.info("Bot checks if city is unused");
        if (user.getUsedCities().stream().anyMatch(c -> c.getTitle().equals(cityToCheck.getTitle()))) {//якшо вибране рандомно місто є в списку
            LOGGER.debug(cityToCheck.getTitle()+" is used");
            citiesFromDB.remove(cityToCheck);
            City anotherCity = findUnusedCity(citiesFromDB, user.getUsedCities());  // використаних місто, то шукаємо інше
            userService.makeUsedCity(user,anotherCity);  //місто зі списку не використаних
            return anotherCity;
        } else {
            LOGGER.info(cityToCheck.getTitle() +" is unused");
            userService.makeUsedCity(user,cityToCheck); // якшо таке місто ше не використовувалося, то ми його вертаємо
            return cityToCheck;
        }
    }
    public City findUnusedCity(List<City> citiesFromDB, List<City>usedCities) {
        LOGGER.info("Finding the unused city");
        if (citiesFromDB.size() == 0) {
            LOGGER.debug("There are no more known cities");
            return null;
        }
        City city = getRandomCity(citiesFromDB);
        citiesFromDB.remove(city);
        if (usedCities.stream().anyMatch(c -> c.getTitle().equals(city.getTitle()))) {//city.getTitle().equals(usedCities.listIterator().next().getTitle())
            LOGGER.debug(city.getTitle()+" is used");
            return findUnusedCity(citiesFromDB, usedCities);
        }
        usedCities.add(city);
        LOGGER.info("Bot found unused city");
        return city;
    }

    private City getRandomCity(List<City> citiesFromDB) {     // вертає рандомне місто зі списку доступних міст
        LOGGER.info("Bot chooses a random city");
        Random random = new Random();
        return citiesFromDB.get(random.nextInt(citiesFromDB.size()));
    }

    public String getLastLetter(User user) {/// This method returns the last letter of a city title
        LOGGER.info("Definition the last letter");
        if (user.getUsedCities().size() == 0) {
            LOGGER.debug("It's the start game, so there's no previous city name");
            return null;
        }
        String title = user.getUsedCities().get(user.getUsedCities().size() - 1).getTitle();
        String letter = title.substring(title.length() - 1);
        if (letter.equals("ь") || letter.equals("и")) {//якщо назва закінчується на "и" або
            LOGGER.debug("The previous city ends with 'ь' or 'и'");
            letter = title.substring(title.length() - 2, title.length() - 1);               //"ь", то береться передостання літера
        }
        return letter;
    }
}
