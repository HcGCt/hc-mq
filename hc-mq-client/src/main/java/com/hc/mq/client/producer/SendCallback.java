package com.hc.mq.client.producer;


/**
 * 异步生产消息回调
 *
 * @Author hc
 */
public interface SendCallback<T> {
    void onSuccess(final T result);
    void onException(final Throwable e);
}
