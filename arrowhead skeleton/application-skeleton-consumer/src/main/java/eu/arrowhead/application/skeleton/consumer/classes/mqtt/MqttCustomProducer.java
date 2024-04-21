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

    private Logger log = LoggerFactory.getLogger(MqttCustomProducer.class);
    private MqttClient client;
    private MqttSettings settings;
    private int numberOfMessages = 1;
    long thing = System.currentTimeMillis();
    private List<Integer> threadCount = new ArrayList<>();
    private List<Integer> threadPeakCount = new ArrayList<>();
    private List<String> heapUsage = new ArrayList<>();
    private List<String> nonHeapUsage = new ArrayList<>();
    private List<Integer> availableProcessors = new ArrayList<>();
    private List<Double> sysLoadAvg = new ArrayList<>();

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

        if (settings.getTopic().equals("")) {
            publishToTopic = this.topicFromConsumer(topic);
        } else {
            publishToTopic = settings.getTopic();
        }

        try {

            if (!client.isConnected())
                client.connect();
            // log.info("Publishing mqtt Message to " + cd + " at topic " + publishToTopic);
            // log.info("Message content - " + message + "\n");

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            MqttConnectOptions conn = new MqttConnectOptions();
            mqttMessage.setQos(settings.getQos());
            client.publish(publishToTopic,mqttMessage);


        } catch (MqttException e) {
            // throw new RuntimeException(e);
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
