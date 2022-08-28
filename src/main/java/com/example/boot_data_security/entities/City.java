package com.example.boot_data_security.entities;

import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.*;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "country")
    private String country;

    private String filename;

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public City() {
    }

    public City(String title) {
        this.title = title;
    }

    public City(String title, String country) {
        this.title = title;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getFilename() {
        return filename;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", country='" + country + '\'' +
                '}';
    }


}
