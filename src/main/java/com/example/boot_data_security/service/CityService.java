package com.example.boot_data_security.service;

import com.example.boot_data_security.entities.City;

import com.example.boot_data_security.repos.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CityService {
    @Autowired
    private CityRepository cityRepository;
    private final List<City> usedCities;

    {
        usedCities = new ArrayList<>();
    }

    public List<City> getUsedCities() {
        return usedCities;
    }

    public void makeUsedCities(City city) {
        usedCities.add(city);
    }

    public Iterable<City> findAll(){
        return cityRepository.findAll();
    }

    public City findByTitle(String title){
       return cityRepository.findByTitle(title);
    }

    public City findById(Integer id){
        return cityRepository.findById(id).get();
    }

    public void save(City city) {
        if (cityRepository.getCity(city.getTitle()) == null) {
            cityRepository.save(city);
        }
    }
    public City bot() {
        String letter = getLastLetter();     //дістаємо останню літеру назви міста
        List<City> citiesFromDB = cityRepository.searchByLetterStartWith(letter);  //шукаємо в базі міста на потрібну літеру
        if (citiesFromDB.size() == 0) {
            return null;      //якщо такого міста нема в базі, то вертаємо нал, який означатиме, що користувач виграв
        } else {
            City city = getRandomCity(citiesFromDB);  //рандомно зі списку міст, які починаються на одну і ту ж літеру вибираємо місто
            return checksAndReturnsUnusedCity(citiesFromDB, usedCities, city); // і вертаємо його
        }
    }
    private City checksAndReturnsUnusedCity(List<City> citiesFromDB, List<City> usedCities, City cityToCheck) {
        if (usedCities.stream().anyMatch(c -> c.getTitle().equals(cityToCheck.getTitle()))) {//якшо вибране рандомно місто є в списку
            citiesFromDB.remove(cityToCheck);
            City anotherCity = findUnusedCity(citiesFromDB, usedCities);  // використаних місто, то шукаємо інше
            makeUsedCities(anotherCity);  //місто зі списку не використаних
            return anotherCity;
        } else {
            makeUsedCities(cityToCheck); // якшо таке місто ше не використовувалося, то ми його вертаємо
            return cityToCheck;
        }
    }
    public City findUnusedCity(List<City> citiesFromDB, List<City>usedCities) {
        if (citiesFromDB.size() == 0) {
            return null;
        }
        City city = getRandomCity(citiesFromDB);
        citiesFromDB.remove(city);
        if (usedCities.stream().anyMatch(c -> c.getTitle().equals(city.getTitle()))) {                                                                    //city.getTitle().equals(usedCities.listIterator().next().getTitle())
            return findUnusedCity(citiesFromDB, usedCities);
        }
        usedCities.add(city);
        return city;
    }

    private City getRandomCity(List<City> citiesFromDB) {     // вертає рандомне місто зі списку доступних міст
        Random random = new Random();
        return citiesFromDB.get(random.nextInt(citiesFromDB.size()));
    }

    public String getLastLetter() {/// This method returns the last letter of a city title
        if (usedCities.size() == 0) {
            return null;
        }
        String title = usedCities.get(usedCities.size() - 1).getTitle();
        String letter = title.substring(title.length() - 1);
        if (letter.equals("ь") || letter.equals("и")) {                                     //якщо назва закінчується на "и" або
            letter = title.substring(title.length() - 2, title.length() - 1);               //"ь", то береться передостання літера
        }
        return letter;
    }
}
