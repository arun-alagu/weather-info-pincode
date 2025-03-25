package com.arun.app.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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
	private static final String STRING_KEY_PREFIX = "pincode:";
	private final PincodeLocationRepo pincodeLocationRepo;
	private final RestTemplate restTemplate;
	private final RedisTemplate<String, PincodeLocation> pincodeRedis;
	
	public PincodeLocationService(PincodeLocationRepo pincodeLocationRepo, RestTemplate restTemplate,
			RedisTemplate<String, PincodeLocation> pincodeRedis) {
		this.pincodeLocationRepo = pincodeLocationRepo;
		this.restTemplate = restTemplate;
		pincodeRedis.setKeySerializer(new StringRedisSerializer());
	    pincodeRedis.setValueSerializer(new Jackson2JsonRedisSerializer<>(PincodeLocation.class));
		this.pincodeRedis = pincodeRedis;
	}
	
	@Override
	public PincodeLocation getPincodeLocation(String pincode) throws JsonMappingException, JsonProcessingException {
		String redisKey = STRING_KEY_PREFIX + pincode;
		PincodeLocation redisPincodeLocation = pincodeRedis.opsForValue().get(redisKey);
		if(redisPincodeLocation != null)
			return redisPincodeLocation;
		
		PincodeLocation pincodeLocationRes =  getPincodeLocationFromDb(pincode);
		if(pincodeLocationRes != null) {
			pincodeRedis.opsForValue().set(redisKey, pincodeLocationRes);
			return pincodeLocationRes;
		}
		
		pincodeLocationRes = getPincodeLocationUsingAPI(pincode);
		pincodeRedis.opsForValue().set(redisKey, pincodeLocationRes);
		return pincodeLocationRes;
		
	}
	
	public PincodeLocation getPincodeLocationFromDb(String pincode) {
		return pincodeLocationRepo.findByPincode(pincode);
	}
	
	@Value("${GEO_CODING_API_URL}")
	private String geoCodingUrl;
	@Value("${GEO_CODING_API_KEY}")
	private String apiKey;
	public PincodeLocation getPincodeLocationUsingAPI(String pincode) throws JsonMappingException, JsonProcessingException {
		String jsonResponse = null;
		try {
		 jsonResponse = restTemplate.getForObject(
				geoCodingUrl+"?zip="+pincode+",IN&appid="+apiKey
				,String.class);
		} catch(HttpClientErrorException  e) {
			 ObjectMapper objectMapper = new ObjectMapper();
           JsonNode jsonNode = objectMapper.readTree(e.getResponseBodyAsString());
           Integer errorCode = jsonNode.path("cod").asInt();
           String errorMessage = jsonNode.path("message").asText();
           if(errorCode == 404)
        	   throw new IllegalArgumentException("Pincode: "+pincode+" "+errorMessage);
           else if(errorCode == 401)
        	   throw new IllegalArgumentException("Invalid API key");
           else
        	   throw new IllegalArgumentException(errorMessage);
		}
		
		if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new IllegalArgumentException("No response received for pincode: " + pincode);
        }
		
		return jsonToPincodeLocation(jsonResponse);
	}
	
	@Override
	public PincodeLocation createPincodeLocation(PincodeLocation pincodeLocation) throws JsonMappingException, JsonProcessingException {
		return pincodeLocationRepo.save(pincodeLocation);
	}
	
	@Override
	public PincodeLocation updatePincodeLocation(PincodeLocation pincodeLocation) throws JsonMappingException, JsonProcessingException {
		PincodeLocation pincodeLocation2 = getPincodeLocationFromDb(pincodeLocation.getPincode());
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
