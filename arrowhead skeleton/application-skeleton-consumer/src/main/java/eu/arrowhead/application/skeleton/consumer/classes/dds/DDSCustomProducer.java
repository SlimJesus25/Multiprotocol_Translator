package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.Message;
import Messenger.MessageDataWriter;
import Messenger.MessageDataWriterHelper;
import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomProducer extends IProducer {

    private final PubSubSettings settings;
    private DataWriter dataWriter;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(DDSCustomProducer.class);
    private long utilsID;
    private int numberOfMessages = 0;
    private String[] args;
    private final String topic;
    private int count;
    private final List<Message> messageBatch = new ArrayList<>();
    private long lastSentTime = System.currentTimeMillis();
    private boolean first = false;
    private boolean quarter = false;
    private boolean half = false;
    private boolean threeQuarters = false;
    private final boolean[] arr = new boolean[3];

    public DDSCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
        this.settings = new PubSubSettings(settings);
        count = 1;

        Arrays.fill(arr, true);

        String[] conf = new String[5];
        conf[0] = "-DCPSBit";
        conf[1] = "-DCPSConfigFile";
        conf[2] = "-r";
        conf[3] = "-w";
        conf[4] = "-DCPSPendingTimeout";

        try {
            String pathToJSON = "arrowhead skeleton/application-skeleton-consumer" +
                    "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/arguments.json";
            this.args = Utils.parseJSON(pathToJSON,
                    conf);
        } catch (IOException | ParseException e){
            log.error("Error parsing DDS Custom Producer arguments JSON file.");
            System.exit(1);
        }

        this.topic = settings.get("topic");
        createProducer(settings.get("topic"));
    }

    private void createProducer(String topic){
        this.dataWriter = DataInstantiation.instantiateDataWriter(this.args, topic, this.settings.getQos(), this.log);
    }

    @Override
    public void produce(String topic, String message) {

            if (numberOfMessages == 1 || first) {
                utilsID = Utils.initializeCounting();
                first = false;
            }

            numberOfMessages++;

            topic = this.topic;
            StatusCondition sc = this.dataWriter.get_statuscondition();
            sc.set_enabled_statuses(PUBLICATION_MATCHED_STATUS.value);
            WaitSet ws = new WaitSet();
            ws.attach_condition(sc);
            PublicationMatchedStatusHolder matched =
                    new PublicationMatchedStatusHolder(new PublicationMatchedStatus());
            Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                    DURATION_INFINITE_NSEC.value);


            while (true) {
                final int result = this.dataWriter.get_publication_matched_status(matched);
                if (result != RETCODE_OK.value) {
                    System.err.println("ERROR: get_publication_matched_status()" +
                            "failed.");
                    return;
                }

                if (matched.value.current_count >= 1) {
                    // System.out.println("Publisher Matched");
                    break;
                }

                ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
                if (ws.wait(cond, timeout) != RETCODE_OK.value) {
                    System.err.println("ERROR: wait() failed.");
                    return;
                }
            }

            ws.detach_condition(sc);
            MessageDataWriter mdw = MessageDataWriterHelper.narrow(this.dataWriter);
            Message msg = new Message();
            msg.subject_id = this.count;
            int handle = mdw.register_instance(msg);
            msg.from = this.settings.getClientId();
            msg.subject = topic;
            msg.text = message;
            msg.count = 1;
            this.count = this.numberOfMessages;
            int ret = RETCODE_TIMEOUT.value;


            addToBatch(msg, mdw, handle);
            /*
            for (; msg.count < amount; ++msg.count) {
                while ((ret = mdw.write(msg, handle)) == RETCODE_TIMEOUT.value) {
                }
                if (ret != RETCODE_OK.value) {
                    System.err.println("ERROR " + msg.count +
                            " write() returned " + ret);
                }
            }
             */

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

    private void addToBatch(Message message, MessageDataWriter mdw, int handle){
        messageBatch.add(message);
        int batchSize = 100;
        int batchTimeout = 2000;
        if(messageBatch.size() >= batchSize || (System.currentTimeMillis() - lastSentTime) >= batchTimeout){
            sendBatch(mdw, handle);
            lastSentTime = System.currentTimeMillis();
            messageBatch.clear();
        }
    }

    private void sendBatch(MessageDataWriter mdw, int handle){
        for(Message msg : messageBatch){
            mdw.write(msg, handle);
        }
    }
}
