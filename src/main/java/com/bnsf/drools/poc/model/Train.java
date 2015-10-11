package com.bnsf.drools.poc.model;

import org.kie.api.definition.type.Modifies;

/**
 * 
 * @author rakesh
 *
 */
public class Train extends AbstractBNSFModel<String>{
	
	private String AEIReaderId;
	private double confidenceLevel;
	
	//below 2 to store the last known GPS location
	private double latitude;
	private double longitude;

	@Modifies( { "confidenceLevel"} )
	public void calculatePercentage(double percentageToAdd){
		//for now add the percentage
		confidenceLevel += percentageToAdd;
	}

	/**
	 * @see com.bnsf.drools.poc.model.BNSFModel#getId()
	 */
	public String getId() {
		return getTrainId();
	}
	
	/*
	 * getters and setters
	 */
	public String getAEIReaderId() {
		return AEIReaderId;
	}
	public void setAEIReaderId(String aEIReaderId) {
		AEIReaderId = aEIReaderId;
	}
	public double getConfidenceLevel() {
		return confidenceLevel;
	}
	public void setConfidenceLevel(double confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}
