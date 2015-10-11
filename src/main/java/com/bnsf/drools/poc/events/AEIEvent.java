package com.bnsf.drools.poc.events;

import com.google.common.base.Objects;

/**
 * 
 * @author rakesh
 *
 */
public class AEIEvent extends AbstractBNSFEvent {
	private String AEIReaderId;

	@Override
	public int hashCode() {
		return Objects.hashCode(this.AEIReaderId, this.locomotiveId, this.eventTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AEIEvent other = (AEIEvent) obj;

		return Objects.equal(this.AEIReaderId, other.AEIReaderId)
				&& Objects.equal(this.locomotiveId, other.locomotiveId)
				&& Objects.equal(this.eventTime, other.eventTime);
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
}
