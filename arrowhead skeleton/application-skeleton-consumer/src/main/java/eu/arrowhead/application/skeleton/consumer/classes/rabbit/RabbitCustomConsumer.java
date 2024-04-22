package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.*;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.JavaRepository;
import org.apache.kafka.common.utils.Java;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitCustomConsumer extends IConsumer {

    private Channel channel;

    private final JavaRepository qosRepository = new JavaRepository();

    private ConnectionFactory factory;

    private final Logger logger = LogManager.getLogger(RabbitCustomConsumer.class);

    private RabbitProducerSettings settings;

    private String queueName;

    AMQP.Queue.DeclareOk queue;

    public RabbitCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        this.settings = new RabbitProducerSettings(settings);
    }

    public RabbitProducerSettings getSettings() {
        return settings;
    }

    @Override
    public void run() {
        connect();
    }

    private void connect() {
        factory = settings.getRabbitSettings().getConnectionFactory();
        factory.setHost(this.getConnectionDetails().getAddress());
        factory.setUsername("admin");
        factory.setPassword("admin");
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            logger.info("AMQP Consumer connected at " + getConnectionDetails().getAddress() + ":" + getConnectionDetails().getPort());
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {


            String message = new String(delivery.getBody(), "UTF-8");
            OnMessageReceived(settings.getRoutingKey(),message);
            lastMessage = message;
            numberOfMessages++;
        };

        try {

            if (!settings.getExchange().equals("")) {
                queueName = channel.queueDeclare().getQueue();
                channel.exchangeDeclare(settings.getExchange(), BuiltinExchangeType.DIRECT);
                channel.queueBind(queueName, settings.getExchange(), settings.getRoutingKey());
            } else {
                queueName = settings.getRoutingKey();
            }

            // queue = channel.queueDeclare(queueName, false, false, false, null);

            if (settings.getQos() == 0) {
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                });

            } else {
                channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException {
                        long deliveryTag = envelope.getDeliveryTag();

                        String messageId = properties.getMessageId();

                        if (settings.getQos() == 2) {
                            if (!qosRepository.messageExists(messageId)) {
                                qosRepository.registerNewMessage(messageId);
                                OnMessageReceived(settings.getRoutingKey(), new String(body, StandardCharsets.UTF_8));

                                lastMessage = new String(body, StandardCharsets.UTF_8);
                                numberOfMessages++;
                            }
                        } else {
                            OnMessageReceived(settings.getRoutingKey(), new String(body, StandardCharsets.UTF_8));

                            lastMessage = new String(body, StandardCharsets.UTF_8);
                            numberOfMessages++;
                        }

                        // positively acknowledge a single delivery, the message will
                        // be discarded
                        channel.basicAck(deliveryTag, false);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
