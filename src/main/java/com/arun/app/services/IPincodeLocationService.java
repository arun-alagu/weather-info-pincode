package com.arun.app.services;

import com.arun.app.models.PincodeLocation;

public interface IPincodeLocationService {
		PincodeLocation getPincodeLocation(String pincode) throws Exception;
		PincodeLocation createPincodeLocation(PincodeLocation pincodeLocation) throws Exception;
		PincodeLocation updatePincodeLocation(PincodeLocation pincodeLocation) throws Exception;
		PincodeLocation jsonToPincodeLocation(String jsonResponse) throws Exception;
}
