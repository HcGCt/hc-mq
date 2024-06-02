# hc-mq

一个简单的轻量级消息队列，通信基于[hc-rpc](https://github.com/HcGCt/hc-rpc)，做这个项目是为了更好理解消息队列原理

## 目录

```
|—— hc-mq-common: 公共包，包括公共实体类、工具类、通信接口
|—— hc-mq-client: 客户端，包括生产者与消费者
	|—— client: 客户端远程调用相关
	|—— config: 客户端配置
	|—— consumer: 消费者，采用多线程轮询pull方式拉取消息
		|—— annotation: 注解标识消费者
		|—— thread: 消费线程
	|—— producer: 生产者
		|—— transaction: 事务监听器
		|—— Producers: push消息的相关接口
|—— hc-mq-server: 服务端
	|—— core
		|—— disk: 消息持久化逻辑，异步持久化
		|—— memo: 消息内存存储
		|—— replication: 多节点间消息复制逻辑
		|—— Broker: broker实例
		|—— MqServiceImpl: 通信接口实现类
|—— hc-mq-dashboard: 消息中心面板todo,内置注册中心
|—— hc-mq-test: demo与测试
```

## 设计

### 消息持久化

可指定消息是否持久化，消息持久化以队列为单位，主要两个文件：

- queue_data：消息二进制顺序写入

  消息结构如下

  ```
    4 字节				 30 字节
  +------------+--------------------------------+
  | 消息长度    |	       消息体二进制            |
  +------------+--------------------------------+
  ```

  消息之间直接相连，使用序列化转为Message对象，消息偏移量为**消息体起始、末尾偏移量**

- queue_stat：消息统计信息

### 消息生产者

- 同步发送

  `sendMessagesSYN`，RPC同步invoke

- oneway（单向消息）

  `sendMessagesASYN`，消息添加到队列，后台线程池异步发送（oneway形式）

- 异步发送（回调）

  `sendCallback`，RPC的callback形式调用，RPC组件有一个专门线程池responseCallbackThreadPool处理回调结果

- 发送事务消息

  `sendMessagesInTransaction`

  事务消息参考RocketMQ实现 ：本地事务+消息事务

  1. 发送半消息，获取半消息发送的结果result，半消息不会存于真正主题的队列中，而是一个临时队列，不会被消费者消费
  2. 若半消息成功发送，执行本地事务
  3. 根据本地事务执行结果，判断此消息是否回滚，由于消息发送成功才会执行本地事务，因此不需考虑本地事务的回滚

  本地事务在`TransactionListener`中执行

### 消息消费者

消费者消费消息模式本质上是拉模式。通过**多线程轮询**的方式实现。

```java
for (PullMessageThread thread : consumerThreads) {
    clientThreadPool.execute(thread);
}
```



## 使用

- 部署启动broker，导入client依赖

  broker支持水平扩展，可以提升消息系统容灾和可用性。

- 消费者

  实现`IConsumer`接口，`@Consumer`注解标识并指定topic即可，后台以轮询pull方式消费消息

  ```java
  @Consumer(topic = "testTopic1")
  public class DemoConsumer1 implements IConsumer {
      @Override
      public boolean consume(Message message) {
          logger.info("消费消息成功! 消息id: {}", message.getMsgId());
          return true;
      }
  }
  ```

- 生产者

  ```java
  public static void main(String[] args) throws Exception {
      // 启动客户端
      MqClientInitializer.getInstance().start();
      Message message = new Message("testTopic1", "msg_test".getBytes(StandardCharsets.UTF_8));
      // 支持:同步、回调、单向、事务消息发生
      // 同步
      Producers.send(message, true);
      // 单向
      Producers.send(message, false);	// 单向发送
      // 回调
      Producers.sendCallback(message, new SendCallback<SendResult>() {
                  @Override
                  public void onSuccess(SendResult result) {
  					// do something
                  }
                  @Override
                  public void onException(Throwable e) {
                      // do something
                  }
              });
      // 事务消息
      Producers.sendMessagesInTransaction(messages, new TransactionListener() {
          @Override
          public LocalTransactionState executeLocalTransaction(List<Message> msgs, Object arg) {
              System.out.println("执行本地事务中+++++++++++");
              return LocalTransactionState.COMMIT_MESSAGE;
          }
          @Override
          public LocalTransactionState checkLocalTransaction(Message msg) {
              return LocalTransactionState.COMMIT_MESSAGE;
          }
      }, null);
  }
  ```

## todo list

- [x] 消息生产模式
  - [x] 同步发送
  - [x] 回调发送
  - [x] 单向发送
  - [x] 事务消息
  - [ ] ......
- [x] 消息模式
  - [x] 串行消息
  - [ ] 延迟消息
  - [ ] 并行消息
  - [ ] 广播消息
- [x] 注册中心
  - [x] 内置注册中心
  - [x] zookeeper
  - [x] etcd
  - [ ] ......
- [x] 消息持久化
  - [x] 异步持久化
  - [ ] 同步持久化
- [ ] 失败重试
- [ ] 失败告警
- [ ] 事务消费
- [ ] 超时控制
- [ ] 消息中心控制台
- [ ] 消息可追踪
- [ ] 高性能/可用集群
- [ ] ......