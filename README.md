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
  | 消息长度    |	       消息体二进制			   |
  +------------+--------------------------------+
  ```

  消息之间直接相连，使用序列化转为Message对象，消息偏移量为**消息体起始、末尾偏移量**

- 消息统计信息queue_stat.txt

## 1.5 rpc通信

自定义rpc通信：

rpc组件设计如下：
![](https://cccblogimgs.oss-cn-hangzhou.aliyuncs.com/rpc%E6%9E%B6%E6%9E%84.png)

模块组成如下：

- loadbalance:负载均衡策略
    - ConsistentHashLoadBalancer：一致性哈希算法
    - RoundRobinLoadBalancer：轮询策略
- protocol:协议层，定义消息传输的格式等
    - codec
        - Decoder:解码器，根据消息个数解码，通过固定`ByteBuf`标记，解决半包粘包的问题，即要求一次性解码一个完整的消息，否则指针回退
        - Encoder:编码器，消息体分魔数、版本、消息状态等；消息体直接根据序列化器下序列化为字节数组
    - serialize：序列化为字节码，或者反序列化为对象
    - RpcMessage：网络传输消息，包括消息头与消息体
    - MsgHeader：消息体头
- invoker:服务调用方,异步调用TODO
    - RpcInvoker：Netty客户端监听及发送客户端请求
    - RpcInvokerProxy：客户端服务调用代理，服务调用基于代理模式，客户端使用动态代理对象调用请求的方法，此处编写调用方发起请求的流程：封装请求、负载均衡、重试机制、容错机制
    - RpcReferenceBean：服务调用方入口类，动态代理对象以便发起远程调用
    - RpcResponseHandler：Netty的ChannelHandler，执行接收响应的处理器逻辑，在编解码之后执行
- provider:服务提供方
    - RpcProviderFactory：服务提供工厂，负责启动Netty服务器，添加服务调用接口实现
    - ThreadPoolFactory：服务端线程池，执行请求处理逻辑
    - RpcRequestHandler：Netty的ChannelHandler，执行接收请求的的处理器逻辑，在编解码之后执行
- registry:注册中心
    - RedistryFactory：获取具体的注册中心
    - RedisRegistryCenter：Redis作为注册中心
    - LoaclRegistryCenter：本地注册中心
    - TODO
- SPI：允许在运行时动态地加载实现特定接口的类，而不需要在代码中显式地指定该类，从而实现解耦和灵活性。实现高效的组件化和模块化，提高组件的扩展性。（重要）
    - SpiLoader：基于类加载、反射机制
        - 加载实现类
        - 获取具体接口实例

## 1.6 模块划分

- 服务端：broker
    - 内存管理：
        - 队列管理
        - 消息管理
        - 确认/待确认消息管理
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
    - 通信协议
    - 序列化与反序列化

# 2. send message



# 3. pull message



# 4. 事务消息

已经支持批量发送消息：`public static void send(List<Message> messages, boolean syn)(...)`

所以事务消息参考RocketMQ实现 ：本地事务+消息事务

1. 发送半消息，获取半消息发送的结果result，半消息不会存于真正主题的队列中，而是一个临时队列，不会被消费者消费
2. 若半消息成功发送，执行本地事务
3. 根据本地事务执行结果，判断此消息是否回滚，由于消息发送成功才会执行本地事务，因此不需考虑本地事务的回滚

本地事务在`TransactionListener`中执行