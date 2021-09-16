package com.gant.trade.domain.listener;

import com.gant.trade.domain.Strategy;
import com.gant.trade.mongo.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class StrategyListener extends AbstractMongoEventListener<Strategy> {

    @Autowired
    private SequenceGeneratorService sequenceGenerator;

    public StrategyListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Strategy> event) {
        if (event.getSource().getSeqId() < 1) {
            event.getSource().setSeqId(sequenceGenerator.generateSequence(Strategy.SEQUENCE_NAME));
        }
    }
}
