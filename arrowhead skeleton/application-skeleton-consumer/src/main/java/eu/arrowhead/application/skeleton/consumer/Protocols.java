package eu.arrowhead.application.skeleton.consumer;

import eu.arrowhead.application.skeleton.consumer.classes.dds.DDSCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.dds.DDSCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomConsumer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;

/**
 * Supported protocols by PolyglIoT. It has constants with the respective classes.
 * @author : Ricardo Venâncio - 1210828
 */
public class Protocols {
    public final static Class<DDSCustomProducer> DDS_PRODUCER = DDSCustomProducer.class;
    public final static Class<DDSCustomConsumer> DDS_CONSUMER = DDSCustomConsumer.class;

    public final static Class<MqttCustomConsumer> MQTT_CONSUMER = MqttCustomConsumer.class;
    public final static Class<MqttCustomProducer> MQTT_PRODUCER = MqttCustomProducer.class;

    public final static Class<KafkaCustomConsumer> KAFKA_CONSUMER = KafkaCustomConsumer.class;
    public final static Class<KafkaCustomProducer> KAFKA_PRODUCER = KafkaCustomProducer.class;

    public final static Class<RabbitCustomConsumer> RABBIT_CONSUMER = RabbitCustomConsumer.class;
    public final static Class<RabbitCustomProducer> RABBIT_PRODUCER = RabbitCustomProducer.class;
}
