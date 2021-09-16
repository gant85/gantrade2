package com.gant.trade.domain.listener;

import com.gant.trade.domain.Trade;
import com.gant.trade.mongo.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class TradeListener extends AbstractMongoEventListener<Trade> {

    @Autowired
    private SequenceGeneratorService sequenceGenerator;

    public TradeListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Trade> event) {
        if (event.getSource().getSeqId() < 1) {
            event.getSource().setSeqId(sequenceGenerator.generateSequence(Trade.SEQUENCE_NAME));
        }
    }
}
