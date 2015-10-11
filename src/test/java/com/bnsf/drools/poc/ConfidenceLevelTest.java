package com.bnsf.drools.poc;

import com.bnsf.drools.poc.events.ConfidenceLevelPercentage;
import com.bnsf.drools.poc.events.GPSLocoEvent;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rakesh on 9/16/15.
 */
public class ConfidenceLevelTest extends AbstractBNSFEventTest{

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setup(){
        //load cache
        loadData();

        this.session = createSession();
        this.gpsHarvestStream = this.session.getEntryPoint( "GPS Harvest Stream" );
    }

    @After
    public void cleanup(){
        this.session.dispose();
        this.trackingAgendaEventListener.reset();
    }

    private void loadData() {
        //populate the inventory
        LocomotiveInventory locomotiveInventory = new LocomotiveInventory();
        locomotiveInventory.setLocomotiveId("1234");
        locomotiveInventory.setTrainId(TRAIN_ID);
        locomotiveInventoryCacheRepository.save(locomotiveInventory.getId(), locomotiveInventory);

        //populate the trains
        Train train = new Train();
        train.setTrainId(TRAIN_ID);
        trainCache.save(train.getId(), train);
    }

    /**
     * Test for missing GPSLocoEvents
     * @throws InterruptedException
     */
    @Test
    public void testGPSLocoEvent_Handle_MissingGPSLocoEvent() throws InterruptedException{
        GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
        gpsLocoEvent.setLocomotiveId("1234");
        gpsLocoEvent.setLatitude(100);
        gpsLocoEvent.setLongitude(200);

        this.gpsHarvestStream.insert( gpsLocoEvent );
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        this.session.fireAllRules();

        //train should have the latest GPS co-ordinate set
        Train train = trainCache.get(TRAIN_ID);
        assertNotNull("Train should not be null", train);
        assertEquals("Latitude should be same", gpsLocoEvent.getLatitude(), train.getLatitude(), 0.0f);
        assertEquals("Longitude should be same", gpsLocoEvent.getLongitude(), train.getLongitude(), 0.0f);
        assertEquals("Confidence level not increased", ConfidenceLevelPercentage.GPSLocoEvent.getPositivePercentageLevel(), train.getConfidenceLevel(), 0.0f);

        //sleep for 2 seconds
        Thread.currentThread().sleep(2000);
        this.trackingAgendaEventListener.reset();
        this.session.fireAllRules();
        assertRuleFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");
        assertRuleFired("Handle MissingGPSLocoEvent");


        train = trainCache.get(TRAIN_ID);
        //confidence level should have reduced from 10 to 5
        assertEquals("Confidence level not reduced", 5.0,train.getConfidenceLevel(),0.0f);
        assertRuleFired("Handle Changes to ConfidenceLevels");
    }
}
