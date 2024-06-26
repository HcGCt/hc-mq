package com.hc.mq.common.comm;

/**
 * @Author hc
 */
public class Constants {

    public static final String DEFAULT_BROKER = "default_broker";

    public static final String DEFAULT_GROUP = "default";

    public static final String TRANSACTION_TOPIC = "transaction_topic";
    public static final String TRANSACTION_QUEUE = "transaction_queue";


    public static final String DEFAULT_MESSAGE_STORE_PATH = "./message-store/";
    public static final String DEFAULT_MESSAGE_DATA_STORE_PATH = "/queue_data.log";
    public static final String DEFAULT_MESSAGE_NEW_DATA_STORE_PATH = "/new_queue_data.log";
    public static final String DEFAULT_MESSAGE_STAT_STORE_PATH = "/queue_stat.log";

    public static final int MESSAGE_HEADER_LENGTH = 4;

    public static final byte COMMIT_MESSAGE = 0x0;
    public static final byte ROLLBACK_MESSAGE = 0x1;
    public static final byte UNCERTAIN_TRAN = 0x2;


    public static final String DEFAULT_CLIENT_CONFIG_PREFIX = "hc.mq.client";
    public static final String DEFAULT_SERVER_CONFIG_PREFIX = "hc.mq.server";

}
