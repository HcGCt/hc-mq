package com.hc.mq.rpctest.client.service;


import com.hc.mq.rpctest.client.dto.Student;

/**
 * @Author hc
 */
public interface DemoService {

    String sayHello(String name);

    Student getStudent(String name);
}
