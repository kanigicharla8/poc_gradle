package com.bnsf.drools.poc;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.cache.repo.SimpleLocomotiveInventoryCache;
import com.bnsf.drools.poc.cache.repo.SimpleTrainCache;
import com.bnsf.drools.poc.channel.ConfidenceLevelChangeEventChannel;
import com.bnsf.drools.poc.channel.LocomotiveInventoryMissingInCacheEventEventChannel;
import com.bnsf.drools.poc.channel.TrainMissingInCacheEventChannel;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;
import com.bnsf.drools.poc.util.TrackingAgendaEventListener;

import org.kie.api.KieServices;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by rakesh on 9/16/15.
 */
public abstract class AbstractBNSFEventTest {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected TrackingAgendaEventListener trackingAgendaEventListener = new TrackingAgendaEventListener();

    protected static final String TRAIN_ID = "Train-ABC";

    protected KieSession session;
    protected EntryPoint gpsHarvestStream;

    protected CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository = new SimpleLocomotiveInventoryCache();
    protected CacheRepository<Train> trainCache = new SimpleTrainCache();

    protected KieSession createSession() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.getKieClasspathContainer();
        session = kc.newKieSession("GPSHarvestKS");
        //global variables
        session.setGlobal("locomotiveInventoryCacheRepository", locomotiveInventoryCacheRepository);
        session.setGlobal("trainCache", trainCache);
        session.setGlobal("logger", logger);

        session.registerChannel("missing_data_loco_inv", new LocomotiveInventoryMissingInCacheEventEventChannel());
        session.registerChannel("missing_data_train", new TrainMissingInCacheEventChannel());
        session.registerChannel("confidence_level_change_channel", new ConfidenceLevelChangeEventChannel());

        //debug listeners
        session.addEventListener(trackingAgendaEventListener);
        if(false){
            session.addEventListener( new DebugAgendaEventListener() );
            session.addEventListener( new DebugRuleRuntimeEventListener() );
        }
        return session;
    }

    protected void assertRuleFired(final String ruleName){
        assertTrue("Expecting "+ruleName+" to be fired",trackingAgendaEventListener.isRuleFired(ruleName));
    }

    protected void assertRuleNotFired(final String ruleName) {
        assertFalse("Expecting " + ruleName + " to be fired", trackingAgendaEventListener.isRuleFired(ruleName));
    }
}
