package com.arun.app.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.arun.app.models.PincodeLocation;
import com.arun.app.repositories.PincodeLocationRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PincodeLocationService implements IPincodeLocationService{
	private final PincodeLocationRepo pincodeLocationRepo;
	private final RestTemplate restTemplate;
	
	public PincodeLocationService(PincodeLocationRepo pincodeLocationRepo, RestTemplate restTemplate) {
		this.pincodeLocationRepo = pincodeLocationRepo;
		this.restTemplate = restTemplate;
	}
	
	@Value("${GEO_CODING_API_URL}")
	private String geoCodingUrl;
	@Value("${GEO_CODING_API_KEY}")
	private String apiKey;
	@Override
	public PincodeLocation getPincodeLocation(String pincode) throws JsonMappingException, JsonProcessingException {
		
		PincodeLocation pincodeLocationFromDb =  pincodeLocationRepo.findByPincode(pincode);
		if(pincodeLocationFromDb != null) return pincodeLocationFromDb;
		
		String jsonResponse = null;
		try {
		 jsonResponse = restTemplate.getForObject(
				geoCodingUrl+"?zip="+pincode+",IN&appid="+apiKey
				,String.class);
		}catch(HttpClientErrorException  e) {
			 ObjectMapper objectMapper = new ObjectMapper();
           JsonNode jsonNode = objectMapper.readTree(e.getResponseBodyAsString());
           String errorMessage = jsonNode.path("message").asText();
//			System.out.println(jsonResponse.isEmpty());
			throw new IllegalArgumentException("Pincode: "+pincode+" "+errorMessage);
		}
		
		
//		logger.debug(object.toString());
//		System.out.println(pincodeLocation.getPincode());
//		System.out.println(pincodeLocation.getName());
//		System.out.println(pincodeLocation.getLatitide());
//		System.out.println(pincodeLocation.getLongitude());
//		System.out.println(pincodeLocation.getCountry());
		return jsonToPincodeLocation(jsonResponse);
	}
	
	@Override
	public PincodeLocation createPincodeLocation(PincodeLocation pincodeLocation) throws JsonMappingException, JsonProcessingException {
		return pincodeLocationRepo.save(pincodeLocation);
	}
	
	@Override
	public PincodeLocation updatePincodeLocation(PincodeLocation pincodeLocation) throws JsonMappingException, JsonProcessingException {
		PincodeLocation pincodeLocation2 = getPincodeLocation(pincodeLocation.getPincode());
		if(pincodeLocation2 == null) return createPincodeLocation(pincodeLocation);
		
		 Optional.ofNullable(pincodeLocation.getLatitude()).ifPresent(pincodeLocation2::setLatitude);
		 Optional.ofNullable(pincodeLocation.getLongitude()).ifPresent(pincodeLocation2::setLatitude);
		 Optional.ofNullable(pincodeLocation.getName()).ifPresent(pincodeLocation2::setName);
		 Optional.ofNullable(pincodeLocation.getCountry()).ifPresent(pincodeLocation2::setCountry);
		 
		 return pincodeLocationRepo.save(pincodeLocation2);
		 
	}
	
	@Override
	public PincodeLocation jsonToPincodeLocation(String jsonResponse) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		
		System.out.println(rootNode.get("lat").asText());
		System.out.println(rootNode.get("lon").asText());
		
		PincodeLocation pincodeLocation = new PincodeLocation();
		pincodeLocation.setPincode(rootNode.get("zip").asText());
		pincodeLocation.setName(rootNode.get("name").asText());
		pincodeLocation.setLatitude(Double.valueOf(rootNode.get("lat").asText()));
		pincodeLocation.setLongitude(Double.valueOf(rootNode.get("lon").asText()));
		pincodeLocation.setCountry(rootNode.get("country").asText());
		
		return createPincodeLocation(pincodeLocation);
	}
}
