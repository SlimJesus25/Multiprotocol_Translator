package eu.arrowhead.application.skeleton.consumer.classes;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.*;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;

/**
 * This class has stuff related to messages per second (and other metrics) that can be used by producers/subscribers.
 * TODO: Maybe make this class write to log files instead of presenting on the screen.
 */
public class Utils {

    /**
     * Classes that allow the memory, thread and CPU information retrieving.
     */
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

    /**
     * Each one of these HashMaps, contain a Long key (identifier for each thread) and a List that stores the values
     * related with that thread.
     */
    private static final Map<Long, List<Integer>> threadCount = new HashMap<>();
    private static final Map<Long, List<Integer>> threadPeakCount = new HashMap<>();
    private static final Map<Long, List<String>> heapUsage = new HashMap<>();
    private static final Map<Long, List<String>> nonHeapUsage = new HashMap<>();
    private static final Map<Long, List<Integer>> availableProcessors = new HashMap<>();
    private static final Map<Long, List<Double>> sysLoadAvg = new HashMap<>();
    private static final Map<Long, Long> timeMillis = new HashMap<>();
    private static final Map<Long, FileWriter> fileWriters = new HashMap<>();

    /**
     * Unique identifier that lets all the maps above know which values are to be visualized/managed.
     */
    private static long identifier = 0;

    /**
     * Lock objects in order to obtain a fined-grained synchronization (used for 2 usages plus).
     */
    private static final Object identifierLock = new Object();
    private static final Object generalMapLock = new Object();
    private static final Object timeMapLock = new Object();
    private static final Object terminalOutputLock = new Object();

    private synchronized static void addFileWriter(long id) throws IOException {
        fileWriters.put(id, new FileWriter("statistic_logs_" + Thread.currentThread().
                getContextClassLoader().toString() + Thread.currentThread().getName() + ".log" ));
    }

    private synchronized static FileWriter accessFileWriter(long id){
        return fileWriters.get(id);
    }

