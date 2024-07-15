package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import Messenger.MessageTypeSupportImpl;
import OpenDDS.DCPS.DEFAULT_STATUS_MASK;
import OpenDDS.DCPS.TheParticipantFactory;
import eu.arrowhead.application.skeleton.consumer.exceptions.DDSException;
import i2jrt.TAOLocalObject;
import org.omg.CORBA.StringSeqHolder;
import java.util.HashMap;
import java.util.Map;


public class DataInstantiation {

    private static DomainParticipant dp;
    private static Topic top;

    private static final Object dpLock = new Object();
    private static final Object topLock = new Object();

    public static DataWriter instantiateDataWriter(String[] dpfArgs, String topic, int qos, org.slf4j.Logger log){

        String errMsg;

        commonInstantiates(dpfArgs, topic, log);

        Publisher pub;

        synchronized (dpLock) {
            pub = dp.create_publisher(PUBLISHER_QOS_DEFAULT.get(), null,
                    DEFAULT_STATUS_MASK.value);
            if (pub == null) {
                errMsg = "ERROR: Publisher creation failed";
                log.error(errMsg);
                throw new DDSException(errMsg);
            }
        }

        DataWriterQos dw_qos = new DataWriterQos();

        dw_qos.deadline = new DeadlineQosPolicy();
        dw_qos.destination_order = new DestinationOrderQosPolicy();

        boolean reliable = true;
        dw_qos.reliability = new ReliabilityQosPolicy();

        if(qos == 0){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(0, 500000000);
            dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
            reliable = false;
        }else if(qos == 1){
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(1, 0);
            dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
        }else{
            dw_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dw_qos.deadline.period = new Duration_t(1, 0);
            dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS);

        }

        dw_qos.durability = new DurabilityQosPolicy();
        dw_qos.durability.kind = DurabilityQosPolicyKind.from_int(DurabilityQosPolicyKind._VOLATILE_DURABILITY_QOS);
        dw_qos.durability_service = new DurabilityServiceQosPolicy();
        dw_qos.durability_service.history_kind = HistoryQosPolicyKind.from_int(HistoryQosPolicyKind._KEEP_LAST_HISTORY_QOS);
        dw_qos.durability_service.service_cleanup_delay = new Duration_t();

        dw_qos.latency_budget = new LatencyBudgetQosPolicy();
        dw_qos.latency_budget.duration = new Duration_t();

        dw_qos.liveliness = new LivelinessQosPolicy();
        dw_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(LivelinessQosPolicyKind._AUTOMATIC_LIVELINESS_QOS);
        dw_qos.liveliness.lease_duration = new Duration_t();

        dw_qos.reliability.max_blocking_time = new Duration_t();

        dw_qos.destination_order = new DestinationOrderQosPolicy();
        dw_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(0);

        dw_qos.history = new HistoryQosPolicy();
        dw_qos.history.kind = HistoryQosPolicyKind.from_int(0);

        dw_qos.resource_limits = new ResourceLimitsQosPolicy();
        dw_qos.resource_limits.max_samples = 10000;
        dw_qos.resource_limits.max_samples_per_instance = 100;
        dw_qos.resource_limits.max_instances = 1000;

        dw_qos.transport_priority = new TransportPriorityQosPolicy();

        dw_qos.lifespan = new LifespanQosPolicy();
        dw_qos.lifespan.duration = new Duration_t();

        dw_qos.user_data = new UserDataQosPolicy();
        dw_qos.user_data.value = new byte[0];

        dw_qos.ownership = new OwnershipQosPolicy();
        dw_qos.ownership.kind = OwnershipQosPolicyKind.from_int(OwnershipQosPolicyKind._SHARED_OWNERSHIP_QOS);
        dw_qos.ownership_strength = new OwnershipStrengthQosPolicy();

        dw_qos.writer_data_lifecycle = new WriterDataLifecycleQosPolicy();

        dw_qos.representation = new DataRepresentationQosPolicy();
        dw_qos.representation.value = new short[0];

        DataWriterQosHolder qosh = new DataWriterQosHolder(dw_qos);
        pub.get_default_datawriter_qos(qosh);
        qosh.value.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
        qosh.value.resource_limits.max_samples = 200000;
        qosh.value.resource_limits.max_instances = 10;
        qosh.value.resource_limits.max_samples_per_instance = 10000;
        qosh.value.reliability.max_blocking_time.nanosec = 0;

        if (reliable) {
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        }else{
            qosh.value.reliability.kind =
                    ReliabilityQosPolicyKind.BEST_EFFORT_RELIABILITY_QOS;
        }

        DataWriterListenerImpl dwListener = new DataWriterListenerImpl(log);

        DataWriter dataWriter;
        synchronized (topLock) {
            dataWriter = pub.create_datawriter(top,
                    qosh.value,
                    dwListener,
                    DEFAULT_STATUS_MASK.value);
        }
        if (dataWriter == null) {
            errMsg = "ERROR: DataWriter creation failed";
            log.error(errMsg);
            throw new DDSException(errMsg);
        }

