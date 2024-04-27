package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.ExactlyOnceProducerHelper;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class RabbitCustomProducer extends IProducer {

    private org.slf4j.Logger log = LoggerFactory.getLogger(RabbitCustomProducer.class);
    private ExactlyOnceProducerHelper exactlyOnceProducerHelper = new ExactlyOnceProducerHelper();
    private Channel channel;
    private ConnectionFactory factory;
    private RabbitConsumerSettings settings;
    private int numberOfMessages = 1;
    private final Object mutex = new Object();
    private long utilsID;

    public RabbitCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails, settings);
        this.settings = new RabbitConsumerSettings(settings);
        connect();
    }

    private void connect() {
        factory = settings.getRabbitSettings().getConnectionFactory();
        factory.setHost(this.getConnectionDetails().getAddress());

        // TODO needs to change the way.
        factory.setUsername("admin");
        factory.setPassword("admin");

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            // 0 is unlimited for both configurations.
            int prefetchSize = 0;
            int prefetchCount = 0;
            boolean global = true;

            if(settings.getQos() == 0){
                channel.basicQos(prefetchSize, prefetchCount, global);
            }else if(settings.getQos() == 1){
                prefetchCount = 10;
                channel.basicQos(prefetchSize, prefetchCount, global);
            }else if(settings.getQos() == 2){
                prefetchCount = 1;
                channel.basicQos(prefetchSize, prefetchCount, global);
            }

            // channel.queueDeclare(settings.getQueue(), false, false, false, null);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void produce(String topic, String message) {

        boolean confirmed = false;

        if (numberOfMessages == 1) {
            utilsID = Utils.initializeCounting();
        }

        numberOfMessages++;

        String queueName;

        // In case queue name is not referred, it will be produced to the same name as topic/queue as the producer.
        if (settings.getQueue().isEmpty()) {
            queueName = topicFromConsumer(topic);
        } else {
            queueName = settings.getQueue();
        }

        try {
            String messageId;
            if (settings.isRandomId()) {
                messageId = UUID.randomUUID().toString();
            } else {
                messageId = message;
            }

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .messageId(messageId)
                    .build();

            AtomicBoolean flag = new AtomicBoolean(true);

            if (settings.getQos() == 0) {
                produceMessage(queueName, properties, message);
            }else if(settings.getQos() == 1){

                channel.confirmSelect();

                channel.addConfirmListener((sequenceNumber, multiple) -> {
                    synchronized (this.mutex) {
                        flag.set(false);
                    }
                    }, (sequenceNumber, multiple) -> {
                    synchronized (this.mutex) {
                        flag.set(false);
                    }

                });

                int retries = 5;
                while(retries-- >= 0){
                    synchronized (this.mutex) {
                        if(flag.get()) {
                            produceMessage(queueName, properties, message);
                        }
                    }
                }
            } else {
                channel.confirmSelect();
                while (!confirmed) {
                    produceMessage(queueName, properties, message);
                    confirmed = channel.waitForConfirms();
                }
            }

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        if(this.numberOfMessages == 50000f || this.numberOfMessages == 25000f || this.numberOfMessages == 75000f){
            Utils.halfCounting(utilsID);
        }

        if (numberOfMessages == 100000) {
            Utils.pointReached(utilsID, log);
            numberOfMessages = 0;
        }
    }

    private void produceMessage(String queueName, AMQP.BasicProperties properties, String message) {

        try {
            channel.basicPublish("", queueName, properties, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }
}
