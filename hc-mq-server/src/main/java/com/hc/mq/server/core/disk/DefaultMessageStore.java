package com.hc.mq.server.core.disk;

import com.hc.mq.client.common.MqException;
import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;
import com.hc.mq.client.util.BinaryUtil;
import com.hc.mq.server.config.MqServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.hc.mq.client.common.Constants.*;

/**
 * @Author hc
 */
public class DefaultMessageStore implements IMessageStore {

    private static Logger logger = LoggerFactory.getLogger(DefaultMessageStore.class);

    private int maxMessages;
    private double validMessageRatio;

    private static volatile DefaultMessageStore instance = new DefaultMessageStore();

    private DefaultMessageStore() {
        maxMessages = MqServerConfig.getInstance().getMaxMessages();
        validMessageRatio = MqServerConfig.getInstance().getValidMessageRatio();
    }

    public static DefaultMessageStore getInstance() {
        return instance;
    }

    @Override
    public synchronized void createQueueFiles(String queueName) throws IOException {
        File queueDir = new File(getQueueDir(queueName));
        if (!queueDir.exists()) {
            synchronized (DefaultMessageStore.class) {
                boolean made = queueDir.mkdirs();
                if (!made) {
                    logger.error("创建目录失败: {}", queueDir.getAbsolutePath());
                    throw new IOException("创建目录失败: " +
                            queueDir.getAbsolutePath());
                }
            }
        }
        File dataPath = new File(getQueueDataPath(queueName));
        if (!dataPath.exists()) {
            synchronized (DefaultMessageStore.class) {
                boolean created = dataPath.createNewFile();
                if (!created) {
                    logger.error("创建data文件失败: {}", queueDir.getAbsolutePath());
                    throw new IOException("创建data文件失败: " +
                            dataPath.getAbsolutePath());
                }
            }
        }
        File statPath = new File(getQueueStatPath(queueName));
        if (!statPath.exists()) {
            synchronized (DefaultMessageStore.class) {
                boolean created = statPath.createNewFile();
                if (!created) {
                    logger.error("创建stat文件失败: {}", queueDir.getAbsolutePath());
                    throw new IOException("创建stat文件失败: " +
                            statPath.getAbsolutePath());
                }
            }
        }
        // 写入默认统计信息
        Statistics stat = new Statistics(0, 0);
        writeStat(queueName, stat);
    }


