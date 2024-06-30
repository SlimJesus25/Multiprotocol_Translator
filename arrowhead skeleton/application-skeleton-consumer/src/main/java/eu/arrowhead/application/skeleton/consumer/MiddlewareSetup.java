package eu.arrowhead.application.skeleton.consumer;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.dds.DDSCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer;
import eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer;
import eu.arrowhead.common.dto.shared.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;

@SpringBootApplication
@Component
public class MiddlewareSetup implements Runnable {

    private Map<String,String> consumerMap = new HashMap<>();
    private Map<String,String> producerMap = new HashMap<>();
    private List<IConsumer> consumerInstances = new ArrayList<>();
    private Map<String,ConnectionDetails> providers = new HashMap<>();
    private final Logger logger = LogManager.getLogger(MiddlewareSetup.class);
    private String externalPropertiesFile = "../resources/properties.json";
    private String externalGeneralPropertiesFile = "../resources/general_properties.json";
    private File propertiesFile = new File(externalPropertiesFile);
    private File generalPropertiesFile = new File(externalGeneralPropertiesFile);
    private ArrowheadService service;

    public MiddlewareSetup(ArrowheadService service) {
        this.service = service;
    }

    @Override
    public void run() {
        try {
            if(!initializationStart()) {
                createMaps();
                loadDefaultBrokers();
                loadProperties();
                startInternalConsumers();
            }
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initializationStart() throws IOException {

        InputStream inputStream;

        if (generalPropertiesFile.exists()) {
            inputStream = Files.newInputStream(generalPropertiesFile.toPath());
            logger.info("Loading general from external properties");
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream("general_properties.json");
        }

        JSONObject jo = new JSONObject(new JSONTokener(inputStream));

        return jo.getBoolean("flexible_api");
    }


    /**
     * Creates the mapping between broker name and Consumer / Producer class to use.
     * This way, whenever the application is parsing through the properties file, it knows which type to create
     */

    private void createMaps() {
        logger.info("Loading initial configs");

        consumerMap.put("mqtt","eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomConsumer");
        consumerMap.put("teste","eu.arrowhead.application.skeleton.consumer.classes.testServices.PeriodicConsumer");
        consumerMap.put("kafka","eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomConsumer");
        consumerMap.put("rabbit","eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomConsumer");
        consumerMap.put("dds", "eu.arrowhead.application.skeleton.consumer.classes.dds.DDSCustomConsumer");

        producerMap.put("mqtt","eu.arrowhead.application.skeleton.consumer.classes.mqtt.MqttCustomProducer");
        producerMap.put("teste","eu.arrowhead.application.skeleton.consumer.classes.testServices.ConsoleProducer");
        producerMap.put("kafka","eu.arrowhead.application.skeleton.consumer.classes.kafka.KafkaCustomProducer");
        producerMap.put("rabbit","eu.arrowhead.application.skeleton.consumer.classes.rabbit.RabbitCustomProducer");
        producerMap.put("dds", "eu.arrowhead.application.skeleton.consumer.classes.dds.DDSCustomProducer");
    }

    /**
     * Loads brokers from default configuration in "general_properties"
     * @throws FileNotFoundException
     * @throws JSONException
     */
    private void loadDefaultBrokers() throws IOException, JSONException {
        InputStream inputStream;

        if (generalPropertiesFile.exists()) {
            inputStream = Files.newInputStream(generalPropertiesFile.toPath());
            logger.info("Loading general from external properties");
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream("general_properties.json");
        }

        JSONObject jo = new JSONObject(new JSONTokener(inputStream));
        JSONObject defaultBrokers = jo.getJSONObject("default_brokers");

        if (!jo.getBoolean("arrowhead_enabled")) {
            for (Iterator it = defaultBrokers.keys(); it.hasNext();) {
                String o = (String) it.next();

                providers.put(o, new ConnectionDetails(defaultBrokers.getJSONObject(o).getString("address"),
                        defaultBrokers.getJSONObject(o).getInt("port")));
            }
        }
    }

    public ConnectionDetails loadBroker(String broker) throws IOException, JSONException {

        InputStream inputStream;

        if (generalPropertiesFile.exists()) {
            inputStream = Files.newInputStream(generalPropertiesFile.toPath());
            logger.info("Loading general from external properties");
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream("general_properties.json");
        }

        JSONObject jo = new JSONObject(new JSONTokener(inputStream));
        JSONObject defaultBrokers = jo.getJSONObject("default_brokers");

        return new ConnectionDetails(defaultBrokers.getJSONObject(broker).getString("address"),
                defaultBrokers.getJSONObject(broker).getInt("port"));
    }

    /**
     * Creates a new Producer instance based on properties read from the configuration file.
     * This method is only called by the loadProperties() method
     *
     * @param cd the connection details (address and port) for the broker of this producer
     * @param name the name of the broker of this producer, as present in the "producerMap"
     * @param settings the .props field, to be parsed and processed depending on the Producer's type
     * @return the new producer instance
     */
    private IProducer createProducer(ConnectionDetails cd, String name, Map<String,String> settings) {
        Class<?> c;
        try {
            c = Class.forName(producerMap.get(name));
            Constructor<?> cons = c.getConstructor(ConnectionDetails.class, Map.class);
            return (IProducer) cons.newInstance(cd, settings);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Producer instance based on properties read from the configuration file.
     *
     * This method is only called by the loadProperties() method
     *
     * @param cd the connection details (address and port) for the broker of this consumer
     * @param producer list of producer instances this consumer will signal to produce whenever they receive a new message
     * @param name the name of the broker of this consumer, as present in the "consumerMap"
     * @param settings the .props field, to be parsed and processed depending on the Consumer's type
     * @return the new consumer instance
     */

    private IConsumer createConsumer(ConnectionDetails cd, List<IProducer> producer, String name, Map<String, String> settings) {
        Class<?> c;
        try {
            c = Class.forName(consumerMap.get(name));
            Constructor<?> cons = c.getConstructor(ConnectionDetails.class, List.class, Map.class);
            return (IConsumer) cons.newInstance(cd,producer,settings);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void startInternalConsumers() {
        for (IConsumer consumer : consumerInstances) {
            new Thread(consumer).start();
        }
    }

    private void loadProperties() throws IOException, JSONException {
        logger.info("Loading properties");
        InputStream inputStream;

        if (propertiesFile.exists()) {
            inputStream = Files.newInputStream(propertiesFile.toPath());
            logger.info("Loading devices from external properties");
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream("properties.json");
        }

        JSONObject jo = new JSONObject(new JSONTokener(inputStream));
        Map<String, IProducer> producerMap = new HashMap<>();
        Map<String, IConsumer> consumerMap = new HashMap<>();

        // Create producers for the client's consumers
        JSONArray clientConsumers = jo.getJSONArray("Consumers");
        logger.info("Loading client consumers");

        for (int i = 0; i < clientConsumers.length(); i++) {
            JSONObject consumer = clientConsumers.getJSONObject(i);
            String id = consumer.getString("internal.id");
            String protocol = consumer.getString("protocol");
            Map<String, String> props = new HashMap<>();
            JSONObject settings = consumer.getJSONObject("additional.props");

            for (Iterator it = settings.keys(); it.hasNext(); ) {
                String o = (String) it.next();
                props.put(o, String.valueOf(settings.get(o)));
            }

            if (!providers.containsKey(protocol)) {
                ConnectionDetails cd = requestConnectionDetails(protocol);
                providers.put(protocol,cd);
            }

            producerMap.put(id,createProducer(providers.get(protocol),protocol,props));
        }

        // Create consumers for the client's producers
        JSONArray clientProducers = jo.getJSONArray("Producers");
        logger.info("Loading client producers");

        for (int i = 0; i < clientProducers.length(); i++) {
            JSONObject producer = clientProducers.getJSONObject(i);
            String id = producer.getString("internal.id");
            String protocol = producer.getString("protocol");
            Map<String, String> props = new HashMap<>();
            JSONObject settings = producer.getJSONObject("additional.props");

            for (Iterator it = settings.keys(); it.hasNext(); ) {
                String o = (String) it.next();
                props.put(o, String.valueOf(settings.get(o)));
            }
            if (!providers.containsKey(protocol)) {
                ConnectionDetails cd = requestConnectionDetails(protocol);
                providers.put(protocol,cd);
            }
            consumerMap.put(id,createConsumer(providers.get(protocol),new ArrayList<IProducer>(),protocol,props));
        }

        // Link the created consumers and producers
        JSONArray streamsArray = jo.getJSONArray("Streams");
        logger.info("Loading device streams");

        for (int i = 0 ; i < streamsArray.length(); i++) {

            JSONObject stream = streamsArray.getJSONObject(i);
            String producers = stream.getString("from.producers");
            String consumers = stream.getString("to.consumers");

            for (String consumerAux : producers.split(",")) {
                IConsumer consumer = consumerMap.get(consumerAux);
                if (!consumerInstances.contains(consumer))
                    consumerInstances.add(consumer);

                for (String producerAux : consumers.split(",")) {
                    IProducer producer = producerMap.get(producerAux);
                    consumer.linkProducer(producer);
                }
            }
        }

    }


    private ConnectionDetails requestConnectionDetails(String requestedService) throws IOException, JSONException {

        try {

            logger.info("Requesting arrowhead for a provider of \"" + requestedService + "\"");

            // Irrelevant if running in local mode (no security)

            SystemRequestDTO dto = new SystemRequestDTO();
            dto.setAddress("127.0.0.1");
            dto.setSystemName("system");
            dto.setPort(1234);
            dto.setAuthenticationInfo("string");

            OrchestrationFormRequestDTO.Builder orchestrationFormBuilder = new OrchestrationFormRequestDTO.Builder(dto);

            orchestrationFormBuilder.flag(OrchestrationFlags.Flag.EXTERNAL_SERVICE_REQUEST, true);

            ServiceQueryFormDTO.Builder serviceQueryFormBuilder = new ServiceQueryFormDTO.Builder(requestedService);

            orchestrationFormBuilder.requestedService(serviceQueryFormBuilder.build());

            OrchestrationResponseDTO response = service.proceedOrchestration(orchestrationFormBuilder.build());

            // Can return multiple providers but that's up to the user to guarantee it doesn't happen.
            // We select the 1st
            System.out.println(response.toString());

            ConnectionDetails cd = new ConnectionDetails(response.getResponse().get(0).getProvider().getAddress(),
                    response.getResponse().get(0).getProvider().getPort());



            logger.info("\nFound provider for service \"" + requestedService + "\" at \n" + cd + "\n");

            return cd;
        } catch (Exception ex) {
            logger.warn("Failed to use Arrowhead to get connection details for the \"" + requestedService + "\" protocol Broker. Using defaults instead");

            return loadBroker(requestedService);
        }
    }
}
