package com.arun.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arun.app.models.PincodeLocation;

@Repository
public interface PincodeLocationRepo extends JpaRepository<PincodeLocation, Long>{
	PincodeLocation findByPincode(String pincode);
}
