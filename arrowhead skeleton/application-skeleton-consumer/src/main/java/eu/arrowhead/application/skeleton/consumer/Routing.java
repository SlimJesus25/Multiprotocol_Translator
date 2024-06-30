package eu.arrowhead.application.skeleton.consumer;

import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.dto.*;
import eu.arrowhead.application.skeleton.consumer.exceptions.ConsumerNotFoundException;
import eu.arrowhead.application.skeleton.consumer.exceptions.InvalidQoSException;
import eu.arrowhead.application.skeleton.consumer.exceptions.ProducerNotFoundException;
import eu.arrowhead.application.skeleton.consumer.exceptions.UnsupportedProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Ricardo Venâncio - 1210828
 **/

@RestController
@RequestMapping("/api")
public class Routing {

    private final Map<String, IProducer> producers = new ConcurrentHashMap<>();
    private final Object producersLock = new Object();

    private final Map<String, IConsumer> consumers = new ConcurrentHashMap<>();
    private final Object consumersLock = new Object();

    private final Map<String, ProducerResponseDTO> producersData = new ConcurrentHashMap<>();
    private final Object producersDataLock = new Object();

    private final Map<String, ConsumerResponseDTO> consumersData = new ConcurrentHashMap<>();
    private final Object consumersDataLock = new Object();

    private final Map<String, Thread> consumersThreads = new ConcurrentHashMap<>();
    private final Object consumersThreadsLock = new Object();

    private final AtomicInteger producerCount = new AtomicInteger();
    private final AtomicInteger consumerCount = new AtomicInteger();

    private final Logger logger = LogManager.getLogger(Routing.class);
    private final Object loggerLocker = new Object();

