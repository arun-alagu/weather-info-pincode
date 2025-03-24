package com.arun.app.models;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class WeatherData extends BaseModel{
	private Double latitude;
	private Double longitude;
	private LocalDate date;
	private Double temperature;
	private Integer humidity;
	private Double windSpeed;
}
