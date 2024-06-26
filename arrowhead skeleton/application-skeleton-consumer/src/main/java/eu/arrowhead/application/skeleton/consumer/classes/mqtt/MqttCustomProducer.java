package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class MqttCustomProducer extends IProducer {

    private final Logger log = LoggerFactory.getLogger(MqttCustomProducer.class);
    private MqttClient client;
    private final MqttSettings settings;
    private int numberOfMessages = 1;
    private long utilsID;
    private boolean first = true;
    private boolean quarter = true;
    private boolean half = true;
    private boolean threeQuarters = true;
    private final boolean[] arr = new boolean[3];

    public MqttCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails,settings);
        this.settings = new MqttSettings(settings);
        Arrays.fill(arr, true);
        connect(connectionDetails.getAddress(),connectionDetails.getPort());
    }

    private void connect(String address, int port) {
        client = null;

        log.info("Connected to MQTT at {}:{}", address, port);

        try {
            client = new MqttClient("tcp://" + address + ":" + port, settings.getClientId(), new MemoryPersistence());

            client.connect(settings.getConnectOptions());

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    log.warn("Connection lost: {}", throwable.getMessage());
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    if(!iMqttDeliveryToken.isComplete())
                        log.warn("Message not delivered");
                    else
                        numberOfMessages++;

                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void produce(String topic, String message) {

        if (numberOfMessages == 1 && first) {
            utilsID = Utils.initializeCounting();
            first = false;
        }

        String publishToTopic;

        if (settings.getTopic().isEmpty()) {
            publishToTopic = this.topicFromConsumer(topic);
        } else {
            publishToTopic = settings.getTopic();
        }

        try {

            if (!client.isConnected())
                client.connect();

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            MqttConnectOptions conn = new MqttConnectOptions();
            int qos = settings.getQos();
            boolean dup;

            if(qos == 0){
                dup = configureParameters(mqttMessage, conn, true, false);
            }else if(qos == 1){
                dup = configureParameters(mqttMessage, conn, true, true);
            }else{
                dup = configureParameters(mqttMessage, conn, false, true);
            }

            if(((qos == 0 || qos == 2) && !dup) || qos == 1) {
                mqttMessage.setQos(qos);
                client.publish(publishToTopic, mqttMessage);
            }

            if(qos == 0)
                numberOfMessages++;


        } catch (MqttException e) {
            log.warn("\n" + new RuntimeException(e) + "\n");
        }

        Utils.checkValue(this.numberOfMessages, quarter, half, threeQuarters, arr, utilsID);

        quarter = arr[0];
        half = arr[1];
        threeQuarters = arr[2];

        if (numberOfMessages >= 100000) {
            Utils.pointReached(utilsID, log);
            this.numberOfMessages -= 100000;
            first = true;
            quarter = true;
            half = true;
            threeQuarters = true;
            Arrays.fill(arr, true);
        }
    }

    private boolean configureParameters(MqttMessage mqttMessage, MqttConnectOptions conn, boolean cleanSession, boolean retained){
        mqttMessage.setRetained(retained);
        conn.setCleanSession(cleanSession);
        return mqttMessage.isDuplicate();
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public MqttClient getClient() {
        return client;
    }

}
