package com.arun.app.repositories;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arun.app.models.WeatherData;

@Repository
public interface WeatherDataRepo extends JpaRepository<WeatherData, Long>{
	WeatherData findByLatitudeAndLongitudeAndDate(Double latitude, Double longitude, LocalDate date);
}
