package com.arun.app.dtos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import com.arun.app.models.WeatherData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherDataDto implements Serializable {
	private static final String TEMPERATURE_UNIT = "°C";
    private static final String HUMIDITY_UNIT = "%";
    private static final String WINDSPEED_UNIT = "km/h";
	private String pincode;
	private Double latitude;
	private Double longitude;
	private LocalDate date;
	private Double temperature;
	private Integer humidity;
	private Double windSpeed;
	private transient String temperatureUnit;
	private transient String humidityUnit;
	private transient String windSpeedUnit;

	public WeatherDataDto() {
		this.temperatureUnit = TEMPERATURE_UNIT;
		this.humidityUnit = HUMIDITY_UNIT;
		this.windSpeedUnit = WINDSPEED_UNIT;
	}
	
    
    public static WeatherDataDto get(WeatherData weatherData, String pincode) {
    	if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
    	WeatherDataDto dto = new WeatherDataDto();
    	dto.setPincode(pincode);
    	dto.setLatitude(weatherData.getLatitude());
    	dto.setLongitude(weatherData.getLongitude());
    	dto.setDate(weatherData.getDate());
    	dto.setTemperature(weatherData.getTemperature());
    	dto.setHumidity(weatherData.getHumidity());
    	dto.setWindSpeed(weatherData.getWindSpeed());
    	return dto;
    }
    public static WeatherData getWeatherData (WeatherDataDto dto) {
    	if (dto == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
    	WeatherData weatherData = new WeatherData();
    	weatherData.setLatitude(dto.getLatitude());
    	weatherData.setLongitude(dto.getLongitude());
    	weatherData.setDate(dto.getDate());
    	weatherData.setTemperature(dto.getTemperature());
    	weatherData.setHumidity(dto.getHumidity());
    	weatherData.setWindSpeed(dto.getWindSpeed());
    	return weatherData;
    }
    
    
}


