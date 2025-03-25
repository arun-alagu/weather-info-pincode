package com.arun.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.arun.app.dtos.WeatherDataDto;
import com.arun.app.models.PincodeLocation;
import com.arun.app.models.WeatherData;
import com.arun.app.repositories.PincodeLocationRepo;

@Configuration
public class RedisTemplateConfig {
	@Bean
	RedisTemplate<String, PincodeLocation> redisTemplate(RedisConnectionFactory connectionFactory) {
	    RedisTemplate<String, PincodeLocation> template = new RedisTemplate<>();
	    template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
//	    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PincodeLocation.class));
	    return template;
	}
	
	@Bean
	RedisTemplate<String, WeatherData> weatherRedisTemplate2(RedisConnectionFactory connectionFactory) {
	    RedisTemplate<String, WeatherData> template = new RedisTemplate<>();
	    template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
//		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(WeatherData.class));
	    return template;
	}
	
	@Bean
	RedisTemplate<String, WeatherDataDto> weatherRedisTemplate(RedisConnectionFactory connectionFactory) {
	    RedisTemplate<String, WeatherDataDto> template = new RedisTemplate<>();
	    template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
//		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(WeatherDataDto.class));
	    return template;
	}
}
