package com.hc.mq.client.consumer;

/**
 * @Author hc
 */
public interface PullCallback<T> {
    void onSuccess(final T result);
    void onException(final Throwable e);
}
