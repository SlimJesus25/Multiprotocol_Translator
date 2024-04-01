package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.ExactlyOnceProducerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private long thing;

    private final Object mutex = new Object();

    private final Logger logger = LogManager.getLogger(RabbitCustomProducer.class);

    public RabbitCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails, settings);
        this.settings = new RabbitConsumerSettings(settings);
        connect();
    }

    private void connect() {
        factory = settings.getRabbitSettings().getConnectionFactory();
        factory.setHost(this.getConnectionDetails().getAddress());

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            // channel.queueDeclare(settings.getQueue(), false, false, false, null);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void produce(String topic, String message) {

        boolean confirmed = false;

        if (numberOfMessages == 1) {
            thing = System.currentTimeMillis();
        }

        numberOfMessages++;

        String queueName;

        // In case queue name is not referred, it will be produced to the same name as topic/queue as the producer.
        if (settings.getQueue().equals("")) {
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

            if(settings.getQos() == 1){

                channel.confirmSelect();

                channel.addConfirmListener((sequenceNumber, multiple) -> {
                    synchronized (this.mutex) {
                        logger.info("RabbitMQ: Mensage sent with QoS 1! " + Thread.currentThread().getName());
                        flag.set(false);
                    }
                    }, (sequenceNumber, multiple) -> {
                    synchronized (this.mutex) {
                        logger.info("RabbitMQ: Mensage NOT sent with QoS 1! " + Thread.currentThread().getName());
                        flag.set(false);
                    }

                });

                int retries = 5;
                while(retries-- >= 0){
                    synchronized (this.mutex) {
                        if(flag.get()) {
                            logger.info("Thread info: " + Thread.currentThread().getName());
                            produceMessage(queueName, properties, message);
                        }
                    }
                }

            }

            if (settings.getQos() == 0) {
                produceMessage(queueName, properties, message);
            } else {
                channel.confirmSelect();
                while (!confirmed) {
                    produceMessage(queueName,properties, message);
                    confirmed = channel.waitForConfirms();
                }
            }

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }




        if (numberOfMessages == 100000) {
            log.info("Messages per second + " + (100000f / ((System.currentTimeMillis() - thing) / 1000f)));
            numberOfMessages = 0;
        }
    }

    private void produceMessage(String queueName, AMQP.BasicProperties properties, String message) {

        try {
            channel.basicPublish("", queueName, properties, message.getBytes());

            // logger.info("Sent Rabbit message to " + this.getConnectionDetails() + "| Message - " + message + ", to default" +
                    // " exchange with routing key " + "\"" + settings.getQueue() + "\"");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }
}
