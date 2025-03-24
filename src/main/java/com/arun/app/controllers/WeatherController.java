package com.arun.app.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arun.app.dtos.WeatherDataDto;
import com.arun.app.models.WeatherData;
import com.arun.app.services.WeatherDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
public class WeatherController {
	private final WeatherDataService weatherDataService;

	public WeatherController(WeatherDataService weatherDataService) {
		this.weatherDataService = weatherDataService;
	}

	@GetMapping("/weather")
	public ResponseEntity<WeatherDataDto> getWeather(
			@RequestParam String pincode,
			@RequestParam(name = "for_date") String date) 
					throws ParseException, JsonMappingException, JsonProcessingException {
		try {
		SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd");
		gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		LocalDate givenDate = LocalDate.parse(date);
		
		Date gmtDate = gmtFormat.parse(date);
		Date currGmtDate = gmtFormat.parse(gmtFormat.format(new Date()));
		WeatherDataDto dto = null;
		if(gmtDate.before(currGmtDate)) {
			WeatherData weatherData = weatherDataService.getOldWeather(pincode, 
					gmtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			return ResponseEntity.ok(WeatherDataDto.get(weatherData, pincode));
		}
		else if(gmtDate.after(currGmtDate))
			throw new IllegalArgumentException("Enter current date or previous date");
		}
		catch (DateTimeParseException e) {
			throw new DateTimeParseException("Invalid Date: "+date, date, 0);
		}
		
		WeatherData weatherData = weatherDataService.getCurrentWeather(pincode);
		return ResponseEntity.ok(WeatherDataDto.get(weatherData, pincode));
	}
}
