package eu.arrowhead.application.skeleton.consumer.classes.mqtt;

import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Map;

public class MqttSettings extends PubSubSettings {

    private final static String CONNECTION_TIMEOUT = "connection.timeout";

    private final static String CLEAN_SESSION = "clean.session";

    private final static String AUTOMATIC_RECONNECT = "automatic.reconnect";

    private final static String KEEP_ALIVE_INTERNAL = "keep.alive.internal";

    private final static String MQTT_VERSION = "mqtt.version";

    private final static String EXECUTOR_SERVICE_TIMEOUT = "executor.service.timeout";

    private final static String HTTPS_HOSTNAME_VERIFICATION_ENABLED = "https.hostname.verification.enabled";

    private final static String MAX_IN_FLIGHT = "max.in.flight";

    private final static String MAX_RECONNECT_DELAY = "max.reconnect.delay";

    private final static String PASSWORD = "password";

    private final static String SERVER_URI = "server.uri";

    private final static String USERNAME = "user.name";

    private final MqttConnectOptions connectOptions = new MqttConnectOptions();

    public MqttSettings(Map<String,String> settingsMap) {
        super(settingsMap);

        if (settingsMap.containsKey(CONNECTION_TIMEOUT)) {
            connectOptions.setConnectionTimeout(Integer.parseInt(settingsMap.get(CONNECTION_TIMEOUT)));
        }

        if (settingsMap.containsKey(CLEAN_SESSION)) {
            connectOptions.setCleanSession(Boolean.parseBoolean(settingsMap.get(CLEAN_SESSION)));
        }

        if (settingsMap.containsKey(AUTOMATIC_RECONNECT)) {
            connectOptions.setAutomaticReconnect(Boolean.parseBoolean(settingsMap.get(AUTOMATIC_RECONNECT)));
        }

        if (settingsMap.containsKey(KEEP_ALIVE_INTERNAL)) {
            connectOptions.setKeepAliveInterval(Integer.parseInt(settingsMap.get(KEEP_ALIVE_INTERNAL)));
        }

        if (settingsMap.containsKey(MQTT_VERSION)) {
            connectOptions.setMqttVersion(Integer.parseInt(settingsMap.get(MQTT_VERSION)));
        }

        if (settingsMap.containsKey(EXECUTOR_SERVICE_TIMEOUT)) {
            connectOptions.setExecutorServiceTimeout(Integer.parseInt(settingsMap.get(EXECUTOR_SERVICE_TIMEOUT)));
        }

        if (settingsMap.containsKey(HTTPS_HOSTNAME_VERIFICATION_ENABLED)){
            connectOptions.setHttpsHostnameVerificationEnabled(Boolean.parseBoolean(settingsMap.get(HTTPS_HOSTNAME_VERIFICATION_ENABLED)));
        }

        if (settingsMap.containsKey(MAX_IN_FLIGHT)) {
            connectOptions.setMaxInflight(Integer.parseInt(settingsMap.get(MAX_IN_FLIGHT)));
        }

        if (settingsMap.containsKey(MAX_RECONNECT_DELAY)) {
            connectOptions.setMaxReconnectDelay(Integer.parseInt(settingsMap.get(MAX_RECONNECT_DELAY)));
        }

        if (settingsMap.containsKey(PASSWORD) && settingsMap.containsKey(USERNAME)) {
            connectOptions.setPassword(settingsMap.get(PASSWORD).toCharArray());
            connectOptions.setUserName(settingsMap.get(USERNAME));
        }

        if (settingsMap.containsKey(SERVER_URI)) {
            connectOptions.setServerURIs(new String[]{settingsMap.get(SERVER_URI)});
        }
    }

    public MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }
}
