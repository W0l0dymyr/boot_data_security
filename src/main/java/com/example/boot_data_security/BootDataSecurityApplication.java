package com.example.boot_data_security;

import com.example.boot_data_security.entities.City;
import com.example.boot_data_security.repos.CityRepository;
import com.example.boot_data_security.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class BootDataSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootDataSecurityApplication.class, args);
	}
//public static void main(String[] args) {
//	CityService cityService = new CityService();
//	cityService.test();
//}

}
