package com.arun.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arun.app.dtos.WeatherDataDto;
import com.arun.app.models.WeatherData;
import com.arun.app.services.WeatherDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SpringBootTest
public class WeatherControllerTests {
	@MockitoBean
    private WeatherDataService weatherDataService;
	
    @Autowired
    private WeatherController weatherController;
    
    @Test
    void testGetWeatherForFutureDate() {
        // Setup data for future date
        String pincode = "12345";
        String futureDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() + 86400000L)); // 1 day ahead of current date
        
        // Call the method and expect IllegalArgumentException
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            weatherController.getWeather(pincode, futureDate);
        });

        // Assertions
        assertEquals("Enter current date or previous date", ex.getMessage());
    }

  
    @Test
    void testGetWeatherForCurrentDate() throws Exception {
        // Setup mock data
        String pincode = "12345";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // Current date in ISO format

        // Mock a valid WeatherData object
        WeatherData mockWeatherData = new WeatherData();
        mockWeatherData.setLatitude(28.7041);  // Mock latitude value
        mockWeatherData.setLongitude(77.1025); // Mock longitude value
        mockWeatherData.setTemperature(25.00); // Mock temperature value

        when(weatherDataService.getCurrentWeather(pincode)).thenReturn(mockWeatherData);

        ResponseEntity<WeatherDataDto> response = weatherController.getWeather(pincode, date);
        
        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockWeatherData.getLatitude(), response.getBody().getLatitude());
        assertEquals(mockWeatherData.getLongitude(), response.getBody().getLongitude());
        assertEquals(mockWeatherData.getTemperature(), response.getBody().getTemperature());

        verify(weatherDataService, times(1)).getCurrentWeather(pincode);
    }
    
    @Test
    void testGetWeatherForInvalidDateFormat() {
        // Setup data for invalid date format
        String pincode = "12345";
        String invalidDate = "202-03-30"; // Invalid date format
        
        // Call the method and expect ParseException
        Exception ex = assertThrows(DateTimeParseException.class, () -> {
            weatherController.getWeather(pincode, invalidDate);
        });

        // Assertions
        assertEquals(ex.getMessage(),"Invalid Date: "+invalidDate);
    }
    
    @Test
    void testGetWeatherForBeforeDate() throws JsonMappingException, JsonProcessingException, ParseException {
    	String pincode = "123456";
    	String date = "2020-10-10"; 
    	
    	WeatherData mockWeatherData = new WeatherData();
        mockWeatherData.setLatitude(28.7041);  // Mock latitude value
        mockWeatherData.setLongitude(77.1025); // Mock longitude value
        mockWeatherData.setTemperature(25.00); // Mock temperature value
        
		when(weatherDataService.getOldWeather(pincode, LocalDate.parse(date))).thenReturn(mockWeatherData);
		
		ResponseEntity<WeatherDataDto> response = weatherController.getWeather(pincode, date);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(mockWeatherData.getLatitude(), response.getBody().getLatitude());
        assertEquals(mockWeatherData.getLongitude(), response.getBody().getLongitude());
        assertEquals(mockWeatherData.getTemperature(), response.getBody().getTemperature());

        verify(weatherDataService, times(1)).getOldWeather(pincode,  LocalDate.parse(date));
		
    }
}
