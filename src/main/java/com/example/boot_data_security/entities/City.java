package com.example.boot_data_security.entities;


import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(id, city.id) && Objects.equals(title, city.title) && Objects.equals(country, city.country) && Objects.equals(filename, city.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, country, filename);
    }
}