    @PostMapping("/addProducer")
    public ResponseEntity<ProducerResponseDTO> addProducer(@RequestBody ProducerRequestDTO req) {

        ConnectionDetails cd = new ConnectionDetails(req.getBrokerAddress(), Integer.parseInt(req.getBrokerPort()));

        verifyQoS(req.getQos());

        Map<String, String> settings = new HashMap<>();

        settings.put("qos", String.valueOf(req.getQos()));
        settings.put("client.id", "middleware-producer");
        String internalId = "producer-" + req.getProtocol() + "-" + producerCount.incrementAndGet();

        try {
            Constructor<?> c = producerProtocol(req.getProtocol());
            if(c.getName().contains("Rabbit"))
                settings.put("queue", req.getTopic());
            else
                settings.put("topic", req.getTopic());
            IProducer prod = (IProducer) c.newInstance(cd, settings);
            synchronized (producersLock) {
                producers.put(internalId, prod);
            }
        }catch(InvalidQoSException err) {
            synchronized (loggerLocker) {
                throw new UnsupportedProtocolException(req.getProtocol());
            }
        }catch (NullPointerException | InstantiationException | IllegalAccessException | InvocationTargetException ignore){
        }

        ProducerResponseDTO res = new ProducerResponseDTO(internalId, req.getBrokerAddress()
                , req.getBrokerPort(), req.getTopic(), req.getQos(), req.getProtocol());

        synchronized (producersDataLock) {
            producersData.put(internalId, res);
        }

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/addConsumer")
    public ResponseEntity<ConsumerResponseDTO> addConsumer(@RequestBody ConsumerRequestDTO req) {

        ConnectionDetails cd = new ConnectionDetails(req.getBrokerAddress(), Integer.parseInt(req.getBrokerPort()));

        verifyQoS(req.getQos());

        Map<String, String> settings = new HashMap<>();
        settings.put("qos", String.valueOf(req.getQos()));
        settings.put("client.id", "middleware-consumer");

        List<IProducer> linkedProducers = new ArrayList<>();
        for(String producer : req.getProducers()) {
            IProducer prod = findProducerByInternalID(producer);
            if(prod == null) {
                synchronized (loggerLocker) {
                    logger.error("Producer not found: {}", producer);
                }
                throw new ProducerNotFoundException(producer);
            }
            linkedProducers.add(prod);
        }

        String internalId = "consumer-" + req.getProtocol() + "-" + consumerCount.incrementAndGet();

        try {
            createConsumer(cd, req.getProtocol(), req.getTopic(), settings, internalId, linkedProducers);
        }catch(UnsupportedProtocolException err) {
            synchronized (loggerLocker) {
                logger.error("Unsupported protocol: {}", req.getProtocol());
            }
            throw new UnsupportedProtocolException(req.getProtocol());
        }

        ConsumerResponseDTO res = new ConsumerResponseDTO(internalId, req.getBrokerAddress(), req.getBrokerPort(),
                req.getTopic(), req.getQos(), req.getProtocol(), req.getProducers());

        synchronized (consumersDataLock) {
            consumersData.put(internalId, res);
        }

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/updateConsumerAssociations")
    public ResponseEntity<String> updateConsumerAssociations(@RequestBody UpdateConsumerAssociationsDTO req) {
        IConsumer consumer = findConsumerByInternalID(req.getConsumerId());
        if(consumer == null)
            throw new ConsumerNotFoundException(req.getConsumerId());

        List<IProducer> prodList;
        List<String> dtoProds;
        ConsumerResponseDTO data;

        synchronized (consumersDataLock) {
            prodList = consumer.producerList;
            dtoProds = consumersData.get(req.getConsumerId()).getProducers();
            data = consumersData.get(req.getConsumerId());
        }

        for(String prod : req.getNewProducers()) {
            IProducer p = findProducerByInternalID(prod);
            if(p != null) {
                prodList.add(p);
                dtoProds.add(prod);
            }else
                throw new ProducerNotFoundException(prod);
        }

        HashMap<String, String> settings = new HashMap<>();
        settings.put("qos", String.valueOf(data.getQos()));
        settings.put("client.id", "middleware-consumer");
        String topic = data.getTopic();
        String protocol = data.getProtocol();

        removeConsumer(req.getConsumerId());

        createConsumer(consumer.getConnectionDetails(), protocol, topic, settings
                , req.getConsumerId(), prodList);

        data.setProducers(dtoProds);

        synchronized (consumersDataLock) {
            consumersData.put(req.getConsumerId(), data);
        }
        return new ResponseEntity<>("Consumer was successfully updated!", HttpStatus.OK);
    }

    @GetMapping("/consumerInfo/{id}")
    public ResponseEntity<ConsumerResponseDTO> consumerInfo(@PathVariable String id){

        ConsumerResponseDTO res;
        synchronized (consumersDataLock) {
            res = consumersData.get(id);
        }

        if(res == null)
            throw new ConsumerNotFoundException(id);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/producerInfo/{id}")
    public ResponseEntity<ProducerResponseDTO> producerInfo(@PathVariable String id){

        ProducerResponseDTO res;
        synchronized (producersDataLock) {
            res = producersData.get(id);
        }

        if(res == null)
            throw new ProducerNotFoundException(id);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/consumersInfo")
    public ResponseEntity<List<ConsumerResponseDTO>> consumersInfo(){

        List<ConsumerResponseDTO> res = new ArrayList<>();
        synchronized (consumersDataLock) {
            consumersData.forEach((k, v) -> res.add(v));
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/producersInfo")
    public ResponseEntity<List<ProducerResponseDTO>> producersInfo(){

        List<ProducerResponseDTO> res = new ArrayList<>();
        synchronized (producersDataLock) {
            producersData.forEach((k, v) -> res.add(v));
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteProducer(@PathVariable String id) {

    }

    private IProducer findProducerByInternalID(String internalID){
        synchronized (producersLock) {
            return producers.get(internalID);
        }
    }

    private IConsumer findConsumerByInternalID(String internalID){
        synchronized (consumersLock) {
            return consumers.get(internalID);
        }
    }

    private Constructor<?> producerProtocol(String protocol) {
        return defineClass(protocol, true);
    }

    private Constructor<?> consumerProtocol(String protocol) {
        return defineClass(protocol, false);
    }

    private Constructor<?> defineClass(String protocol, boolean producer){
        try {
            switch (protocol) {
                case "dds":
                    return (producer)
                            ? Protocols.DDS_PRODUCER.getConstructor(ConnectionDetails.class, Map.class)
                            : Protocols.DDS_CONSUMER.getConstructor(ConnectionDetails.class, List.class, Map.class);
                case "kafka":
                    return (producer)
                            ? Protocols.KAFKA_PRODUCER.getConstructor(ConnectionDetails.class, Map.class)
                            : Protocols.KAFKA_CONSUMER.getConstructor(ConnectionDetails.class, List.class, Map.class);
                case "mqtt":
                    return (producer)
                            ? Protocols.MQTT_PRODUCER.getConstructor(ConnectionDetails.class, Map.class)
                            : Protocols.MQTT_CONSUMER.getConstructor(ConnectionDetails.class, List.class, Map.class);
                case "rabbitmq":
                    return (producer)
                            ? Protocols.RABBIT_PRODUCER.getConstructor(ConnectionDetails.class, Map.class)
                            : Protocols.RABBIT_CONSUMER.getConstructor(ConnectionDetails.class, List.class, Map.class);
                default:
                    throw new UnsupportedProtocolException(protocol);
            }
        }catch (NoSuchMethodException ignore){
        }
        return null;
    }

    private void createConsumer(ConnectionDetails cd, String protocol, String topic, Map<String, String> settings,
                                String internalId, List<IProducer> linkedProducers){
        try {

            Constructor<?> c = consumerProtocol(protocol);
            if(c.getName().contains("Rabbit"))
                settings.put("queue", topic);
            else
                settings.put("topic", topic);

            IConsumer cons = (IConsumer) c.newInstance(cd, linkedProducers, settings);
            synchronized (consumersLock) {
                consumers.put(internalId, cons);
            }
            Thread t = new Thread(cons);
            synchronized (consumersThreadsLock) {
                consumersThreads.put(internalId, t);
            }
            t.start();
        } catch (NullPointerException | InstantiationException | IllegalAccessException | InvocationTargetException ignore){
        }
    }

    private void removeConsumer(String internalID){
        synchronized (consumersThreadsLock) {
            consumersThreads.get(internalID).interrupt();
            consumersThreads.remove(internalID);
        }
        synchronized (consumersDataLock) {
            consumersData.remove(internalID);
        }
    }

    private void verifyQoS(int qos){
        if(qos < 0 || qos > 2)
            throw new InvalidQoSException(String.valueOf(qos));
    }
}
