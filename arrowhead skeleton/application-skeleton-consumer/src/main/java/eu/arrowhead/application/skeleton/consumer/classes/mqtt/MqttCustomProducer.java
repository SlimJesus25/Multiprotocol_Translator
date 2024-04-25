package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MqttCustomProducer extends IProducer {

    private final Logger log = LoggerFactory.getLogger(MqttCustomProducer.class);
    private MqttClient client;
    private final MqttSettings settings;
    private int numberOfMessages = 1;
    long thing = System.currentTimeMillis();
    private final List<Integer> threadCount = new ArrayList<>();
    private final List<Integer> threadPeakCount = new ArrayList<>();
    private final List<String> heapUsage = new ArrayList<>();
    private final List<String> nonHeapUsage = new ArrayList<>();
    private final List<Integer> availableProcessors = new ArrayList<>();
    private final List<Double> sysLoadAvg = new ArrayList<>();

    public MqttCustomProducer(ConnectionDetails connectionDetails, Map<String,String> settings) {
        super(connectionDetails,settings);
        this.settings = new MqttSettings(settings);
        connect(connectionDetails.getAddress(),connectionDetails.getPort());
    }

    private void connect(String address, int port) {
        client = null;

        log.info("Connected to MQTT at " + address  + ":" + port);

        try {
            client = new MqttClient("tcp://" + address + ":" + port, settings.getClientId(), new MemoryPersistence());

            client.connect(settings.getConnectOptions());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void produce(String topic, String message) {


        if (numberOfMessages == 1) {
            Utils.threads(threadCount, threadPeakCount);
            Utils.memory(heapUsage, nonHeapUsage);

            thing = System.currentTimeMillis();
        }

        numberOfMessages++;

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
                dup = configureParameters(mqttMessage, conn, false, true);
            }else if(qos == 2){
                dup = configureParameters(mqttMessage, conn, false, true);
            }else{
                throw new RuntimeException("Invalid QoS level!");
            }

            if(((qos == 0 || qos == 2) && !dup) || qos == 1) {
                mqttMessage.setQos(qos);
                client.publish(publishToTopic, mqttMessage);
            }
        } catch (MqttException e) {
            log.warn("\n" + new RuntimeException(e) + "\n");
        }

        if(this.numberOfMessages == 50000f || this.numberOfMessages == 25000f || this.numberOfMessages == 75000f){
            Utils.threads(threadCount, threadPeakCount);
            Utils.memory(heapUsage, nonHeapUsage);
        }

        if (numberOfMessages == 100000) {
            long execTime = System.currentTimeMillis() - thing;
            log.info("Messages per second + " + (100000f / (execTime / 1000f)));
            log.info("Execution time: " + execTime / 1000f);
            Utils.cpu(availableProcessors, sysLoadAvg);
            Utils.cpuInfo(availableProcessors, sysLoadAvg, log);
            Utils.memoryInfo(heapUsage, nonHeapUsage, log);
            Utils.threadsInfo(threadCount, threadPeakCount, log);
            numberOfMessages = 0;
            clearLists();
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

    private void clearLists(){
        this.availableProcessors.clear();
        this.heapUsage.clear();
        this.nonHeapUsage.clear();
        this.threadCount.clear();
        this.threadPeakCount.clear();
        this.sysLoadAvg.clear();
    }

    /*"key.deserializer": "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer": "org.apache.kafka.common.serialization.StringDeserializer",
            "group.id": "mid"
            */

}
