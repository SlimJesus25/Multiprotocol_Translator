package eu.arrowhead.application.skeleton.consumer.classes.dds;

import DDS.*;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;

public class DataWriterListenerImpl implements DataWriterListener {

    private org.slf4j.Logger log;

    public DataWriterListenerImpl(org.slf4j.Logger log) {
        this.log = log;
    }

    @Override
    public void on_offered_deadline_missed(DataWriter dataWriter, OfferedDeadlineMissedStatus offeredDeadlineMissedStatus) {

    }

    @Override
    public void on_offered_incompatible_qos(DataWriter dataWriter, OfferedIncompatibleQosStatus offeredIncompatibleQosStatus) {
        log.error("Incompatible QoS between publisher and subscriber {}", offeredIncompatibleQosStatus.toString());
    }

    @Override
    public void on_liveliness_lost(DataWriter dataWriter, LivelinessLostStatus livelinessLostStatus) {

    }

    @Override
    public void on_publication_matched(DataWriter dataWriter, PublicationMatchedStatus publicationMatchedStatus) {

    }

    @Override
    public boolean _is_a(String s) {
        return false;
    }

    @Override
    public boolean _is_equivalent(Object object) {
        return false;
    }

    @Override
    public boolean _non_existent() {
        return false;
    }

    @Override
    public int _hash(int i) {
        return 0;
    }

    @Override
    public Object _duplicate() {
        log.warn("Message was duplicated!");
        return null;
    }

    @Override
    public void _release() {

    }

    @Override
    public Object _get_interface_def() {
        return null;
    }

    @Override
    public Request _request(String s) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue, ExceptionList exceptionList, ContextList contextList) {
        return null;
    }

    @Override
    public Policy _get_policy(int i) {
        return null;
    }

    @Override
    public DomainManager[] _get_domain_managers() {
        return new DomainManager[0];
    }

    @Override
    public Object _set_policy_override(Policy[] policies, SetOverrideType setOverrideType) {
        return null;
    }
}
