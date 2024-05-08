package com.hc.mq.common.util;

import java.io.*;

/**
 * @Author hc
 */
public class BinaryUtil {

    // 序列化
    public static byte[] toByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                outputStream.writeObject(object);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    // 反序列化
    public static Object toObject(byte[] array) throws IOException {
        Object object = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                object = objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return object;
    }
}
