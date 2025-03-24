package com.arun.app.services;

import java.time.LocalDate;
import java.util.Date;

import com.arun.app.models.PincodeLocation;
import com.arun.app.models.WeatherData;

public interface IWeatherDataService {
	WeatherData getCurrentWeather(String pincode) throws Exception;
	WeatherData getOldWeather(String pincode, LocalDate date) throws Exception;
	WeatherData createOldWeather(WeatherData weatherData);
	WeatherData jsonToWeatherData(String jsonResponse) throws Exception;
	
}
