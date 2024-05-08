package com.hc.mq.common.comm;

/**
 * @Author hc
 */
public class MqException extends RuntimeException {

    public MqException(String msg) {
        super(msg);
    }

    public MqException(Throwable cause) {
        super(cause);
    }
}
