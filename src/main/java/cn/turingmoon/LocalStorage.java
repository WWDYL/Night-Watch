package cn.turingmoon;

import cn.turingmoon.models.Flow;
import cn.turingmoon.models.TrafficPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalStorage {
    public static boolean DEBUG = false;
    public static int CYCLE_TIME = 60;
    public static String OFFLINE_FILE = null;
    public static String RUNNING_MODE = null;

    public static String TRAFFIC_DB = null;
    public static String FLOWS_DB = null;

    public static String BroadcastAddr = "255.255.255.255";

    public static List<Flow> tempFlows = new ArrayList<Flow>();
    public static Map<String, TrafficPattern> source_based = new HashMap<String, TrafficPattern>();
    public static Map<String, TrafficPattern> destination_based = new HashMap<String, TrafficPattern>();
}
