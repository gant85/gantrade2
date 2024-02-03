package com.gant.trade.service;

import com.gant.trade.domain.SymbolInfo;
import com.gant.trade.model.CandlestickSymbol;
import com.gant.trade.model.Timeframe;
import org.ta4j.core.BarSeries;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public interface HistoricalCandlesService<T,K> {

    public Map<CandlestickSymbol, BarSeries> requestHistoricalCandles(T exchange, Timeframe timeframe, List<SymbolInfo> tradedCurrencies) throws NoSuchAlgorithmException, InvalidKeyException;

    public Map<CandlestickSymbol, BarSeries> requestHistoricalCandles(T exchange, Timeframe timeframe, List<SymbolInfo> tradedCurrencies, Long startTime, Long endTime) throws NoSuchAlgorithmException, InvalidKeyException;

    public List<K> getCandlestickBars(T exchange, Timeframe timeframe, SymbolInfo symbolInfo) throws NoSuchAlgorithmException, InvalidKeyException;

    public List<K> getCandlestickBars(T exchange, Timeframe timeframe, SymbolInfo symbolInfo, Long startTime, Long endTime) throws NoSuchAlgorithmException, InvalidKeyException;
}

