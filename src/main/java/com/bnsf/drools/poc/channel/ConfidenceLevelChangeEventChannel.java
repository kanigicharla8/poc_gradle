package com.bnsf.drools.poc.channel;

import com.bnsf.drools.poc.events.ConfidenceLevelChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rakesh on 9/17/15.
 */
public class ConfidenceLevelChangeEventChannel extends AbstractBNSFChannel<ConfidenceLevelChangeEvent>{
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handle(ConfidenceLevelChangeEvent obj) {
        logger.info("Confidence level changed for {} from {} to {}",obj.getTrain().getTrainId(), obj.getOldPercentage(), obj.getNewPercentage());
    }
}
