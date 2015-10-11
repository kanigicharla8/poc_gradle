package com.bnsf.drools.poc.events;

/**
 * Created by rakesh on 9/16/15.
 */
public class MissingGPSLocoEvent implements BNSFEvent{
    private GPSLocoEvent gpsLocoEvent;

    public MissingGPSLocoEvent(GPSLocoEvent gpsLocoEvent) {
        this.gpsLocoEvent = gpsLocoEvent;
    }

    public GPSLocoEvent getGpsLocoEvent() {
        return gpsLocoEvent;
    }
}
