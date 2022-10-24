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
import org.springframework.ui.Model;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Service
public class CityService {
    private static final Logger LOGGER = LogManager.getLogger(CityService.class);
    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserService userService;

    public Page<City> findAll(Integer page) {
        return cityRepository.findAll(PageRequest.of(page, 17, Sort.by("title")));
    }

    public City findByTitle(String title) {
        return cityRepository.findByTitle(title);
    }

    public City findById(Long id) {
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
            LOGGER.debug(cityToCheck.getTitle() + " is used");
            citiesFromDB.remove(cityToCheck);
            City anotherCity = findUnusedCity(citiesFromDB, user.getUsedCities());  // використаних місто, то шукаємо інше
            userService.makeUsedCity(user, anotherCity);  //місто зі списку не використаних
            return anotherCity;
        } else {
            LOGGER.info(cityToCheck.getTitle() + " is unused");
            userService.makeUsedCity(user, cityToCheck); // якшо таке місто ше не використовувалося, то ми його вертаємо
            return cityToCheck;
        }
    }

    public City findUnusedCity(List<City> citiesFromDB, List<City> usedCities) {
        LOGGER.info("Finding the unused city");
        if (citiesFromDB.size() == 0) {
            LOGGER.debug("There are no more known cities");
            return null;
        }
        City city = getRandomCity(citiesFromDB);
        citiesFromDB.remove(city);
        if (usedCities.stream().anyMatch(c -> c.getTitle().equals(city.getTitle()))) {//city.getTitle().equals(usedCities.listIterator().next().getTitle())
            LOGGER.debug(city.getTitle() + " is used");
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

    public boolean checkIfCityExists(String city, String whereToLookFor) {
        LOGGER.info("Checking if city exists");
        boolean flag = false;
        StringBuilder title = new StringBuilder(city);
        if (title.indexOf(" ") > 0) {
            LOGGER.debug("Name of city has \" \"");
            ifTitleHasSpace(title);
        }
        String url = whereToLookFor + title;
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String[] keyWords = {"Місто", "Село", "Селище міського типу", "Населений пункт", "місто", "село", "селище міського типу", "населений пункт"};
            flag = lookingForKeyWordInResponseBody(keyWords, in);
        } catch (IOException exception) {
            System.out.println("Сторінка не існує");
        }
        if (!flag && url.contains("https://uk.wikipedia.org/wiki/")) {
            LOGGER.debug("Looking for in wiki");
            flag = checkIfCityExists(city, "https://www.google.com/maps/place/");
        }
        if (!flag && !city.contains("_(місто або село)")) {
            flag = checkIfCityExists(city + "_(місто або село)", whereToLookFor);
        }
        return flag;
    }

        private boolean lookingForKeyWordInResponseBody (String[]keyWords, BufferedReader in){
        LOGGER.info("Looking for key word in response body");
            String inputLine;
            boolean flag = false;
            try {
                while ((inputLine = in.readLine()) != null) {
                    for (String keyWord : keyWords) {
                        if (inputLine.contains(keyWord)) {
                            flag = true;
                            break;
                        }
                    }
                }
            } catch (IOException exception) {
                System.out.println("Сторінка не існує");
            }
            return flag;
        }

        private void ifTitleHasSpace (StringBuilder title){
            int position = title.indexOf(" ");
            title.deleteCharAt(position);
            title.insert(position, "_");
        }

        public String checkingIfCityMatches (User user, Model model, @Valid City city){
            if (city.getTitle().startsWith(getLastLetter(user).toUpperCase())) {
                return checkIfCityIsUnused(user, model, city);
            } else {
                LOGGER.debug(city.getTitle() + " does not start with " + getLastLetter(user));
                City botsCity = user.getUsedCities().get(user.getUsedCities().size() - 1);
                model.addAttribute("botsCity", botsCity.getTitle());
                return "tryagain";
            }
        }

        public String checkIfCityIsUnused (User user, Model model, City city){
            if (user.getUsedCities().stream().anyMatch(c -> c.getTitle().equals(city.getTitle()))) {
                LOGGER.debug(city.getTitle() + " is used");
                City botsCity = user.getUsedCities().get(user.getUsedCities().size() - 1);
                model.addAttribute("botsCity", botsCity.getTitle());
                return "tryagain";
            } else {

                LOGGER.info(city.getTitle() + " is not used");
                userService.makeUsedCity(user, city);
                save(city);
                return "redirect:/play";
            }
        }

    }
