package eu.arrowhead.application.skeleton.consumer.classes;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 * This class has stuff related to messages per second (and other metrics) that can be used by producers/subscribers.
 *
 * TODO: Maybe make this class write to log files instead of presenting on the screen.
 */
public class Utils {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

    private static final Map<Long, List<Integer>> threadCount = new HashMap<>();
    private static final Map<Long, List<Integer>> threadPeakCount = new HashMap<>();
    private static final Map<Long, List<String>> heapUsage = new HashMap<>();
    private static final Map<Long, List<String>> nonHeapUsage = new HashMap<>();
    private static final Map<Long, List<Integer>> availableProcessors = new HashMap<>();
    private static final Map<Long, List<Double>> sysLoadAvg = new HashMap<>();
    private static final Map<Long, Long> timeMillis = new HashMap<>();

    /**
     * Unique identifier that lets all the maps above know which values are to be visualized/managed.
     */
    private static long identifier = 0;

    /**
     * Stores information, related to heap and non-heap memory at invoking time, on the parameter lists.
     * @param heapUsage - list to store heap usage at invoking time.
     * @param nonHeapUsage - list to store non-heap usage at invoking time.
     */
    private static void memory(List<String> heapUsage, List<String> nonHeapUsage){
        heapUsage.add(memoryMXBean.getHeapMemoryUsage().toString());
        nonHeapUsage.add(memoryMXBean.getNonHeapMemoryUsage().toString());
    }

    private static void threads(List<Integer> threadsCount, List<Integer> peakThreadCount){
        threadsCount.add(threadMXBean.getThreadCount());
        peakThreadCount.add(threadMXBean.getPeakThreadCount());
    }

    /*
    public static void garbageCollector(List<Integer> threadsCount, List<Integer> peakThreadCount){}
     */

    /**
     * This method is going to add a new value to both parameters lists. This new value contains, respectively, the
     * available processors and the system load average.
     * Note: "getSystemLoadAverage" retrieves the average for the last minute.
     * @param availableProcessors - list to store available processors at invoking moment.
     * @param systemLoadAvg - list to store system load average at invoking moment.
     */
    private static void cpu(List<Integer> availableProcessors, List<Double> systemLoadAvg){
        availableProcessors.add(osMXBean.getAvailableProcessors());
        systemLoadAvg.add(osMXBean.getSystemLoadAverage());
    }

    private static void memoryInfo(List<String> heapUsage, List<String> nonHeapUsage){
        System.out.println("\tHeap usage\n");
        heapUsage.forEach(System.out::println);

        System.out.println("\n\tNon-Heap Usage\n");
        nonHeapUsage.forEach(System.out::println);
    }

    /**
     * Tries to print, either with log or System.out.println(). You can pass the log as null.
     * @param heapUsage
     * @param nonHeapUsage
     * @param log
     */
    private static void memoryInfo(List<String> heapUsage, List<String> nonHeapUsage, org.slf4j.Logger log){
        log.info("\tHeap usage\n");
        heapUsage.forEach(log::info);

        log.info("\n\tNon-Heap Usage\n");
        nonHeapUsage.forEach(log::info);
    }

    private static void threadsInfo(List<Integer> threadsCount, List<Integer> peakThreadCount){
        System.out.println("\tThead Count\n");
        threadsCount.forEach(System.out::println);

        System.out.println("\n\tPeak Thead Count\n");
        peakThreadCount.forEach(System.out::println);
    }

    private static void threadsInfo(List<Integer> threadsCount, List<Integer> peakThreadCount, org.slf4j.Logger log){
        log.info("\tThead Count\n");
        threadsCount.forEach(v -> log.info(String.valueOf(v)));

        log.info("\n\tPeak Thead Count\n");
        peakThreadCount.forEach(v -> log.info(String.valueOf(v)));
    }

    private static void cpuInfo(List<Integer> availableProcessors, List<Double> systemLoadAvg){
        System.out.println("\tAvailable Processors\n");
        availableProcessors.forEach(System.out::println);

        System.out.println("\n\tSystem Load Average\n");
        systemLoadAvg.forEach(System.out::println);
    }

    private static void cpuInfo(List<Integer> availableProcessors, List<Double> systemLoadAvg, org.slf4j.Logger log){
        log.info("\tAvailable Processors\n");
        availableProcessors.forEach(v -> log.info(String.valueOf(v)));

        log.info("\n\tSystem Load Average\n");
        systemLoadAvg.forEach(v -> log.info(String.valueOf(v)));
    }

    private synchronized static long incrementIdentifier(){
        return ++identifier;
    }

    private synchronized static <E> void insertNewList(long id, Map<Long, ? super List<E>> map, List<E> list){
        map.put(id, list);
    }

    private synchronized static void insertNewTime(long id){
        timeMillis.put(id, System.currentTimeMillis());
    }

    public static long initializeCounting(){
        long myID = incrementIdentifier();

        List<Integer> tcl = new ArrayList<>();
        List<Integer> tpc = new ArrayList<>();
        List<String> hu = new ArrayList<>();
        List<String> nhu = new ArrayList<>();
        List<Integer> ap = new ArrayList<>();
        List<Double> sla = new ArrayList<>();

        insertNewList(myID, threadCount, tcl);
        //threadCount.put(identifier, tcl);

        insertNewList(myID, threadPeakCount, tpc);
        //threadPeakCount.put(identifier, tpc);

        insertNewList(myID, heapUsage, hu);
        //heapUsage.put(identifier, hu);

        insertNewList(myID, nonHeapUsage, nhu);
        //nonHeapUsage.put(identifier, nhu);

        insertNewList(myID, availableProcessors, ap);
        //availableProcessors.put(identifier, ap);

        insertNewList(myID, sysLoadAvg, sla);
        //sysLoadAvg.put(identifier, sla);

        threads(tcl, tpc);
        memory(hu, nhu);

        insertNewTime(myID);
        //timeMillis.put(identifier, System.currentTimeMillis());

        return myID;
    }

    private static synchronized List<Integer> accessThreadCount(long id){
        return threadCount.get(id);
    }

    private static synchronized List<Integer> accessThreadPeakCount(long id){
        return threadPeakCount.get(id);
    }

    private static synchronized List<String> accessHeapUsage(long id){
        return heapUsage.get(id);
    }

    private static synchronized List<String> accessNonHeapUsage(long id){
        return nonHeapUsage.get(id);
    }

    private static synchronized List<Integer> accessAvailableProcessors(long id){
        return availableProcessors.get(id);
    }

    private static synchronized List<Double> accessSysLoadAvg(long id){
        return sysLoadAvg.get(id);
    }

    public static void halfCounting(long id){
        threads(accessThreadCount(id), accessThreadPeakCount(id));
        memory(accessHeapUsage(id), accessNonHeapUsage(id));
    }

    public static void pointReached(long id, Logger log){

        long execTime = System.currentTimeMillis() - timeMillis.get(id);
        log.info("Messages per second + " + (100000f / (execTime / 1000f)));
        log.info("Execution time: " + execTime / 1000f);

        Utils.cpu(accessAvailableProcessors(id), accessSysLoadAvg(id));
        Utils.cpuInfo(accessAvailableProcessors(id), accessSysLoadAvg(id), log);
        Utils.memoryInfo(accessHeapUsage(id), accessNonHeapUsage(id), log);
        Utils.threadsInfo(accessThreadCount(id), accessThreadPeakCount(id), log);
    }
}
