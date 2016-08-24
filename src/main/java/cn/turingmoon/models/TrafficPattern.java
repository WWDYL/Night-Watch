package cn.turingmoon.models;


import cn.turingmoon.constants.FlowType;

public class TrafficPattern {
    private int Flow_num;
    private int SrcIP_num;
    private int DstIP_num;
    private int SrcPort_num;
    private int DstPort_num;
    private FlowType Proto;
    private int Flow_size_sum;
    private float Flow_size_avr;
    private float Flow_size_dev;
    private int Packet_num_sum;
    private float Packet_num_avr;
    private float Packet_num_dev;
    private int SYN_num;
    private int ACK_num;

    public int getFlow_num() {
        return Flow_num;
    }

    public void setFlow_num(int flow_num) {
        Flow_num = flow_num;
    }

    public int getSrcIP_num() {
        return SrcIP_num;
    }

    public void setSrcIP_num(int srcIP_num) {
        SrcIP_num = srcIP_num;
    }

    public int getDstIP_num() {
        return DstIP_num;
    }

    public void setDstIP_num(int dstIP_num) {
        DstIP_num = dstIP_num;
    }

    public int getSrcPort_num() {
        return SrcPort_num;
    }

    public void setSrcPort_num(int srcPort_num) {
        SrcPort_num = srcPort_num;
    }

    public int getDstPort_num() {
        return DstPort_num;
    }

    public void setDstPort_num(int dstPort_num) {
        DstPort_num = dstPort_num;
    }

    public FlowType getProto() {
        return Proto;
    }

    public void setProto(FlowType proto) {
        Proto = proto;
    }

    public int getFlow_size_sum() {
        return Flow_size_sum;
    }

    public void setFlow_size_sum(int flow_size_sum) {
        Flow_size_sum = flow_size_sum;
    }

    public float getFlow_size_avr() {
        return Flow_size_avr;
    }

    public void setFlow_size_avr(float flow_size_avr) {
        Flow_size_avr = flow_size_avr;
    }

    public float getFlow_size_dev() {
        return Flow_size_dev;
    }

    public void setFlow_size_dev(float flow_size_dev) {
        Flow_size_dev = flow_size_dev;
    }

    public int getPacket_num_sum() {
        return Packet_num_sum;
    }

    public void setPacket_num_sum(int packet_num_sum) {
        Packet_num_sum = packet_num_sum;
    }

    public float getPacket_num_avr() {
        return Packet_num_avr;
    }

    public void setPacket_num_avr(float packet_num_avr) {
        Packet_num_avr = packet_num_avr;
    }

    public float getPacket_num_dev() {
        return Packet_num_dev;
    }

    public void setPacket_num_dev(float packet_num_dev) {
        Packet_num_dev = packet_num_dev;
    }

    public int getSYN_num() {
        return SYN_num;
    }

    public void setSYN_num(int SYN_num) {
        this.SYN_num = SYN_num;
    }

    public int getACK_num() {
        return ACK_num;
    }

    public void setACK_num(int ACK_num) {
        this.ACK_num = ACK_num;
    }
}
