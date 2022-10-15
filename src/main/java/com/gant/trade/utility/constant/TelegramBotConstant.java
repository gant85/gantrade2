package com.gant.trade.utility.constant;

import java.util.Arrays;
import java.util.List;

public class TelegramBotConstant {

    public static final String HELP = "/help";
    public static final String STATUS = "/status";
    public static final String AUTOMATION_START = "/automationStart";
    public static final String AUTOMATION_STOP = "/automationStop";
    public static final String AUTOMATION_LIST = "/automationlist";
    public static final String STRATEGY_STATUS_INFO = "/strategyStatusInfo";
    public static final String STRATEGY_TOTAL_GAIN = "/strategyTotalGain";
    public static final String OPEN_ORDER = "/openorder";
    public static final String START = "/start";

    public static final String CALLBACK_STATUS_AUTOMATION = "status_strategy";
    public static final String CALLBACK_CLOSE_ORDER = "close_order";

    public static final List<String> COMMAND_LIST = Arrays.asList(
            HELP,
            STATUS,
            AUTOMATION_LIST,
            OPEN_ORDER
    );

    private TelegramBotConstant() {

    }
}
