/**
 * 
 */
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
import static org.junit.Assert.assertNull;

/**
 * @author rakesh
 *
 */
public class GPSLocoEventTest extends AbstractBNSFEventTest{
	
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

	@Test
	public void testGPSLocoEvent_Insert(){
		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId("1234");
		gpsLocoEvent.setLatitude(100);
		gpsLocoEvent.setLongitude(200);
		
		this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        this.session.fireAllRules();
        
        //train should have the latest GPS co-ordinate set
        Train train = trainCache.get(TRAIN_ID);
        assertNotNull("Train should not be null", train);
        assertEquals("Latitude should be same", gpsLocoEvent.getLatitude(), train.getLatitude(), 0.0f);
        assertEquals("Longitude should be same", gpsLocoEvent.getLongitude(), train.getLongitude(), 0.0f);
        assertEquals("Confidence Level does not match", ConfidenceLevelPercentage.GPSLocoEvent.getPositivePercentageLevel(), train.getConfidenceLevel(), 0.0f);
	}
	
	@Test
	public void testGPSLocoEvent_Missing_LocomotiveInventory(){
		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId("12344444444");
		gpsLocoEvent.setLatitude(100);
		gpsLocoEvent.setLongitude(200);
		
		this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        int numberOfRulesFired = this.session.fireAllRules();
		logger.info("Rules fired {}", trackingAgendaEventListener.getRulesFiredList());
        assertEquals("Invalid number of rules fired", 2, numberOfRulesFired);
	}
	
	@Test
	public void testGPSLocoEvent_Missing_Train() throws InterruptedException {
		//remove the train from the cache
		trainCache.delete(TRAIN_ID);
		
		assertNull(trainCache.get(TRAIN_ID));

		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId("1234");
		gpsLocoEvent.setLatitude(100);
		gpsLocoEvent.setLongitude(200);
		
		this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
		int numberOfRulesFired = this.session.fireAllRules();
		logger.info("Rules fired {}", trackingAgendaEventListener.getRulesFiredList());
        assertEquals("Invalid number of rules fired", 3, numberOfRulesFired);

		//Thread.sleep(2000);
		//numberOfRulesFired = this.session.fireAllRules();

	}

	/**
	 * Test for missing GPSLocoEvents
	 * @throws InterruptedException
	 */
	@Test
	public void testGPSLocoEvent_ConsecutiveEvent_Missing() throws InterruptedException{
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

        //sleep for 2 seconds
        Thread.currentThread().sleep(2000);
		this.trackingAgendaEventListener.reset();
		this.session.fireAllRules();
		logger.info("Rules fired {}", trackingAgendaEventListener.getRulesFiredList());
		assertRuleFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");
	}

	/**
	 * Test for missing GPSLocoEvents
	 * @throws InterruptedException
	 */
	@Test
	public void testGPSLocoEvent_ConsecutiveEvent() throws InterruptedException{
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

		//send a consecutive event
		GPSLocoEvent gpsLocoEvent_2 = new GPSLocoEvent();
		gpsLocoEvent_2.setLocomotiveId("1234");
		gpsLocoEvent_2.setLatitude(300);
		gpsLocoEvent_2.setLongitude(400);
		this.gpsHarvestStream.insert(gpsLocoEvent_2);
		this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
		//rule should not be fired gpsLocoEvent_2 should have been processed
		assertRuleNotFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");
		//sleep for 2 seconds
		Thread.currentThread().sleep(2000);

		this.session.fireAllRules();
		//rule should have been fired as a event is missing for the past 2s
		assertRuleFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");
	}
}
