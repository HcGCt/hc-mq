package com.hc.mq.rpctest.server.impl;


import com.hc.mq.rpctest.client.dto.Student;
import com.hc.mq.rpctest.client.service.DemoService;

/**
 * @Author hc
 */
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        System.out.println(name + " hello!");
        return name;
    }

    @Override
    public Student getStudent(String name) {
        Student student = new Student();
        student.setAge(20);
        student.setName(name);
        student.setSchool("xxx");
        return student;
    }
}
