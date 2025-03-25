package com.arun.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.arun.app.models.PincodeLocation;
import com.arun.app.repositories.PincodeLocationRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


@SpringBootTest
public class PincodeLocationServiceTests {
	
	@MockitoBean
	private PincodeLocationRepo pincodeLocationRepo;
	
	@MockitoBean
	private RestTemplate restTemplate;
	
	@Value("${GEO_CODING_API_URL}")
	private String geoCodingUrl = "https://test.com";
	@Value("${GEO_CODING_API_KEY}")
	private String apiKey = "test_api";
	
	@Autowired
	private PincodeLocationService pincodeLocationService;
	
	 @Test
	    void testGetPincodeLocationFromDb() throws JsonMappingException, JsonProcessingException {
	        String pincode = "110001";
	        PincodeLocation expectedLocation = new PincodeLocation();
	        expectedLocation.setPincode(pincode);
	        expectedLocation.setName("Connaught Place");
	        expectedLocation.setLatitude(28.6342);
	        expectedLocation.setLongitude(77.2176);
	        expectedLocation.setCountry("India");

	        when(pincodeLocationRepo.findByPincode(pincode)).thenReturn(expectedLocation);

	        PincodeLocation result = pincodeLocationService.getPincodeLocation(pincode);

	        assertNotNull(result);
	        assertEquals(expectedLocation.getPincode(), result.getPincode());
	        assertEquals(expectedLocation.getName(), result.getName());
	    }

	 @Test
	 	void testGetPincodeLocationFromAPI() throws JsonMappingException, JsonProcessingException {
	     String pincode = "110001";
	     // Correcting the mock response to a valid JSON string.
	     String jsonResponse = "{\"zip\":\"110001\",\"name\":\"Connaught Place\",\"lat\":\"28.6342\",\"lon\":\"77.2176\",\"country\":\"India\"}";
	     PincodeLocation expectedPincodeLocation = new PincodeLocation();
	     expectedPincodeLocation.setPincode("110001");
	     expectedPincodeLocation.setName("Connaught Place");
	     expectedPincodeLocation.setLatitude(28.6342);
	     expectedPincodeLocation.setLongitude(77.2176);
	     expectedPincodeLocation.setCountry("India");

	     // Mock the save behavior of the PincodeLocationRepo to return the expected PincodeLocation.
	     when(pincodeLocationRepo.save(any(PincodeLocation.class))).thenReturn(expectedPincodeLocation);

	     // Ensure the repo does not return a cached value for this pincode.
	     when(pincodeLocationRepo.findByPincode(pincode)).thenReturn(null);

	     // Mock the restTemplate to return the valid JSON response.
	     when(restTemplate.getForObject(eq(geoCodingUrl+"?zip="+pincode+",IN&appid="+apiKey), eq(String.class))).thenReturn(jsonResponse);

	     // Calling the method under test.
	     PincodeLocation result = pincodeLocationService.getPincodeLocation(pincode);

	     // Assertions to verify the result.
	     assertNotNull(result);
	     assertEquals("Connaught Place", result.getName());
	     assertEquals("India", result.getCountry());
	     assertEquals("110001", result.getPincode());
	     assertEquals(28.6342, result.getLatitude());
	     assertEquals(77.2176, result.getLongitude());
	 }

