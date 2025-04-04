package com.arun.app.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.arun.app.dtos.WeatherDataDto;
import com.arun.app.models.PincodeLocation;
import com.arun.app.models.WeatherData;
import com.arun.app.repositories.WeatherDataRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherDataService implements IWeatherDataService{
	private final WeatherDataRepo weatherDataRepo;
	private final RestTemplate restTemplate;
	private final PincodeLocationService pincodeLocationService;
	private final RedisTemplate<String, WeatherDataDto> weatherRedis;
	private final RedisTemplate<String, WeatherData> weatherRedis2;
	private static final String STRING_KEY_PREFIX_C = "current-weather:";
	private static final String STRING_KEY_PREFIX_O = "old-weather:";
	
	Logger logger = LoggerFactory.getLogger(WeatherDataService.class);

	public WeatherDataService(WeatherDataRepo weatherDataRepo, RestTemplate restTemplate,
			PincodeLocationService pincodeLocationService, 
			RedisTemplate<String, WeatherDataDto> weatherRedis, RedisTemplate<String, WeatherData> weatherRedis2) {
		this.weatherDataRepo = weatherDataRepo;
		this.restTemplate = restTemplate;
		this.pincodeLocationService = pincodeLocationService;
		this.weatherRedis = weatherRedis;
		this.weatherRedis2 = weatherRedis2;
	}
	
	@Value("${CURRENT_WEATHER_API_URL}")
	private String currentWeatherUrl;
	@Override
	public WeatherData getCurrentWeather(String pincode) throws JsonMappingException, JsonProcessingException, ParseException {
		
		PincodeLocation pincodeLocation = pincodeLocationService.getPincodeLocation(pincode);
		String redisKey = STRING_KEY_PREFIX_C+pincodeLocation.getLatitude()+":"
				+pincodeLocation.getLongitude()+":"+LocalDate.now();
		
		WeatherDataDto redisWeatherData = weatherRedis.opsForValue().get(redisKey);
		if(redisWeatherData != null) return WeatherDataDto.getWeatherData(redisWeatherData);
		
		String jsonResponse = null;
		try {
		jsonResponse = restTemplate.getForObject(
				currentWeatherUrl+"?latitude="+pincodeLocation.getLatitude()+
				"&longitude="+pincodeLocation.getLongitude()+"&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m&forecast_days=1"
				,String.class);
		}catch(HttpClientErrorException  e) {
			 ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(e.getResponseBodyAsString());
//            String errorMessage = jsonNode.path("reason").asText();
//			System.out.println(jsonResponse.isEmpty());
			throw new IllegalArgumentException(e.getResponseBodyAsString());
		}
		
		if(jsonResponse == null) {
			throw new IllegalArgumentException("No response received for \"lat:"+pincodeLocation.getLatitude()+
					", lon:"+pincodeLocation.getLongitude()+", date:"+ LocalDate.now()+"\"");
		}
		
		WeatherData weatherData =  jsonToWeatherData(jsonResponse);
		weatherData.setLatitude(pincodeLocation.getLatitude());
		weatherData.setLongitude(pincodeLocation.getLongitude());
		
		setWithExpiration(redisKey, WeatherDataDto.get(weatherData, pincode), 5, TimeUnit.MINUTES);
		return weatherData;
	}
	
	private void setWithExpiration(String key, WeatherDataDto value, long timeout, TimeUnit unit) {
        ValueOperations<String, WeatherDataDto> ops = weatherRedis.opsForValue();
        ops.set(key, value, timeout, unit);
    }
	
	@Value("${OLD_WEATHER_API_URL}")
	private String oldWeatherUrl;
	@Override
	public WeatherData getOldWeather(String pincode, LocalDate date) throws JsonMappingException, JsonProcessingException, ParseException {
		
		PincodeLocation pincodeLocation = pincodeLocationService.getPincodeLocation(pincode);
		String redisKey = STRING_KEY_PREFIX_O+pincodeLocation.getLatitude()+":"
				+pincodeLocation.getLongitude()+":"+date;
		
		WeatherData redisWeatherData = weatherRedis2.opsForValue().get(redisKey);
		if(redisWeatherData != null) return redisWeatherData;
		
		WeatherData weatherDataFromDb = weatherDataRepo.findByLatitudeAndLongitudeAndDate(
				pincodeLocation.getLatitude(), pincodeLocation.getLongitude(), date);
		
		if(weatherDataFromDb != null) {
			weatherRedis2.opsForValue().set(redisKey, weatherDataFromDb);
			return weatherDataFromDb;
		}
		
		String jsonResponse = null;
		try {
			jsonResponse= restTemplate.getForObject(
				oldWeatherUrl+"?latitude="+pincodeLocation.getLatitude()+
				"&longitude="+pincodeLocation.getLongitude()+
				"&start_date="+date+"&end_date="+date+"&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m"
				,String.class);
		}
		catch(HttpClientErrorException  e) {
			 ObjectMapper objectMapper = new ObjectMapper();
             JsonNode jsonNode = objectMapper.readTree(e.getResponseBodyAsString());
             String errorMessage = jsonNode.path("reason").asText();
//			System.out.println(jsonResponse.isEmpty());
			throw new IllegalArgumentException(errorMessage);
		}
		
		if(jsonResponse == null) {
			throw new IllegalArgumentException("No response received for lat: "+pincodeLocation.getLatitude()+
					" lon: "+pincodeLocation.getLongitude()+" date: "+ date);
		}
		WeatherData weatherData =  jsonToWeatherData(jsonResponse);
		weatherData.setLatitude(pincodeLocation.getLatitude());
		weatherData.setLongitude(pincodeLocation.getLongitude());
		weatherDataFromDb = createOldWeather(weatherData);
		
		weatherRedis2.opsForValue().set(redisKey, weatherDataFromDb);
		return weatherDataFromDb;
		 
	}
	
	@Override
	public WeatherData createOldWeather(WeatherData weatherData) {
		return weatherDataRepo.save(weatherData);
	}
	
	@Override
	public WeatherData jsonToWeatherData(String jsonResponse) throws JsonMappingException, JsonProcessingException, ParseException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		
		WeatherData weatherData = new WeatherData();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		JsonNode current = rootNode.get("current");
		if(current != null) {
			Date date = dateFormat.parse(current.get("time").asText());
			weatherData.setDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			weatherData.setTemperature(Double.valueOf(current.get("temperature_2m").asText()));
			weatherData.setHumidity(rootNode.get("hourly").get("relative_humidity_2m").get(Calendar.HOUR_OF_DAY-1).asInt());
			weatherData.setWindSpeed(Double.valueOf(current.get("wind_speed_10m").asText()));
		}
		else {
			JsonNode hourly = rootNode.get("hourly");
			int currentHour = Calendar.HOUR_OF_DAY-1;
			Date date = dateFormat.parse(hourly.get("time").get(currentHour).asText());
			weatherData.setDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			weatherData.setTemperature(Double.valueOf(hourly.get("temperature_2m").get(currentHour).asText()));
			weatherData.setHumidity(hourly.get("relative_humidity_2m").get(currentHour).asInt());
			weatherData.setWindSpeed(Double.valueOf(hourly.get("wind_speed_10m").get(currentHour).asText()));
			
		}
		return weatherData;
		
	}

}