        return dataWriter;
    }

    private static void commonInstantiates(String[] dpfArgs, String topic, org.slf4j.Logger log){

        String errMsg;

        DomainParticipantFactory dpf =
                TheParticipantFactory.WithArgs(new StringSeqHolder(dpfArgs));
        if (dpf == null) {
            errMsg = "Error creating Domain Participating Factory";
            log.error(errMsg);
            throw new DDSException(errMsg);
        }

        synchronized (dpLock) {
            dp = dpf.create_participant(1,
                    PARTICIPANT_QOS_DEFAULT.get(), null, DEFAULT_STATUS_MASK.value);

            if (dp == null) {
                errMsg = "Error creating Domain Participant";
                log.error(errMsg);
                throw new DDSException(errMsg);
            }
        }

        MessageTypeSupportImpl servant = new MessageTypeSupportImpl();
        synchronized (dpLock) {
                if (servant.register_type(dp, "") != RETCODE_OK.value) {
                    errMsg = "Register type failed!";
                    log.error(errMsg);
                    throw new DDSException(errMsg);
                }
                synchronized (topLock) {
                top = dp.create_topic(topic,
                        servant.get_type_name(),
                        TOPIC_QOS_DEFAULT.get(),
                        null,
                        DEFAULT_STATUS_MASK.value);
                if (top == null) {
                    errMsg = "ERROR: Topic creation failed";
                    log.error(errMsg);
                    throw new DDSException(errMsg);
                }
            }
        }
    }


    public static <E extends TAOLocalObject> Map<String, E> instantiateDataReader(String[] dpfArgs, String topic, int qos, org.slf4j.Logger log,
                                                                                  DDSCustomConsumer dataReaderListener) {
        boolean reliable = qos != 0;

        String errMsg;
        commonInstantiates(dpfArgs, topic, log);

        Subscriber sub;
        synchronized (dpLock) {
            sub = dp.create_subscriber(SUBSCRIBER_QOS_DEFAULT.get(),
                    null, DEFAULT_STATUS_MASK.value);
        }
        if (sub == null) {
            errMsg = "ERROR: Subscriber creation failed";
            log.error(errMsg);
            throw new DDSException(errMsg);
        }

        DataReaderQos dr_qos = new DataReaderQos();

        dr_qos.reliability = new ReliabilityQosPolicy();
        dr_qos.deadline = new DeadlineQosPolicy();
        dr_qos.destination_order = new DestinationOrderQosPolicy();

        if (qos == 0) {
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._BEST_EFFORT_RELIABILITY_QOS);
            dr_qos.deadline.period = new Duration_t(0, 500000000);
            dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_RECEPTION_TIMESTAMP_DESTINATIONORDER_QOS);
        } else if (qos == 1 || qos == 2) {
            dr_qos.reliability.kind = ReliabilityQosPolicyKind.from_int(ReliabilityQosPolicyKind._RELIABLE_RELIABILITY_QOS);
            dr_qos.deadline.period = new Duration_t(1, 0);
            dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(DestinationOrderQosPolicyKind
                    ._BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS);
        }

        dr_qos.durability = new DurabilityQosPolicy();
        dr_qos.durability.kind = DurabilityQosPolicyKind.from_int(DurabilityQosPolicyKind._VOLATILE_DURABILITY_QOS);

        dr_qos.deadline = new DeadlineQosPolicy();
        dr_qos.deadline.period = new Duration_t();

        dr_qos.latency_budget = new LatencyBudgetQosPolicy();
        dr_qos.latency_budget.duration = new Duration_t();

        dr_qos.liveliness = new LivelinessQosPolicy();
        dr_qos.liveliness.kind = LivelinessQosPolicyKind.from_int(LivelinessQosPolicyKind._AUTOMATIC_LIVELINESS_QOS);
        dr_qos.liveliness.lease_duration = new Duration_t();

        dr_qos.reliability.max_blocking_time = new Duration_t();
        dr_qos.reliability.max_blocking_time.sec = 10;
        dr_qos.reliability.max_blocking_time.nanosec = 0;

        dr_qos.destination_order = new DestinationOrderQosPolicy();
        dr_qos.destination_order.kind = DestinationOrderQosPolicyKind.from_int(0);

        dr_qos.history = new HistoryQosPolicy();
        dr_qos.history.kind = HistoryQosPolicyKind.from_int(0);

        dr_qos.resource_limits = new ResourceLimitsQosPolicy();
        dr_qos.resource_limits.max_samples = 200000;

        dr_qos.user_data = new UserDataQosPolicy();
        dr_qos.user_data.value = new byte[0];

        dr_qos.ownership = new OwnershipQosPolicy();
        dr_qos.ownership.kind = OwnershipQosPolicyKind.from_int(OwnershipQosPolicyKind._SHARED_OWNERSHIP_QOS);

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

        DataReaderListenerImpl listener = new DataReaderListenerImpl(dataReaderListener);

        GuardCondition gc = new GuardCondition();
        WaitSet ws = new WaitSet();
        ws.attach_condition(gc);
        listener.set_guard_condition(gc);

        DataReader dr;
        synchronized (topLock) {
            dr = sub.create_datareader(top,
                    qosh.value,
                    listener,
                    DEFAULT_STATUS_MASK.value);
            if (!reliable) {
                listener.set_expected_count(1);
            }
        }

        if (dr == null) {
            errMsg = "ERROR: DataReader creation failed";
            log.error(errMsg);
            throw new DDSException(errMsg);
        }

        Map<String, E> ret = new HashMap<>();
        ret.put("ws", (E) ws);
        ret.put("gc", (E) gc);

        return ret;
    }
}
