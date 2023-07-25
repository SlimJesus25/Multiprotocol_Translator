package eu.arrowhead.application.skeleton.consumer.classes.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import eu.arrowhead.application.skeleton.consumer.classes.Settings;

import java.util.Map;

public class RabbitSettings {

    private final ConnectionFactory connectionFactory = new ConnectionFactory();

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String CONNECTION_TIMEOUT = "connection.timeout";

    public static final String AUTOMATIC_RECOVERY = "automatic.recovery";

    public static final String CHANNEL_RPC_TIMEOUT = "channel.rpc.timeout";

    public static final String CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE = "channel.should.check.rpc.response.type";

    public static final String HANDSHAKE_TIMEOUT = "handshake.timeout";

    public static final String NETWORK_RECOVERY_INTERVAL = "network.recovery.interval";

    public static final String REQUESTED_CHANNEL_MAX = "requested.channel.max";

    public static final String REQUESTED_FRAME_MAX = "requested.frame.max";

    public static final String REQUESTED_HEARTBEAT = "requested.heartbeat";

    public static final String SHUTDOWN_TIMEOUT = "shutdown.timeout";

    public static final String VIRTUAL_HOST = "virtual.host";

    public static final String WORK_POOL_TIMEOUT = "work.pool.timeout";

    public RabbitSettings(Map<String,String> settingsMap) {

        if (settingsMap.containsKey(USERNAME) && settingsMap.containsKey(PASSWORD)) {
            connectionFactory.setUsername(settingsMap.get(USERNAME));
            connectionFactory.setPassword(settingsMap.get(PASSWORD));
        }

        if (settingsMap.containsKey(CONNECTION_TIMEOUT)) {
            connectionFactory.setConnectionTimeout(Integer.parseInt(settingsMap.get(CONNECTION_TIMEOUT)));
        }

        if (settingsMap.containsKey(AUTOMATIC_RECOVERY)) {
            connectionFactory.setAutomaticRecoveryEnabled(Boolean.parseBoolean(settingsMap.get(AUTOMATIC_RECOVERY)));
        }

        if (settingsMap.containsKey(CHANNEL_RPC_TIMEOUT)) {
            connectionFactory.setChannelRpcTimeout(Integer.parseInt(settingsMap.get(CHANNEL_RPC_TIMEOUT)));
        }

        if (settingsMap.containsKey(CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE)) {
            connectionFactory.setChannelShouldCheckRpcResponseType(Boolean.parseBoolean(settingsMap.get(CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE)));
        }

        if (settingsMap.containsKey(HANDSHAKE_TIMEOUT)) {
            connectionFactory.setHandshakeTimeout(Integer.parseInt(settingsMap.get(HANDSHAKE_TIMEOUT)));
        }

        if (settingsMap.containsKey(NETWORK_RECOVERY_INTERVAL)) {
            connectionFactory.setNetworkRecoveryInterval(Integer.parseInt(settingsMap.get(NETWORK_RECOVERY_INTERVAL)));
        }

        if (settingsMap.containsKey(REQUESTED_CHANNEL_MAX)) {
            connectionFactory.setRequestedChannelMax(Integer.parseInt(settingsMap.get(REQUESTED_CHANNEL_MAX)));
        }

        if (settingsMap.containsKey(REQUESTED_FRAME_MAX)) {
            connectionFactory.setRequestedFrameMax(Integer.parseInt(settingsMap.get(REQUESTED_FRAME_MAX)));
        }

        if (settingsMap.containsKey(REQUESTED_HEARTBEAT)) {
            connectionFactory.setRequestedHeartbeat(Integer.parseInt(settingsMap.get(REQUESTED_HEARTBEAT)));
        }

        if (settingsMap.containsKey(SHUTDOWN_TIMEOUT)) {
            connectionFactory.setShutdownTimeout(Integer.parseInt(settingsMap.get(SHUTDOWN_TIMEOUT)));
        }

        if (settingsMap.containsKey(VIRTUAL_HOST)) {
            connectionFactory.setVirtualHost(settingsMap.get(VIRTUAL_HOST));
        }

        if (settingsMap.containsKey(WORK_POOL_TIMEOUT)) {
            connectionFactory.setWorkPoolTimeout(Integer.parseInt(settingsMap.get(WORK_POOL_TIMEOUT)));
        }
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
