package com.hc.mq.common.comm;

import java.io.Serializable;

/**
 * @Author hc
 */
public enum LocalTransactionState implements Serializable {
    COMMIT_MESSAGE,
    ROLLBACK_MESSAGE,
    UNKNOW,
}
