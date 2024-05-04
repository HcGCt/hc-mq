package com.hc.mq.dashboard.entity;

import java.time.LocalDateTime;

/**
 * @Author hc
 */
public class Topic {
    private String topic;
    private LocalDateTime creatTime;

    public Topic() {

    }

    public Topic(String topic, LocalDateTime creatTime) {
        this.topic = topic;
        this.creatTime = creatTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(LocalDateTime creatTime) {
        this.creatTime = creatTime;
    }
}
