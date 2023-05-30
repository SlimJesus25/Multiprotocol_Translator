package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.*;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitCustomConsumer extends IConsumer {

    private Channel channel;

    private ConnectionFactory factory;

    private RabbitLoadBalancerMonitor loadBalancer;

    private final Logger logger = LogManager.getLogger(RabbitCustomConsumer.class);

    private RabbitSettings settings;

    private String queueName;

    AMQP.Queue.DeclareOk queue;

    private boolean loadBalanceMode;

    public RabbitCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        this.loadBalanceMode = true;
        this.settings = new RabbitSettings(settings);
    }

    public RabbitCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, RabbitSettings settings, boolean load) {
        super(connectionDetails, producer, new HashMap<>());
        this.settings = settings;
        this.loadBalanceMode = false;
    }

    public RabbitSettings getSettings() {
        return settings;
    }

    public RabbitCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, RabbitSettings settings) {
        super(connectionDetails,producer, new HashMap<>());
        this.settings = settings;
    }

    @Override
    public void run() {
        connect();
    }

    private void connect() {
        factory = new ConnectionFactory();
        factory.setHost(this.getConnectionDetails().getAddress());
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();


        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            // logger.info("Queue - " + queue.getConsumerCount());


            String message = new String(delivery.getBody(), "UTF-8");
            // logger.info(" Message Received on rabbit consumer '" + message + "'");
            OnMessageReceived(settings.getQueue(),message);
        };

        try {


            if (!settings.getExchange().equals("")) {
                queueName = channel.queueDeclare().getQueue();
                channel.exchangeDeclare(settings.getExchange(), BuiltinExchangeType.DIRECT);
                channel.queueBind(queueName, settings.getExchange(), settings.getRoutingKey());
            } else {
                queueName = settings.getRoutingKey();
            }

            queue = channel.queueDeclare(queueName, false, false, false, null);

            int consumerCount = queue.getConsumerCount();
            int messageCount = queue.getMessageCount();

            // If there are more than maxMessages messages in the queue, and less than maxConsumers consumers, create a new consumer
            if (messageCount > 70000 && consumerCount < 5) {
                // Create a new RabbitConsumer with the same configuration and start consuming messages
                RabbitCustomConsumer newConsumer = new RabbitCustomConsumer(getConnectionDetails(),producerList,settings);
                Thread thread = new Thread(newConsumer);
                thread.start();
            }

            /*
            if (loadBalanceMode) {
                loadBalancer = new RabbitLoadBalancerMonitor(queue);
                loadBalancer.addConsumer(this);
                new Thread(loadBalancer).start();
            }
            */


            /*logger.warn("Queue name - " + settings.getQueue());
            AMQP.Queue.DeclareOk queue = channel.queueDeclare(settings.getQueue(), false, false, false, null);
            channel.queueBind(queue.getQueue(), "", settings.getQueue());*/

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
                            throws IOException
                    {
                        long deliveryTag = envelope.getDeliveryTag();

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
