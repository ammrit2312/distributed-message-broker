package com.broker.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.broker.consumer.ConsumerGroupManager;
import com.broker.partition.Topic;

public class BrokerServer {

    public static final Logger logger = LoggerFactory.getLogger(BrokerServer.class);

    private volatile boolean running = true; // volatile makes sure that each thread doesnt cache this value in its memory as the value for this is changes by different threads
    private ServerSocket serverSocket;
    private final BrokerConfig config;
    private final DataSourceProvider databaseSource;
    private final ConsumerGroupManager consumerGroupManager;
    private final ExecutorService threadPool;
    private final Map<String, Topic> topics;

    private static final int DEFAULT_PARTITION_COUNT = 3;
    private static final String[] DEFAULT_TOPICS = {
        "events", "alerts", "metrics", "logs", "orders",
        "payments", "notifications", "audit", "analytics", "system"
    };

    public BrokerServer(BrokerConfig config) {
        this.config = config;
        this.databaseSource = new DataSourceProvider(config);
        this.consumerGroupManager = new ConsumerGroupManager(DEFAULT_PARTITION_COUNT);
        this.threadPool = Executors.newCachedThreadPool();
        this.topics = new ConcurrentHashMap<>();
        this.createDefaultTopics();
    }

    private void createDefaultTopics() {
        for (String topic : DEFAULT_TOPICS) {
            this.topics.put(topic, new Topic(topic, DEFAULT_PARTITION_COUNT));
            logger.info("Created topic '{}' with {} partitions", topic, DEFAULT_PARTITION_COUNT);
        }
    }

    public void start() throws IOException {
        MDC.put("brokerId", String.valueOf(config.getBrokerId()));

        this.serverSocket = new ServerSocket(config.getPort());
        registerShutdownHook();

        logger.info("Broker - {} started on port - {}", config.getBrokerId(), config.getPort());
        logger.info("Broker - {} is leader: {}", config.getBrokerId(), config.isLeader());

        while (running) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                logger.info("New connection from {}", clientSocket.getInetAddress());

                this.threadPool.submit(new ClientHandler(clientSocket, this)); // threads are created here 
            } catch (SocketException e) {
                if (running) {
                    throw e;
                }
                logger.info("Server socket closed, exiting accepting loop");
                break;
            }
        }
    }

    public Topic getTopic(String name) {
        return this.topics.get(name);
    }

    public DataSourceProvider getDataSourceProvider() {
        return this.databaseSource;
    }

    public ConsumerGroupManager getConsumerGroupManager() {
        return this.consumerGroupManager;
    }

    private void registerShutdownHook() { // this is run at the very end 
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "broker-shutdown"));
    }

    private void shutdown() {
        logger.info("Shutdown initiated, cleaning up resources...");
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }

        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        databaseSource.close();
        logger.info("Cleanup complete");
    }

    public static void main(String[] args) throws IOException {
        BrokerConfig config = new BrokerConfig();
        logger.info("Starting broker with config: {}", config);
        new BrokerServer(config).start();
    }
}
