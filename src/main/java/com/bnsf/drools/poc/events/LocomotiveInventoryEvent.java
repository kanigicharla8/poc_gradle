package com.bnsf.drools.poc.events;

import com.google.common.base.Objects;

/**
 * 
 * @author rakesh
 *
 */
public class LocomotiveInventoryEvent extends AbstractBNSFEvent{
	private String trainId;
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.trainId, this.locomotiveId, this.eventTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LocomotiveInventoryEvent other = (LocomotiveInventoryEvent) obj;

		return Objects.equal(this.trainId, other.trainId)
				&& Objects.equal(this.locomotiveId, other.locomotiveId)
				&& Objects.equal(this.eventTime, other.eventTime);
	}
}