    private static void configureLogs(long id){

        try {
            addFileWriter(id);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> fileWriters.forEach((k, v) -> {
                try {
                    v.flush();
                    v.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })));
        }catch (IOException e){
            System.err.println("It wasn't possible to create a log file.");
        }
    }

    private static void writeToLog(long id, String message) {
        try {
            accessFileWriter(id)
                    .append("[")
                    .append(String.valueOf(LocalDateTime.now()))
                    .append("]\n")
                    .append(message);
        } catch (IOException e) {
            System.err.println("An error occurred trying to write to file.");
        }
    }

    /**
     * Stores information, related to heap and non-heap memory at invoking time, on the parameter lists.
     * @param heapUsage - list to store heap usage at invoking time.
     * @param nonHeapUsage - list to store non-heap usage at invoking time.
     */
    private synchronized static void memory(List<String> heapUsage, List<String> nonHeapUsage){
        heapUsage.add(memoryMXBean.getHeapMemoryUsage().toString());
        nonHeapUsage.add(memoryMXBean.getNonHeapMemoryUsage().toString());
    }

    private synchronized static void threads(List<Integer> threadsCount, List<Integer> peakThreadCount){
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
    private synchronized static void cpu(List<Integer> availableProcessors, List<Double> systemLoadAvg){
        availableProcessors.add(osMXBean.getAvailableProcessors());
        systemLoadAvg.add(osMXBean.getSystemLoadAverage());
    }

    private static <E> void writeToScreen(org.slf4j.Logger log, List<List<E>> lists, String... title){

        synchronized (terminalOutputLock) {
            if (lists.isEmpty())
                return;

            boolean flag = title.length == 1;

            int increment = -1;
            for (List<E> l : lists) {
                if (!flag || increment < 0)
                    increment++;
                if (log != null) {
                    log.info("\t" + title[increment] + "\n");
                    l.forEach(v -> log.info(String.valueOf(v)));
                } else {
                    System.out.println("\t" + title[increment] + "\n");
                    l.forEach(System.out::println);
                }
            }
        }
    }

    private static void memoryInfo(List<String> heapUsage, List<String> nonHeapUsage){
        writeToScreen(null, Arrays.asList(heapUsage, nonHeapUsage), "Heap Usage", "Non-Heap Usage");
    }

    /**
     * Tries to print, either with log or System.out.println(). You can pass the log as null.
     * @param heapUsage shows statistic information about heap memory usage.
     * @param nonHeapUsage shows statistic information about non-heap memory usage.
     * @param log logger to print the statistical information.
     */
    private static void memoryInfo(List<String> heapUsage, List<String> nonHeapUsage, org.slf4j.Logger log){
        writeToScreen(log, Arrays.asList(heapUsage, nonHeapUsage), "Heap usage", "Non-Heap Usage");
    }

    private static void threadsInfo(List<Integer> threadsCount, List<Integer> peakThreadCount){
        writeToScreen(null, Arrays.asList(threadsCount, peakThreadCount), "Thead Count", "Peak Thead Count");
    }

    private static void threadsInfo(List<Integer> threadsCount, List<Integer> peakThreadCount, org.slf4j.Logger log){
        writeToScreen(log, Arrays.asList(threadsCount, peakThreadCount), "Thead Count", "Peak Thead Count");
    }

    private static void cpuInfo(List<Integer> availableProcessors, List<Double> systemLoadAvg){
        writeToScreen(null, Collections.singletonList(systemLoadAvg), "System Load Average");
    }

    private static void cpuInfo(List<Integer> availableProcessors, List<Double> systemLoadAvg, org.slf4j.Logger log){
        writeToScreen(log, Collections.singletonList(systemLoadAvg), "Available Processors", "System Load Average");
    }

    private static long incrementIdentifier(){
        synchronized (identifierLock){

            if(identifier == Long.MAX_VALUE-1)
                identifier = 0;

            return ++identifier;
        }
    }

    private static long accessIdentifier(){
        synchronized (identifierLock){
            return identifier;
        }
    }

    private static <E> void insertNewList(long id, Map<Long, ? super List<E>> map, List<E> list){
        synchronized (generalMapLock) {
            map.put(id, list);
        }
    }

    private static void insertNewTime(long id){
        synchronized (timeMapLock) {
            timeMillis.put(id, System.currentTimeMillis());
        }
    }

    private static long accessInitialTime(long id){
        synchronized (timeMapLock){
            return timeMillis.get(id);
        }
    }

    public static long initializeCounting(){
        long myID = incrementIdentifier();
        configureLogs(myID);

        List<Integer> tcl = new ArrayList<>();
        List<Integer> tpc = new ArrayList<>();
        List<String> hu = new ArrayList<>();
        List<String> nhu = new ArrayList<>();
        List<Integer> ap = new ArrayList<>();
        List<Double> sla = new ArrayList<>();

        insertNewList(myID, threadCount, tcl);
        insertNewList(myID, threadPeakCount, tpc);
        insertNewList(myID, heapUsage, hu);
        insertNewList(myID, nonHeapUsage, nhu);
        insertNewList(myID, availableProcessors, ap);
        insertNewList(myID, sysLoadAvg, sla);

        threads(tcl, tpc);
        memory(hu, nhu);

        insertNewTime(myID);

        return myID;
    }

    private static List<Integer> accessThreadCount(long id){
        return threadCount.get(id);
    }

    private static List<Integer> accessThreadPeakCount(long id){
        return threadPeakCount.get(id);
    }

    private static List<String> accessHeapUsage(long id){
        return heapUsage.get(id);
    }

    private static List<String> accessNonHeapUsage(long id){
        return nonHeapUsage.get(id);
    }

    private static List<Integer> accessAvailableProcessors(long id){
        return availableProcessors.get(id);
    }

    private static List<Double> accessSysLoadAvg(long id){
        return sysLoadAvg.get(id);
    }

    public static void halfCounting(long id){
        synchronized (generalMapLock) {
            threads(accessThreadCount(id), accessThreadPeakCount(id));
            memory(accessHeapUsage(id), accessNonHeapUsage(id));
        }
    }

    public static void pointReached(long id, Logger log){

        long execTime = System.currentTimeMillis() - accessInitialTime(id);
        StringBuilder sb = new StringBuilder();
        sb.append("Messages per second + ").
                append(100000f / (execTime / 1000f)).
                append(" ||| Execution time: ").
                append(execTime / 1000f);

        // writeToLog(id, sb.toString());

        synchronized (terminalOutputLock) {
            if(log != null)
                log.info(sb.toString());
            else
                System.out.println(sb);
        }

        synchronized (generalMapLock) {
            cpu(accessAvailableProcessors(id), accessSysLoadAvg(id));
            cpuInfo(accessAvailableProcessors(id), accessSysLoadAvg(id), log);
            memoryInfo(accessHeapUsage(id), accessNonHeapUsage(id), log);
            threadsInfo(accessThreadCount(id), accessThreadPeakCount(id), log);
        }

        deleteUnnecessaryData(id);
    }

    private static void deleteUnnecessaryData(long id){
        synchronized (generalMapLock) {
            threadCount.remove(id);
            threadPeakCount.remove(id);
            heapUsage.remove(id);
            nonHeapUsage.remove(id);
            availableProcessors.remove(id);
            sysLoadAvg.remove(id);
        }

        synchronized (timeMapLock) {
            timeMillis.remove(id);
        }
    }
}
