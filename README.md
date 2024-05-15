# 1.项目设计

## 1.1 核心模块

- **生产者producer**

  行为：发布publish

- **消费者comsumer**

  行为：订阅subsribe

- **服务代理broker**

CS模式，其中client包括producer与comsumer，server即broker，响应生产者/消费者请求。

消费者消费消息的模式包括两种：

- 推push：broker向消费者推送消息
- 拉pull：消费者向broker拉取消息

kafka消费消息为pull；rocketMQ支持push与pull

**实现拉模式**

### 1.2 核心概念

- Message：消息实体
- Topic：消息主题
- MessageQueue：一个topic可设置多个队列，是消息存储的实体
- comsumer
- comsumer group
- producer
- broker

## 1.2 Message设计

## 1.3 持久化存储

消息存于内存中（即MessageQueue中），持久化存储采用关系型数据库（for dashboard todo）+文件形式

数据用于管理中心队列主题增删改查等

文件存储消息：

- 消息数据queue_data，二进制存储，结构如下

  ```
    4 字节				 30 字节
  +------------+--------------------------------+
  | 消息长度    |	       消息体二进制            |
  +------------+--------------------------------+
  ```

  消息之间直接相连，使用序列化转为Message对象，消息偏移量为**消息体起始、末尾偏移量**

- 消息统计信息queue_stat.txt

## 1.5 rpc通信

[自定义rpc通信](https://github.com/HcGCt/hc-rpc)：

rpc组件设计如下：
![](https://cccblogimgs.oss-cn-hangzhou.aliyuncs.com/rpc%E6%9E%B6%E6%9E%84.png)

模块组成如下：

- loadbalance:负载均衡策略
- protocol:协议层，定义消息传输的格式等
- invoker:服务调用方,异步调用
- provider:服务提供方
- registry:注册中心
- SPI：允许在运行时动态地加载实现特定接口的类，而不需要在代码中显式地指定该类，从而实现解耦和灵活性。实现高效的组件化和模块化，提高组件的扩展性。

## 1.6 模块划分

- 服务端：broker
    - 内存管理：队列与消息存储
    - 持久化：
        - 数据库：
            - 主题存储
            - 队列存储
        - 文件：
            - 消息内存存储
            - 消息统计信息
            - 垃圾信息
    - 消息转发todo
    - API
- 客户端：producer与consumer
    - 连接管理
    - 网络通信
- 公共：
    - rpc通信组件
    - 序列化与反序列化

# 2. Send message

1. 同步发送

   sendMessagesSYN，RPC同步invoke

2. oneway（单向消息）

   sendMessagesASYN，消息添加到队列，后台线程池异步发送（oneway形式）

3. 异步发送（回调）

   sendCallback，RPC的callback形式调用，RPC组件有一个专门线程池responseCallbackThreadPool处理回调结果

4. 发送事务消息

   sendMessagesInTransaction，参考rocketmq。首先发送半消息，发送成功后由transactionListener执行本地事务，根据半消息发送结果与本地事务执行状态

# 3. Pull message

消费者消费消息模式是拉模式。通过**多线程轮询**的方式实现

```java
for (PullMessageThread thread : consumerThreads) {
    clientThreadPool.execute(thread);
}
```

# 4. 事务消息

已经支持批量发送消息：`public static void send(List<Message> messages, boolean syn)(...)`

所以事务消息参考RocketMQ实现 ：本地事务+消息事务

1. 发送半消息，获取半消息发送的结果result，半消息不会存于真正主题的队列中，而是一个临时队列，不会被消费者消费
2. 若半消息成功发送，执行本地事务
3. 根据本地事务执行结果，判断此消息是否回滚，由于消息发送成功才会执行本地事务，因此不需考虑本地事务的回滚

本地事务在`TransactionListener`中执行