	    @Test
	    void testGetPincodeLocationAPIEmptyResponse() {
	        String pincode = "110001";

	        when(pincodeLocationRepo.findByPincode(pincode)).thenReturn(null);
	        when(restTemplate.getForObject(eq(geoCodingUrl+"?zip="+pincode+",IN&appid="+apiKey), eq(String.class))).thenReturn(null);

	        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
	            pincodeLocationService.getPincodeLocationUsingAPI(pincode);
	        });

	        assertEquals("No response received for pincode: " + pincode, exception.getMessage());
	    }

	    @Test
	    void testGetPincodeLocationAPIHttpClientErrorException() throws JsonProcessingException {
	        String pincode = "110001";
	        HttpClientErrorException exception = mock(HttpClientErrorException.class);
	        when(exception.getResponseBodyAsString()).thenReturn(
	        		"{"
	        		+"\"cod\": 401,"
	        		+ "\"message\":\"Invalid API key\""
	        		+ "}");

	        when(pincodeLocationRepo.findByPincode(pincode)).thenReturn(null);
	        when(restTemplate.getForObject(eq(geoCodingUrl+"?zip="+pincode+",IN&appid="+apiKey), eq(String.class))).thenThrow(exception);

	        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
	            pincodeLocationService.getPincodeLocationUsingAPI(pincode);
	        });

	        assertEquals("Invalid API key", thrownException.getMessage());
	    }

	    @Test
	    void testCreatePincodeLocation() throws JsonMappingException, JsonProcessingException {
	        PincodeLocation pincodeLocation = new PincodeLocation();
	        pincodeLocation.setPincode("110001");
	        pincodeLocation.setName("Connaught Place");
	        pincodeLocation.setLatitude(28.6342);
	        pincodeLocation.setLongitude(77.2176);
	        pincodeLocation.setCountry("India");

	        when(pincodeLocationRepo.save(pincodeLocation)).thenReturn(pincodeLocation);

	        PincodeLocation result = pincodeLocationService.createPincodeLocation(pincodeLocation);

	        assertNotNull(result);
	        assertEquals(pincodeLocation.getPincode(), result.getPincode());
	        assertEquals(pincodeLocation.getName(), result.getName());
	    }

	    @Test
	    void testUpdatePincodeLocationWithExistingData() throws JsonMappingException, JsonProcessingException {
	        PincodeLocation existingLocation = new PincodeLocation();
	        existingLocation.setPincode("110001");
	        existingLocation.setName("Connaught Place");
	        existingLocation.setLatitude(28.6342);
	        existingLocation.setLongitude(77.2176);
	        existingLocation.setCountry("India");

	        PincodeLocation updatedLocation = new PincodeLocation();
	        updatedLocation.setPincode("110001");
	        updatedLocation.setName("New Connaught Place");
	        updatedLocation.setLatitude(28.6342);
	        updatedLocation.setLongitude(77.2176);
	        updatedLocation.setCountry("India");

	        when(pincodeLocationRepo.findByPincode("110001")).thenReturn(existingLocation);
	        when(pincodeLocationRepo.save(existingLocation)).thenReturn(existingLocation);

	        PincodeLocation result = pincodeLocationService.updatePincodeLocation(updatedLocation);

	        assertNotNull(result);
	        assertEquals(updatedLocation.getName(), result.getName());
	    }

	    @Test
	    void testUpdatePincodeLocationWithNoExistingData() throws JsonMappingException, JsonProcessingException {
	        PincodeLocation newLocation = new PincodeLocation();
	        newLocation.setPincode("110001");
	        newLocation.setName("New Connaught Place");
	        newLocation.setLatitude(28.6342);
	        newLocation.setLongitude(77.2176);
	        newLocation.setCountry("IN");

	        when(pincodeLocationRepo.findByPincode("110001")).thenReturn(null);
	        when(pincodeLocationRepo.save(newLocation)).thenReturn(newLocation);

	        PincodeLocation result = pincodeLocationService.updatePincodeLocation(newLocation);

	        assertNotNull(result);
	        assertEquals(newLocation.getPincode(), result.getPincode());
	    }

	    @Test
	    void testJsonToPincodeLocation() throws JsonMappingException, JsonProcessingException {
	        String jsonResponse = "{\"zip\":\"110001\",\"name\":\"Connaught Place\",\"lat\":\"28.6342\",\"lon\":\"77.2176\",\"country\":\"India\"}";
	        PincodeLocation expectedPincodeLocation = new PincodeLocation();
	        expectedPincodeLocation.setPincode("110001");
	        expectedPincodeLocation.setName("Connaught Place");
	        expectedPincodeLocation.setLatitude(28.6342);
	        expectedPincodeLocation.setLongitude(77.2176);
	        expectedPincodeLocation.setCountry("India");

	        // Mock the save behavior of the PincodeLocationRepo to return the expected PincodeLocation.
	        when(pincodeLocationRepo.save(any(PincodeLocation.class))).thenReturn(expectedPincodeLocation);

	        
	        PincodeLocation result = pincodeLocationService.jsonToPincodeLocation(jsonResponse);

	        assertNotNull(result);
	        assertEquals("110001", result.getPincode());
	        assertEquals("Connaught Place", result.getName());
	        assertEquals(28.6342, result.getLatitude());
	        assertEquals(77.2176, result.getLongitude());
	        assertEquals("India", result.getCountry());
	    }
	}