package eu.arrowhead.application.skeleton.consumer;

import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.dto.*;
import eu.arrowhead.application.skeleton.consumer.exceptions.ConsumerNotFoundException;
import eu.arrowhead.application.skeleton.consumer.exceptions.InvalidQoSException;
import eu.arrowhead.application.skeleton.consumer.exceptions.ProducerNotFoundException;
import eu.arrowhead.application.skeleton.consumer.exceptions.UnsupportedProtocolException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * This class acts as Routing + Controller. It has all the endpoints (with respective documentation available on swagger
 * page) that the API offer.
 * It's notable to say that synchronization issues are handled here. For example, if two requests are made ate the same
 * time, it's guaranteed that there is not going to have any race condition due to the fine-grained mechanism
 * implemented.
 * @author : Ricardo Venâncio - 1210828
 **/

@Tag(name = "PolyglIoT API", description = "PolyglIoT's API endpoints")
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

    @Operation(
            summary = "Adds an internal producer to PolyglIoT.",
            description = "Adds an internal producer to the running PolyglIoT instance by specifying:\n" +
                    " - Broker Address - The broker address that this producer must produce data.\n" +
                    " - Broker Port - The broker port that this producer must produce data.\n" +
                    " - Topic - The topic that this producer must produce data.\n" +
                    " - QoS - Quality of Service that this producer should produce (must be between 0 and 2).\n" +
                    " - Protocol - Communication protocol that this producer should produce (must be either 'mqtt', 'dds', 'rabbitmq' or 'kafka').\n" ,
            tags = { "Producer", "POST" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProducerResponseDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
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

    @Operation(
            summary = "Adds an internal consumer to PolyglIoT.",
            description = "Adds an internal consumer to the running PolyglIoT instance by specifying:\n" +
                    " - Broker Address - The broker address that this producer must produce data.\n" +
                    " - Broker Port - The broker port that this producer must produce data.\n" +
                    " - Topic - The topic that this producer must produce data.\n" +
                    " - QoS - Quality of Service that this producer should produce (must be between 0 and 2).\n" +
                    " - Protocol - Communication protocol that this producer should produce (must be either 'mqtt', " +
                    "'dds', 'rabbitmq' or 'kafka').\n" +
                    " - Producers - List of producers internal IDs (previously created) that should be linked to this " +
                    "consumer. Note that the internal ID is always returned upon a producer/consumer successfull creation." ,
            tags = { "Consumer", "POST" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ConsumerResponseDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
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

    @Operation(
            summary = "Updates producers associations from a consumer.",
            description = "Updates the producers that the specified consumer must redirect messages. The following attributes:\n" +
                    " - consumerId - Internal ID from the desired consumer.\n" +
                    " - keepOldProducers - It either maintains the previously associated producers or remove them.\n" +
                    " - newProducers - List of new producers to link to this consumer.\n" ,
            tags = { "Consumer", "PUT" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = UpdateConsumerAssociationsDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @PutMapping("/updateConsumerAssociations")
    public ResponseEntity<String> updateConsumerAssociations(@RequestBody UpdateConsumerAssociationsDTO req) {
        IConsumer consumer = findConsumerByInternalID(req.getConsumerId());
        if(consumer == null)
            throw new ConsumerNotFoundException(req.getConsumerId());

        List<IProducer> prodList;
        List<String> dtoProds;
        ConsumerResponseDTO data;

        synchronized (consumersDataLock) {
            prodList = req.keepOldProducers() ? consumer.producerList : new ArrayList<>();
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


    @Operation(
            summary = "Grabs specific information about a consumer.",
            description = "Retrieves specific information about a previously created consumer. The following attribute: \n" +
                    " - id - Internal ID from the consumer intended to search." ,
            tags = { "Consumer", "GET" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ConsumerResponseDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
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

    @Operation(
            summary = "Grabs specific information about a producer.",
            description = "Retrieves specific information about a previously created producer. The following attribute: \n" +
                    " - id - Internal ID from the producer intended to search." ,
            tags = { "Producer", "GET" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProducerResponseDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
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

    @Operation(
            summary = "Grabs specific information about all consumers.",
            description = "Retrieves specific information about all previously created consumers." ,
            tags = { "Consumer", "GET" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ConsumerResponseDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/consumersInfo")
    public ResponseEntity<List<ConsumerResponseDTO>> consumersInfo(){

        List<ConsumerResponseDTO> res = new ArrayList<>();
        synchronized (consumersDataLock) {
            consumersData.forEach((k, v) -> res.add(v));
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Operation(
            summary = "Grabs specific information about all producers.",
            description = "Retrieves specific information about all previously created producers." ,
            tags = { "Producers", "GET" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProducerResponseDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/producersInfo")
    public ResponseEntity<List<ProducerResponseDTO>> producersInfo(){

        List<ProducerResponseDTO> res = new ArrayList<>();
        synchronized (producersDataLock) {
            producersData.forEach((k, v) -> res.add(v));
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProducer/{id}")
    public ResponseEntity<ProducerResponseDTO> deleteProducer(@PathVariable String id) {

        ProducerResponseDTO res;

        synchronized (producersLock){
            if(producers.remove(id) == null)
                throw new ProducerNotFoundException(id);
        }

        synchronized (producersDataLock){
            res = producersData.remove(id);
        }

        producerCount.decrementAndGet();

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/deleteConsumer/{id}")
    public ResponseEntity<ConsumerResponseDTO> deleteConsumer(@PathVariable String id) {

        ConsumerResponseDTO res;

        synchronized (consumersDataLock) {
            res = consumersData.get(id);
        }

        if(res == null)
            throw new ConsumerNotFoundException(id);

        removeConsumer(id);

        return new ResponseEntity<>(res, HttpStatus.OK);
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
        consumerCount.decrementAndGet();
    }

    private void verifyQoS(int qos){
        if(qos < Protocols.MIN_QOS_ALLOWED || qos > Protocols.MAX_QOS_ALLOWED)
            throw new InvalidQoSException(String.valueOf(qos));
    }
}
