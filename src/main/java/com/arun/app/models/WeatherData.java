package com.arun.app.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
		indexes = {
				@Index(name = "index_latitude_longitude_date", columnList = "latitude, longitude, date")
		}
		)
public class WeatherData extends BaseModel implements Serializable{
	private Double latitude;
	private Double longitude;
	private LocalDate date;
	private Double temperature;
	private Integer humidity;
	private Double windSpeed;
}
