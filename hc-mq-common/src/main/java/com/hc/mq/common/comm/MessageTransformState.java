package com.hc.mq.common.comm;

import java.io.Serializable;

/**
 * @Author hc
 */
public enum MessageTransformState implements Serializable {
    SEND_OK,
    SEND_ERROR,
    SEND_TIMEOUT,

}
