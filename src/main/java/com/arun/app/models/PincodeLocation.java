package com.arun.app.models;

import java.io.Serializable;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "pincode_locations")
public class PincodeLocation extends BaseModel implements Serializable {
	private String pincode;
	private String name;
	private Double latitude;
	private Double longitude;
	private String country;
}
