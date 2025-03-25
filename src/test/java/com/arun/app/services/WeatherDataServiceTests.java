package com.arun.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.arun.app.models.PincodeLocation;
import com.arun.app.models.WeatherData;
import com.arun.app.repositories.WeatherDataRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class WeatherDataServiceTests {

    @MockitoBean
    private WeatherDataRepo weatherDataRepo;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private PincodeLocationService pincodeLocationService;

    @Autowired
    private WeatherDataService weatherDataService;

    @MockitoBean
    private WeatherData weatherData;
    
    @Test
    public void testGetCurrentWeather_Success() throws Exception {
        String pincode = "12345";
        String jsonResponse = "{"
        	    + "\"current\":{"
        	    + "\"temperature_2m\":\"25.5\","
        	    + "\"wind_speed_10m\":\"5.0\","
        	    + "\"time\":\"2025-03-25\""
        	    + "},"
        	    + "\"hourly\":{"
        	    + "\"relative_humidity_2m\":["
        	    + "50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, "
        	    + "74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 96"
        	    + "]"
        	    + "}"
        	    + "}";
        PincodeLocation location = new PincodeLocation(); // Example latitude and longitude
        location.setLatitude(12.9716);
        location.setLongitude(77.5946);

        // Mocking the pincodeLocationService to return a valid location
        when(pincodeLocationService.getPincodeLocation(pincode)).thenReturn(location);

        // Mocking RestTemplate to return the expected JSON response
        when(restTemplate.getForObject(any(String.class), eq(String.class))).thenReturn(jsonResponse);

        // Expected behavior for jsonToWeatherData
        WeatherData expectedWeatherData = new WeatherData();
        expectedWeatherData.setLatitude(location.getLatitude());
        expectedWeatherData.setLongitude(location.getLongitude());
        expectedWeatherData.setTemperature(25.5);
        expectedWeatherData.setWindSpeed(5.0);
        ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
        expectedWeatherData.setHumidity(rootNode.get("hourly").get("relative_humidity_2m").get(Calendar.HOUR_OF_DAY-1).asInt());

        // Call the method
        WeatherData actualWeatherData = weatherDataService.getCurrentWeather(pincode);

        // Assertions to check that the values are correct
        assertNotNull(actualWeatherData);
        assertEquals(expectedWeatherData.getTemperature(), actualWeatherData.getTemperature());
        assertEquals(expectedWeatherData.getWindSpeed(), actualWeatherData.getWindSpeed());
        assertEquals(expectedWeatherData.getHumidity(), actualWeatherData.getHumidity());
        assertEquals(location.getLatitude(), actualWeatherData.getLatitude());
        assertEquals(location.getLongitude(), actualWeatherData.getLongitude());
    }

    @Test
    public void testGetCurrentWeather_HttpClientErrorException() throws JsonMappingException, JsonProcessingException {
        String pincode = "12345";
        PincodeLocation location = new PincodeLocation(); // Example latitude and longitude
        location.setLatitude(12.9716);
        location.setLongitude(77.5946);
        
        // Mocking the pincodeLocationService to return a valid location
        when(pincodeLocationService.getPincodeLocation(pincode)).thenReturn(location);

        // Mocking RestTemplate to throw HttpClientErrorException
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");
        when(restTemplate.getForObject(eq(toString()), eq(String.class))).thenThrow(exception);

        // Asserting that an exception is thrown when calling the method
        assertThrows(IllegalArgumentException.class, () -> weatherDataService.getCurrentWeather(pincode));
    }

    @Test
    public void testGetOldWeather_FromDb() throws Exception {
        String pincode = "12345";
        LocalDate date = LocalDate.of(2025, 3, 25);
        PincodeLocation location = new PincodeLocation(); // Example latitude and longitude
        location.setLatitude(12.9716);
        location.setLongitude(77.5946);
        WeatherData weatherDataFromDb = new WeatherData();
        weatherDataFromDb.setTemperature(22.5);

        // Mocking the pincodeLocationService to return a valid location
        when(pincodeLocationService.getPincodeLocation(pincode)).thenReturn(location);

        // Mocking weatherDataRepo to return existing weather data
        when(weatherDataRepo.findByLatitudeAndLongitudeAndDate(location.getLatitude(), location.getLongitude(), date))
                .thenReturn(weatherDataFromDb);

        // Calling the method
        WeatherData actualWeatherData = weatherDataService.getOldWeather(pincode, date);

        // Assertions to check that the data is fetched from the DB
        assertNotNull(actualWeatherData);
        assertEquals(weatherDataFromDb.getTemperature(), actualWeatherData.getTemperature());
    }

    @Test
    public void testCreateOldWeather() {
        // Mocking the repository's save method to return the same weatherData object
        when(weatherDataRepo.save(weatherData)).thenReturn(weatherData);

        // Calling the method
        WeatherData actualWeatherData = weatherDataService.createOldWeather(weatherData);

        // Verifying that the save method was called
        verify(weatherDataRepo, times(1)).save(weatherData);

        // Assertions to check that the returned object is the same as the input
        assertNotNull(actualWeatherData);
        assertEquals(weatherData, actualWeatherData);
    }

    @Test
    public void testJsonToWeatherData() throws Exception {
    	String jsonResponse = "{"
        	    + "\"current\":{"
        	    + "\"temperature_2m\":\"25.5\","
        	    + "\"wind_speed_10m\":\"5.0\","
        	    + "\"time\":\"2025-03-25\""
        	    + "},"
        	    + "\"hourly\":{"
        	    + "\"relative_humidity_2m\":["
        	    + "50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, "
        	    + "74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 96"
        	    + "]"
        	    + "}"
        	    + "}";
        // Call the method
        WeatherData actualWeatherData = weatherDataService.jsonToWeatherData(jsonResponse);
        ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Assertions to check that the values are parsed correctly
        assertNotNull(actualWeatherData);
        assertEquals(25.5, actualWeatherData.getTemperature());
        assertEquals(5.0, actualWeatherData.getWindSpeed());
        assertEquals(rootNode.get("hourly").get("relative_humidity_2m").get(Calendar.HOUR_OF_DAY-1).asInt()
        		, actualWeatherData.getHumidity());
    }

    public void testGetOldWeather_FromApi() throws JsonMappingException, JsonProcessingException, ParseException {
    	String pincode = "12345";
    	String jsonResponse = "{"
    		    + "\"hourly\": {"
    		    + "\"time\": ["
    		    + "\"2020-03-23T00:00\","
    		    + "\"2020-03-23T01:00\","
    		    + "\"2020-03-23T02:00\","
    		    + "\"2020-03-23T03:00\","
    		    + "\"2020-03-23T04:00\","
    		    + "\"2020-03-23T05:00\","
    		    + "\"2020-03-23T06:00\","
    		    + "\"2020-03-23T07:00\","
    		    + "\"2020-03-23T08:00\","
    		    + "\"2020-03-23T09:00\","
    		    + "\"2020-03-23T10:00\","
    		    + "\"2020-03-23T11:00\","
    		    + "\"2020-03-23T12:00\","
    		    + "\"2020-03-23T13:00\","
    		    + "\"2020-03-23T14:00\","
    		    + "\"2020-03-23T15:00\","
    		    + "\"2020-03-23T16:00\","
    		    + "\"2020-03-23T17:00\","
    		    + "\"2020-03-23T18:00\","
    		    + "\"2020-03-23T19:00\","
    		    + "\"2020-03-23T20:00\","
    		    + "\"2020-03-23T21:00\","
    		    + "\"2020-03-23T22:00\","
    		    + "\"2020-03-23T23:00\""
    		    + "],"
    		    + "\"temperature_2m\": ["
    		    + "23.9, 24.3, 25.6, 27.3, 28.5, 31.0, 33.0, 33.6, 33.4, 34.9, 33.6, 26.3, 31.5, 30.3, 29.0, 28.3, 27.8, 27.0, 26.1, 25.3, 24.7, 24.3, 23.8, 23.7"
    		    + "],"
    		    + "\"relative_humidity_2m\": ["
    		    + "94, 92, 82, 73, 68, 58, 47, 44, 43, 37, 38, 83, 45, 49, 58, 63, 67, 72, 81, 87, 89, 92, 95, 95"
    		    + "],"
    		    + "\"wind_speed_10m\": ["
    		    + "7.7, 3.8, 1.4, 3.1, 3.9, 3.7, 5.1, 7.6, 5.8, 5.5, 2.3, 14.7, 11.4, 10.9, 13.2, 12.0, 12.5, 10.3, 10.7, 13.1, 12.2, 10.7, 8.8, 5.5"
    		    + "]"
    		    + "}"
    		    + "}";

        PincodeLocation location = new PincodeLocation(); // Example latitude and longitude
        location.setLatitude(12.9716);
        location.setLongitude(77.5946);
        
     // Expected behavior for jsonToWeatherData
        ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
        WeatherData expectedWeatherData = new WeatherData();
        expectedWeatherData.setLatitude(location.getLatitude());
        expectedWeatherData.setLongitude(location.getLongitude());
        expectedWeatherData.setTemperature(rootNode.get("hourly").get("temperature_2m").get(Calendar.HOUR_OF_DAY-1).asDouble());
        expectedWeatherData.setWindSpeed(rootNode.get("hourly").get("wind_speed_10m").get(Calendar.HOUR_OF_DAY-1).asDouble());
        expectedWeatherData.setHumidity(rootNode.get("hourly").get("relative_humidity_2m").get(Calendar.HOUR_OF_DAY-1).asInt());

        
        when(weatherDataRepo.findByLatitudeAndLongitudeAndDate(
        		location.getLatitude(), location.getLongitude(), LocalDate.of(2020, 10, 10)))
        .thenReturn(null);
        
        when(restTemplate.getForObject(eq(toString()), eq(String.class))).thenReturn(jsonResponse);
        
     // Call the method
        WeatherData actualWeatherData = weatherDataService.getOldWeather(pincode, LocalDate.of(2020, 10, 10));

        // Assertions to check that the values are correct
        assertNotNull(actualWeatherData);
        assertEquals(expectedWeatherData.getTemperature(), actualWeatherData.getTemperature());
        assertEquals(expectedWeatherData.getWindSpeed(), actualWeatherData.getWindSpeed());
        assertEquals(expectedWeatherData.getHumidity(), actualWeatherData.getHumidity());
        assertEquals(location.getLatitude(), actualWeatherData.getLatitude());
        assertEquals(location.getLongitude(), actualWeatherData.getLongitude());
        
        
    }

    @Test
    public void testGetOldWeather_ClientErrorException() throws JsonMappingException, JsonProcessingException, ParseException {
        // Prepare test data
        String pincode = "123456";
        LocalDate date = LocalDate.of(2025, 3, 25);
        PincodeLocation location = new PincodeLocation();
        location.setLatitude(28.7041);
        location.setLongitude(77.1025);

        // Mock the behavior of the pincodeLocationService
        when(pincodeLocationService.getPincodeLocation(pincode)).thenReturn(location);
        
        when(weatherDataRepo.findByLatitudeAndLongitudeAndDate(
        		location.getLatitude(), location.getLongitude(), date))
        .thenReturn(null);

        // Prepare the mock response body (simulate the Bad Request response)
        String errorResponse = "{ \"error\": true, \"reason\": \"Invalid date\" }";

        // Create the HttpClientErrorException with 400 Bad Request and the mocked response body
        HttpClientErrorException exception = new HttpClientErrorException(
        		HttpStatus.BAD_REQUEST, "Bad Request", errorResponse.getBytes(), null);

        // Mock the RestTemplate to throw the exception
        when(restTemplate.getForObject(any(String.class), eq(String.class))).thenThrow(exception);
        try {
            // Call the method and assert that the exception is thrown with the expected message
            weatherDataService.getOldWeather(pincode, date);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
        	verify(pincodeLocationService, times(1)).getPincodeLocation(pincode);
        	verify(weatherDataRepo, times(1)).findByLatitudeAndLongitudeAndDate(location.getLatitude(), 
        			location.getLongitude(), date);
            verify(restTemplate, times(1)).getForObject(any(String.class), eq(String.class));
            // Assert that the exception message contains the expected error message from the response
            assertEquals("Invalid date", e.getMessage());
        }
    }
}
