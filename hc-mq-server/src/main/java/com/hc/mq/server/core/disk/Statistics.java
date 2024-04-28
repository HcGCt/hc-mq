package com.hc.mq.server.core.disk;

/**
 * 消息统计信息
 *
 * @Author hc
 */
public class Statistics {
    public int totalCount; // 总消息数
    public int validCount; // 有效消息数

    public Statistics() {

    }

    public Statistics(int totalCount, int validCount) {
        this.totalCount = totalCount;
        this.validCount = validCount;
    }

    public int increaseTotalCount() {
        return ++totalCount;
    }

    public int increaseValidCount() {
        return ++validCount;
    }

    public int decreaseTotalCount() {
        return --totalCount;
    }

    public int decreaseValidCount() {
        return --validCount;
    }
}
