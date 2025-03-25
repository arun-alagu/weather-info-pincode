package com.arun.app.models;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
		name = "pincode_locations",
		indexes = {
				@Index(name ="index_pincode", columnList ="pincode")
		}
		)
public class PincodeLocation extends BaseModel implements Serializable {
	private String pincode;
	private String name;
	private Double latitude;
	private Double longitude;
	private String country;
}
