package eu.arrowhead.application.skeleton.consumer.classes;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * This class contains methods that can be used by all classes.
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

    private static long identifier = 0;

    /**
     * Stores information, related to heap and non-heap memory at invoking time, on the parameter lists.
     * @param heapUsage - list to store heap usage at invoking time.
     * @param nonHeapUsage - list to store non-heap usage at invoking time.
     */
    public static void memory(List<String> heapUsage, List<String> nonHeapUsage){
        heapUsage.add(memoryMXBean.getHeapMemoryUsage().toString());
        nonHeapUsage.add(memoryMXBean.getNonHeapMemoryUsage().toString());
    }

    public static void threads(List<Integer> threadsCount, List<Integer> peakThreadCount){
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
    public static void cpu(List<Integer> availableProcessors, List<Double> systemLoadAvg){
        availableProcessors.add(osMXBean.getAvailableProcessors());
        systemLoadAvg.add(osMXBean.getSystemLoadAverage());
    }

    public static void memoryInfo(List<String> heapUsage, List<String> nonHeapUsage){
        System.out.println("\tHeap usage\n");
        heapUsage.forEach(System.out::println);

        System.out.println("\n\tNon-Heap Usage\n");
        nonHeapUsage.forEach(System.out::println);
    }

    public static void memoryInfo(List<String> heapUsage, List<String> nonHeapUsage, org.slf4j.Logger log){
        log.info("\tHeap usage\n");
        heapUsage.forEach(log::info);

        System.out.println("\n\tNon-Heap Usage\n");
        nonHeapUsage.forEach(log::info);
    }

    public static void threadsInfo(List<Integer> threadsCount, List<Integer> peakThreadCount){
        System.out.println("\tThead Count\n");
        threadsCount.forEach(System.out::println);

        System.out.println("\n\tPeak Thead Count\n");
        peakThreadCount.forEach(System.out::println);
    }

    public static void threadsInfo(List<Integer> threadsCount, List<Integer> peakThreadCount, org.slf4j.Logger log){
        log.info("\tThead Count\n");
        threadsCount.forEach(v -> log.info(String.valueOf(v)));

        log.info("\n\tPeak Thead Count\n");
        peakThreadCount.forEach(v -> log.info(String.valueOf(v)));
    }

    public static void cpuInfo(List<Integer> availableProcessors, List<Double> systemLoadAvg){
        System.out.println("\tAvailable Processors\n");
        availableProcessors.forEach(System.out::println);

        System.out.println("\n\tSystem Load Average\n");
        systemLoadAvg.forEach(System.out::println);
    }

    public static void cpuInfo(List<Integer> availableProcessors, List<Double> systemLoadAvg, org.slf4j.Logger log){
        log.info("\tAvailable Processors\n");
        availableProcessors.forEach(v -> log.info(String.valueOf(v)));

        log.info("\n\tSystem Load Average\n");
        systemLoadAvg.forEach(v -> log.info(String.valueOf(v)));
    }

    private synchronized static void incrementIdentifier(){
        identifier++;
    }

    public static synchronized long initializeCouting(){
        incrementIdentifier();

        List<Integer> tcl = new ArrayList<>();
        List<Integer> tpc = new ArrayList<>();
        List<String> hu = new ArrayList<>();
        List<String> nhu = new ArrayList<>();

        threadCount.put(identifier, tcl);
        threadPeakCount.put(identifier, tpc);
        heapUsage.put(identifier, hu);
        nonHeapUsage.put(identifier, nhu);

        threads(tcl, tpc);
        memory(hu, nhu);

        timeMillis.put(identifier, System.currentTimeMillis());

        return identifier;
    }

    public static synchronized void halfCounting(long id){
        threads(threadCount.get(id), threadPeakCount.get(id));
        memory(heapUsage.get(id), nonHeapUsage.get(id));
    }

    public static synchronized void pointReached(long id, Logger log){

        long execTime = System.currentTimeMillis() - timeMillis.get(id);

        log.info("Messages per second + " + (100000f / (execTime / 1000f)));
        log.info("Execution time: " + execTime / 1000f);

        Utils.cpu(availableProcessors.get(id), sysLoadAvg.get(id));
        Utils.cpuInfo(availableProcessors.get(id), sysLoadAvg.get(id), log);
        Utils.memoryInfo(heapUsage.get(id), nonHeapUsage.get(id), log);
        Utils.threadsInfo(threadCount.get(id), threadPeakCount.get(id), log);
    }

    /**
     *
     * Note: This doesn't need to be synchronized because the only one that invokes it (pointReached) already is.
     */
    private static void clearLists(){
        availableProcessors.clear();
        heapUsage.clear();
        nonHeapUsage.clear();
        threadCount.clear();
        threadPeakCount.clear();
        sysLoadAvg.clear();
    }
}
