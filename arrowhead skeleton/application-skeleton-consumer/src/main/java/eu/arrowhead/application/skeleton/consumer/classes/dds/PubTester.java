package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.Message;
import Messenger.MessageDataWriter;
import Messenger.MessageDataWriterHelper;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PubTester {

    private static final List<Message> messageBatch = new ArrayList<>();
    private static long lastSentTime = System.currentTimeMillis();

    private static int addToBatch(Message message, MessageDataWriter mdw, int handle){
        messageBatch.add(message);
        int size = 0;
        int batchSize = 1000;
        int batchTimeout = 2000;
        if(messageBatch.size() >= batchSize || (System.currentTimeMillis() - lastSentTime) >= batchTimeout){
            sendBatch(mdw, handle);
            lastSentTime = System.currentTimeMillis();
            size = messageBatch.size();
            messageBatch.clear();
        }
        return size;
    }

    private static void sendBatch(MessageDataWriter mdw, int handle){
        for(Message msg : messageBatch){

            int ret = RETCODE_TIMEOUT.value;
            while(ret == RETCODE_TIMEOUT.value) {
                ret = mdw.write(msg, handle);
            }
            if (ret != RETCODE_OK.value) {
                System.err.println("ERROR " + msg.count +
                        " write() returned " + ret);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException {

        args = new String[5];
        args[0] = "-DCPSBit";
        args[1] = "-DCPSConfigFile";
        args[2] = "-r";
        args[3] = "-w";
        args[4] = "-DCPSPendingTimeout";
        args = Utils.parseJSON("arrowhead skeleton/application-skeleton-consumer"
                + "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/arguments.json", args);

        String[] values = new String[4];
        values[0] = "qos";
        values[1] = "amount";
        values[2] = "topic";
        values[3] = "message";
        String[] finalValues = Utils.parseJSON("arrowhead skeleton/application-skeleton-consumer/src/main/java/eu/arrowhead/" +
                "application/skeleton/consumer/classes/dds/pubSubConf.json", values);

        long qos = Long.parseLong(finalValues[1]);
        long amount = Long.parseLong(finalValues[3]);
        String topic = finalValues[5];
        String message = finalValues[7];

        DataWriter dataWriter = DataInstantiation.instantiateDataWriter(args, topic, (int) qos, null);

        System.out.println("Publisher Created DataWriter");

        StatusCondition sc = dataWriter.get_statuscondition();
        sc.set_enabled_statuses(PUBLICATION_MATCHED_STATUS.value);
        WaitSet ws = new WaitSet();
        ws.attach_condition(sc);
        PublicationMatchedStatusHolder matched =
                new PublicationMatchedStatusHolder(new PublicationMatchedStatus());
        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);


        while (true) {
            final int result = dataWriter.get_publication_matched_status(matched);
            if (result != RETCODE_OK.value) {
                System.err.println("ERROR: get_publication_matched_status()" +
                        "failed.");
                return;
            }

            if (matched.value.current_count >= 1) {
                System.out.println("Publisher Matched");
                break;
            }

            ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
            if (ws.wait(cond, timeout) != RETCODE_OK.value) {
                System.err.println("ERROR: wait() failed.");
                return;
            }
        }

        ws.detach_condition(sc);
        MessageDataWriter mdw = MessageDataWriterHelper.narrow(dataWriter);
        Message msg = new Message();
        msg.subject_id = 1;
        int handle = mdw.register_instance(msg);
        msg.from = topic;
        msg.subject = topic;
        msg.text = message;
        msg.count = 1;
        int ret = RETCODE_TIMEOUT.value;



        if(qos > 0) {
            while (msg.count < amount) {
                if(msg.count % 100000 == 0)
                    System.out.println("Done %");
                msg.count += addToBatch(msg, mdw, handle);
            }
        }else{
            for (; msg.count < amount; ++msg.count) {
                while ((ret = mdw.write(msg, handle)) == RETCODE_TIMEOUT.value) {
                }
                if (ret != RETCODE_OK.value) {
                    System.err.println("ERROR " + msg.count +
                            " write() returned " + ret);
                }
            }
        }

        /*
        while (matched.value.current_count != 0) {
            final int result = mdw.get_publication_matched_status(matched);
            try {
                Thread.sleep(100);
            } catch(InterruptedException ie) {
            }
        }
        */

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Stop Publisher");
    }
}