    @Override
    public void storeMessage(MessageQueue queue, Message message) throws IOException {
        String queueName = queue.getQueueName();
        boolean exist = checkFileExists(queueName);
        if (!exist) {
            logger.debug("文件中队列不存在，队列名: {}, 准备创建", queue.getQueueName());
            createQueueFiles(queueName);
        }
        byte[] bytes = BinaryUtil.toByteArray(message);

        synchronized (queue) {
            String path = getQueueDataPath(queueName);
            File dataFile = new File(path);
            message.setOffsetBegin(dataFile.length() + MESSAGE_HEADER_LENGTH);
            message.setOffsetEnd(message.getOffsetBegin() + bytes.length);
            // 写入文件 todo 直接缓存区写入 NIO
            try (OutputStream outputStream = new FileOutputStream(path, true)) {
                try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                    dataOutputStream.writeInt(bytes.length); // 消息长度(4字节)
                    dataOutputStream.write(bytes); // 消息体
                }
            }
            // 更新 stat.log 文件中的数据
            Statistics stat = readStat(queueName);
            stat.increaseTotalCount();
            stat.increaseValidCount();
            writeStat(queueName, stat);
            logger.debug("消息:{} 已持久化, 队列: {}", message.getMsgId(), queueName);
        }

    }

    @Override
    public void deleteMessage(MessageQueue queue, Message message) throws IOException {
        // 逻辑删除
        synchronized (queue) {
            // 随机访问文件
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(getQueueDataPath(queue.getQueueName()), "rw")) {
                byte[] bytesFrom = new byte[(int) (message.getOffsetEnd() - message.getOffsetBegin())];
                randomAccessFile.seek(message.getOffsetBegin()); // 光标移动
                randomAccessFile.read(bytesFrom);
                Message messageInFile = (Message) BinaryUtil.toObject(bytesFrom);
                // 逻辑删除
                messageInFile.setValid((byte) 0x0);
                byte[] bytesTo = BinaryUtil.toByteArray(messageInFile);
                randomAccessFile.seek(message.getOffsetBegin());
                randomAccessFile.write(bytesTo);
            } catch (Exception e) {
                logger.error("从文件删除消息失败!", e);
            }
            // 更新 stat.log 文件中的数据
            Statistics stat = readStat(queue.getQueueName());
            stat.decreaseValidCount();
            writeStat(queue.getQueueName(), stat);

            logger.debug("消息:{} 已逻辑删除, 队列: {}", message.getMsgId(), queue.getQueueName());
        }
    }

    @Override
    public boolean isNeedClean(String queueName) {
        // 当文件中消息总数超过2k, 但有效消息不足30%则进行清理
        Statistics stat = readStat(queueName);
        return stat.totalCount > maxMessages && (double) stat.validCount / stat.totalCount < validMessageRatio;
    }

    @Override
    public void clean(MessageQueue queue) throws IOException, MqException {
        // 加载有效消息，删除原消息文件
        synchronized (queue) {
            String queueName = queue.getQueueName();
            long startCleanTimestamp = System.currentTimeMillis();
            File newDataFile = new File(getNewQueueDataPath(queue.getQueueName()));
            if (newDataFile.exists()) {
                logger.error("new_queue_data.log 文件已经存在, queueName: {}", queueName);
                throw new MqException("new_queue_data.log 文件已经存在, queueName = " + queueName);
            }
            boolean created = newDataFile.createNewFile();
            if (!created) {
                logger.error("new_queue_data.log 创建失败 queueName: {}", queueName);
                throw new MqException("new_queue_data.log 创建失败, queueName = " + queueName);
            }
            List<Message> validMessages = loadAllValidMessage(queueName);

            // 写入新文件
            try (OutputStream outputStream = Files.newOutputStream(Paths.get(getNewQueueDataPath(queueName)))) {
                try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                    for (Message message : validMessages) {
                        byte[] bytes = BinaryUtil.toByteArray(message);
                        dataOutputStream.writeInt(bytes.length);
                        dataOutputStream.write(bytes);
                    }
                }
            }

            // 删除旧文件
            File dataFile = new File(getQueueDataPath(queue.getQueueName()));
            boolean deleted = dataFile.delete();
            if (!deleted) {
                logger.error("删除 queue_data.log 文件失败! dataFile: {}", dataFile.getAbsolutePath());
                throw new MqException("删除 queue_data.log 文件失败! dataFile = " + dataFile.getAbsolutePath());
            }

            // 重命名
            boolean renamed = newDataFile.renameTo(dataFile);
            if (!renamed) {
                logger.error("重命名 new_queue_data.log 文件失败! newDataFil: {}", newDataFile.getAbsolutePath());
                throw new MqException("重命名 new_queue_data.log 文件失败! newDataFile = " + newDataFile.getAbsolutePath());
            }

            // 更新 stat.log 文件
            Statistics stat = readStat(queueName);
            stat.totalCount = validMessages.size();
            stat.validCount = validMessages.size();
            writeStat(queueName, stat);
            logger.debug("清除队列:{} 无效消息成功, 消耗时间: {} ms", queueName, System.currentTimeMillis() - startCleanTimestamp);
        }
    }

    @Override
    public List<Message> loadAllValidMessage(String queueName) throws IOException, MqException {
        Statistics stat = readStat(queueName);
        List<Message> messageList = new ArrayList<>(stat != null ? stat.validCount : 10);
        try (InputStream inputStream = Files.newInputStream(Paths.get(getQueueDataPath(queueName)))) {
            try (DataInputStream dataInputStream = new DataInputStream(inputStream)) { // 这个流可以读取字节
                int currentOffsetBegin = 0;
                // 循环读取所有有效消息
                while (true) {
                    int messageLength = dataInputStream.readInt();
                    byte[] messageBinaryFrom = new byte[messageLength];
                    int messageActualLength = dataInputStream.read(messageBinaryFrom);
                    if (messageActualLength != messageLength) {
                        logger.error("读取消息时文件错误, queueName: {}", queueName);
                        throw new MqException("读取消息时文件错误, queueName = " + queueName);
                    }
                    Message messageInFile = (Message) BinaryUtil.toObject(messageBinaryFrom);
                    if (messageInFile.getValid() != 0x1) {
                        currentOffsetBegin += (MESSAGE_HEADER_LENGTH + messageActualLength);
                        continue;
                    }
                    messageInFile.setOffsetBegin(currentOffsetBegin + MESSAGE_HEADER_LENGTH);
                    messageInFile.setOffsetEnd(currentOffsetBegin + MESSAGE_HEADER_LENGTH + messageActualLength);
                    messageList.add(messageInFile);
                    currentOffsetBegin += MESSAGE_HEADER_LENGTH + messageActualLength;
                }
            } catch (EOFException e) {
                logger.info("读取 有效Message 数据完成!");
            }
        }
        return messageList;
    }

    @Override
    public String getQueueDir(String queueName) {
        return DEFAULT_MESSAGE_STORE_PATH + MqServerConfig.getInstance().getBrokerName() + "/" + queueName;
    }

    @Override
    public String getQueueDataPath(String queueName) {
        return getQueueDir(queueName) + DEFAULT_MESSAGE_DATA_STORE_PATH;
    }

    @Override
    public String getQueueStatPath(String queueName) {
        return getQueueDir(queueName) + DEFAULT_MESSAGE_STAT_STORE_PATH;
    }

    // 写入queue_stat.log
    private void writeStat(String queueName, Statistics stat) {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(getQueueStatPath(queueName)))) {
            PrintWriter writer = new PrintWriter(outputStream);
            writer.write(stat.totalCount + "\t" + stat.validCount);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从queue_stat.log
    private Statistics readStat(String queueName) {
        Statistics stat = new Statistics();
        try (InputStream inputStream = Files.newInputStream(Paths.get(getQueueStatPath(queueName)))) {
            Scanner scanner = new Scanner(inputStream);
            stat.totalCount = scanner.nextInt();
            stat.validCount = scanner.nextInt();
            return stat;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized boolean checkFileExists(String queueName) {
        return Files.exists(Paths.get(getQueueDir(queueName))) &&
                Files.exists(Paths.get(getQueueDataPath(queueName))) &&
                Files.exists(Paths.get(getQueueStatPath(queueName)));
    }

    private String getNewQueueDataPath(String queueName) {
        return getQueueDir(queueName) + DEFAULT_MESSAGE_NEW_DATA_STORE_PATH;
    }
}
