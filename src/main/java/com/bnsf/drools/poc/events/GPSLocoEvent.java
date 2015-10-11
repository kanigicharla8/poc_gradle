package com.bnsf.drools.poc.events;

import com.google.common.base.Objects;

/**
 * 
 * @author rakesh
 *
 */
public class GPSLocoEvent extends AbstractBNSFEvent{
	private double latitude;
	private double longitude;

	@Override
	public int hashCode() {
		return Objects.hashCode(this.latitude, this.longitude, this.locomotiveId, this.eventTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final GPSLocoEvent other = (GPSLocoEvent) obj;

		return Objects.equal(this.latitude, other.latitude)
				&& Objects.equal(this.longitude, other.longitude)
				&& Objects.equal(this.locomotiveId, other.locomotiveId)
				&& Objects.equal(this.eventTime, other.eventTime);
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
