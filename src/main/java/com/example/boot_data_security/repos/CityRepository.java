package com.example.boot_data_security.repos;

import com.example.boot_data_security.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface CityRepository extends JpaRepository<City, Long> {
    @Query(value = "select * from cities  WHERE title like ?%", nativeQuery = true)
    List<City> searchByLetterStartWith(String letter);

   @Query(value = "select * from cities where title like ?",nativeQuery = true)
    City getCity(String title);

   City findByTitle(String title);
   Optional<City> findById(Integer id);
}
