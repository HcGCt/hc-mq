package com.hc.mq.client.consumer.annotation;

import java.lang.annotation.*;

import static com.hc.mq.common.comm.Constants.DEFAULT_GROUP;


/**
 * 标识消费者
 * @Author hc
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Consumer {
    String group() default DEFAULT_GROUP;

    String topic();

    // 事务消费 todo
}
