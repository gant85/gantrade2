package com.gant.trade.websocket.bybit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gant.trade.exception.BusinessRuntimeException;
import com.gant.trade.model.CandlestickEvent;
import com.gant.trade.model.Timeframe;
import com.gant.trade.proxy.bybit.v5.BybitEncryption;
import com.gant.trade.rest.model.SymbolInfoTO;
import com.gant.trade.websocket.bybit.mapper.TopicKLineDataMapper;
import com.gant.trade.websocket.bybit.mapper.TopicKLineDataMapperImpl;
import com.gant.trade.websocket.bybit.model.TopicKLine;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ClientEndpoint
public class ByBitWebSocketClient {

    private final BybitEncryption bybitEncryption;
    private final String url;
    private final ObjectMapper objectMapper;
    private Session session;
    private ObservableEmitter<CandlestickEvent> emitterKline;
    private final TopicKLineDataMapper topicKLineDataMapper = new TopicKLineDataMapperImpl();

    public ByBitWebSocketClient(String url, BybitEncryption bybitEncryption) throws BusinessRuntimeException {
        this.url = url;
        this.bybitEncryption = bybitEncryption;
        this.objectMapper = new ObjectMapper();
        connect();
    }

    public void connect() throws BusinessRuntimeException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, new URI(url));
        } catch (URISyntaxException | DeploymentException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected to {}", this.url);
        this.session = session;
        try {
            // auth
            long expires = Instant.now().toEpochMilli() + 10000;
            Map<String, Object> authMessage = new HashMap<>();
            authMessage.put("op", "auth");
            authMessage.put("args", new Object[]{bybitEncryption.getApiKey(), expires, bybitEncryption.genWebSocketSign(expires)});
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(authMessage));
        } catch (Exception e) {
            log.error("onOpen", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            if (message != null) {
                if (message.contains("topic") && message.contains("kline")) {
                    TopicKLine topicKLine = this.objectMapper.readValue(message, TopicKLine.class);
                    CandlestickEvent candlestickEvent = topicKLineDataMapper.mapList(topicKLine.getData()).stream().findFirst().orElse(null);
                    assert candlestickEvent != null;
                    candlestickEvent.setSymbol(topicKLine.getTopic().split("\\.")[2]);
                    emitterKline.onNext(candlestickEvent);
                }
            }
        } catch (Exception e) {
            log.error("onMessage", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.error("onError", t);
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 900000)
    private void ping() throws IOException {
        try {
            if (this.session != null && session.isOpen()) {
                Map<String, Object> message = new HashMap<>();
                message.put("req_id", "100001");
                message.put("op", "ping");
                session.getBasicRemote().sendText(this.objectMapper.writeValueAsString(message));
            } else {
                connect();
            }
        } catch (Exception e) {
            log.error("ping", e);
        }
    }

    public Observable<CandlestickEvent> subscribeKlines(List<SymbolInfoTO> symbolInfo, Timeframe timeframe) throws IOException {
        Observable<CandlestickEvent> observableKline = Observable.<CandlestickEvent>create(e -> emitterKline = e).doOnDispose(() -> {
            log.info("observableKline disposed");
        });
        Object[] subscriptions = symbolInfo.stream().map(s -> "kline.".concat(String.valueOf(timeframe.getMinutes()).concat(".").concat(s.getSymbol()))).toArray();
        Map<String, Object> subscriptionMessage = new HashMap<>();
        subscriptionMessage.put("op", "subscribe");
        subscriptionMessage.put("args", subscriptions);
        log.info("Subscribed to {}", subscriptions);
        session.getBasicRemote().sendText(this.objectMapper.writeValueAsString(subscriptionMessage));

        return observableKline;
    }
}
