package cn.turingmoon.constants;

public class FlowType {
    public static String TCP = "TCP";
    public static String UDP = "UDP";
    public static String SYN = "SYN";
    public static String ACK = "ACK";
    public static String FIN = "FIN";
    public static String ICMP = "ICMP";
    public static String ICMP_Echo_Request = "ECHO_REQUEST";
    public static String ICMP_Echo_Response = "ECHO_RESPONSE";

    public static boolean isTCP(String type) {
        return type.equals(TCP) || type.equals(SYN) || type.equals(ACK) || type.equals(FIN);
    }

    public static boolean isICMP(String type) {
        return type.equals(ICMP) || type.equals(ICMP_Echo_Request) || type.equals(ICMP_Echo_Response);
    }

}
