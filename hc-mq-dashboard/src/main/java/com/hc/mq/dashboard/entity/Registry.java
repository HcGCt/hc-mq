package com.hc.mq.dashboard.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author hc
 */
public class Registry implements Serializable {
    private int id;
    private String registryKey;
    private String registryInfo;
    private Date updateTime;
    private int hashCode;    // 注册信息唯一标识

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRegistryKey() {
        return registryKey;
    }

    public void setRegistryKey(String registryKey) {
        this.registryKey = registryKey;
    }

    public String getRegistryInfo() {
        return registryInfo;
    }

    public void setRegistryInfo(String registryInfo) {
        this.registryInfo = registryInfo;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }
}
