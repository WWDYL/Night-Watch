package cn.turingmoon;

import cn.turingmoon.models.Flow;
import cn.turingmoon.models.TrafficPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalStorage {
    public static List<Flow> tempFlows = new ArrayList<Flow>();
    private static Map<String, TrafficPattern> source_based = new HashMap<String, TrafficPattern>();
    private static Map<String, TrafficPattern> destination_based = new HashMap<String, TrafficPattern>();
}
