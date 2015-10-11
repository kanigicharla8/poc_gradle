/**
 * 
 */
package com.bnsf.drools.poc;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.cache.repo.SimpleLocomotiveInventoryCache;
import com.bnsf.drools.poc.cache.repo.SimpleTrainCache;
import com.bnsf.drools.poc.channel.LocomotiveInventoryMissingInCacheEventEventChannel;
import com.bnsf.drools.poc.channel.TrainMissingInCacheEventChannel;
import com.bnsf.drools.poc.events.AEIEvent;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author rakesh
 *
 */
public class AEIEventTest {

	private static final String LOCOMOTIVE_ID = "1234";

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	private static final String TRAIN_ID = "Train-ABC";
	private KieSession session;
	private EntryPoint aeiHarvestStream;

	private CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository = new SimpleLocomotiveInventoryCache();
	private CacheRepository<Train> trainCache = new SimpleTrainCache();

	@Before
	public void setup() {
		// load cache
		loadData();

		this.session = createSession();
		this.aeiHarvestStream = this.session.getEntryPoint("AEI Harvest Stream");
	}

	@After
	public void cleanup(){
		this.session.dispose();
	}

	/**
	 * 
	 */
	private void loadData() {
		// populate the inventory
		LocomotiveInventory locomotiveInventory = new LocomotiveInventory();
		locomotiveInventory.setLocomotiveId(LOCOMOTIVE_ID);
		locomotiveInventory.setTrainId(TRAIN_ID);
		locomotiveInventoryCacheRepository.save(locomotiveInventory.getId(), locomotiveInventory);

		// populate the trains
		Train train = new Train();
		train.setTrainId(TRAIN_ID);
		trainCache.save(train.getId(), train);
	}

	@Test
	public void testGPSLocoEvent_Insert() {
		AEIEvent aeiEvent = new AEIEvent();
		aeiEvent.setLocomotiveId(LOCOMOTIVE_ID);
		aeiEvent.setAEIReaderId("Reader-123");

		this.aeiHarvestStream.insert(aeiEvent);
		this.session.getAgenda().getAgendaGroup("evaluation").setFocus();
		this.session.fireAllRules();

		// train should have the latest GPS co-ordinate set
		Train train = trainCache.get(TRAIN_ID);
		assertNotNull("Train should not be null", train);
		assertEquals("AEI Reader should be same", aeiEvent.getAEIReaderId(), train.getAEIReaderId());
	}

	@Test
	public void testAEIEvent_Missing_LocomotiveInventory() {
		AEIEvent aeiEvent = new AEIEvent();
		aeiEvent.setLocomotiveId("InvalidlocomotiveId");
		aeiEvent.setAEIReaderId("Reader-123");

		this.aeiHarvestStream.insert(aeiEvent);
		this.session.getAgenda().getAgendaGroup("evaluation").setFocus();
		int numberOfRulesFired = this.session.fireAllRules();
        assertEquals("Invalid number of rules fired", 2, numberOfRulesFired);
	}

	@Test
	public void testAEIEvent_Missing_Train() {
		// remove the train from the cache
		trainCache.delete(TRAIN_ID);

		assertNull(trainCache.get(TRAIN_ID));

		AEIEvent aeiEvent = new AEIEvent();
		aeiEvent.setLocomotiveId(LOCOMOTIVE_ID);
		aeiEvent.setAEIReaderId("Reader-123");

		this.aeiHarvestStream.insert(aeiEvent);
		this.session.getAgenda().getAgendaGroup("evaluation").setFocus();
		int numberOfRulesFired = this.session.fireAllRules();
        assertEquals("Invalid number of rules fired", 3, numberOfRulesFired);
	}

	private KieSession createSession() {
		KieServices ks = KieServices.Factory.get();
		KieContainer kc = ks.getKieClasspathContainer();
		session = kc.newKieSession("AEIHarvestKS");
		// global variables
		session.setGlobal( "locomotiveInventoryCacheRepository", locomotiveInventoryCacheRepository );
		session.setGlobal("trainCache", trainCache);
		session.setGlobal("logger", logger);

		// debug listeners
		session.addEventListener(new DebugAgendaEventListener());
		session.addEventListener(new DebugRuleRuntimeEventListener());

		session.registerChannel("missing_data_loco_inv", new LocomotiveInventoryMissingInCacheEventEventChannel());
		session.registerChannel("missing_data_train", new TrainMissingInCacheEventChannel());

		return session;
	}
}
