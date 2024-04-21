package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.omg.CORBA.StringSeqHolder;
import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class DDSCustomConsumer extends IConsumer {

    private final String topic;
    private final int qos;
    private int count;
    private String[] args;

    public DDSCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        this.topic = settings.get("topic");
        this.qos = Integer.parseInt(settings.get("qos"));
        args = new String[6];
        args[0] = "-DCPSBit";
        args[1] = "0";
        args[2] = "-DCPSConfigFile";
        args[3] = "/home/ricardo/IdeaProjects/Multiprotocol_Translator/arrowhead skeleton/application-skeleton-consumer" +
                "/src/main/java/eu/arrowhead/application/skeleton/consumer/classes/dds/tcp2.ini";
        args[4] = "-r";
        args[5] = "-w";
        count = 1;
    }


    private void createConsumer(){

        boolean reliable = this.qos != 0;

        DomainParticipantFactory dpf =
                TheParticipantFactory.WithArgs(new StringSeqHolder(this.args));
        if (dpf == null) {
            System.err.println("ERROR: Domain Participant Factory not found");
            return;
        }

        DomainParticipant dp = dpf.create_participant(1,
                    PARTICIPANT_QOS_DEFAULT.get(), null, DEFAULT_STATUS_MASK.value);
        if (dp == null) {
            System.err.println("ERROR: Domain Participant creation failed");
            return;
        }

        MessageTypeSupportImpl servant = new MessageTypeSupportImpl();
        if (servant.register_type(dp, "") != RETCODE_OK.value) {
            System.err.println("ERROR: register_type failed");
            return;
        }
        Topic top = dp.create_topic(this.topic,
                servant.get_type_name(),
                TOPIC_QOS_DEFAULT.get(),
                null,
                DEFAULT_STATUS_MASK.value);
        if (top == null) {
            System.err.println("ERROR: Topic creation failed");
            return;
        }

        Subscriber sub = dp.create_subscriber(SUBSCRIBER_QOS_DEFAULT.get(),
                null, DEFAULT_STATUS_MASK.value);
        if (sub == null) {
            System.err.println("ERROR: Subscriber creation failed");
            return;
        }

        DataReaderQos dr_qos = new DataReaderQos();
        dr_qos.deadline = new DeadlineQosPolicy();
        dr_qos.deadline.period = new Duration_t();
        dr_qos.reliability = new ReliabilityQosPolicy();
        dr_qos.reliability.max_blocking_time = new Duration_t();

        if(this.qos == 0){
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
        }else if(this.qos == 1 || this.qos == 2){
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
        }

        dr_qos.durability = new DurabilityQosPolicy();
        dr_qos.durability.kind = DurabilityQosPolicyKind.from_int(0);
        dr_qos.deadline = new DeadlineQosPolicy();
        dr_qos.deadline.period = new Duration_t();
        dr_qos.latency_budget = new LatencyBudgetQosPolicy();
        dr_qos.latency_budget.duration = new Duration_t();
        dr_qos.liveliness = new LivelinessQosPolicy();
        dr_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(0);
        dr_qos.liveliness.lease_duration = new Duration_t();
        dr_qos.reliability.max_blocking_time = new Duration_t();
        dr_qos.destination_order = new DestinationOrderQosPolicy();
        dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(0);
        dr_qos.history = new HistoryQosPolicy();
        dr_qos.history.kind = HistoryQosPolicyKind.from_int(0);
        dr_qos.resource_limits = new ResourceLimitsQosPolicy();
        dr_qos.user_data = new UserDataQosPolicy();
        dr_qos.user_data.value = new byte[0];
        dr_qos.ownership = new OwnershipQosPolicy();
        dr_qos.ownership.kind = OwnershipQosPolicyKind.from_int(0);
        dr_qos.time_based_filter = new TimeBasedFilterQosPolicy();
        dr_qos.time_based_filter.minimum_separation = new Duration_t();
        dr_qos.reader_data_lifecycle = new ReaderDataLifecycleQosPolicy();
        dr_qos.reader_data_lifecycle.autopurge_nowriter_samples_delay = new Duration_t();
        dr_qos.reader_data_lifecycle.autopurge_disposed_samples_delay = new Duration_t();
        dr_qos.representation = new DataRepresentationQosPolicy();
        dr_qos.representation.value = new short[0];
        dr_qos.type_consistency = new TypeConsistencyEnforcementQosPolicy();
        dr_qos.type_consistency.kind = 2;
        dr_qos.type_consistency.ignore_member_names = false;
        dr_qos.type_consistency.force_type_validation = false;

        DataReaderQosHolder qosh = new DataReaderQosHolder(dr_qos);
        sub.get_default_datareader_qos(qosh);
        if (reliable) {
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        }
        qosh.value.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

        DataReaderListenerImpl listener = new DataReaderListenerImpl(this);

        GuardCondition gc = new GuardCondition();
        WaitSet ws = new WaitSet();
        ws.attach_condition(gc);
        listener.set_guard_condition(gc);

        DataReader dr = sub.create_datareader(top,
                qosh.value,
                listener,
                DEFAULT_STATUS_MASK.value);
        if (!reliable) {
            listener.set_expected_count(1);
        }

        if (dr == null) {
            System.err.println("ERROR: DataReader creation failed");
            return;
        }
        Duration_t timeout = new Duration_t(DURATION_INFINITE_SEC.value,
                DURATION_INFINITE_NSEC.value);

        ConditionSeqHolder cond = new ConditionSeqHolder(new Condition[]{});
        if (ws.wait(cond, timeout) != RETCODE_OK.value) {
            System.err.println("ERROR: wait() failed.");
            return;
        }

        ws.detach_condition(gc);

    }


    @Override
    public void run() {
        createConsumer();
    }


}
