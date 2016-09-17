package cn.turingmoon.models;

import org.bson.Document;

import java.util.Date;

public class Flow {
    private Date bTime;
    private Date eTime;
    private String sIP;
    private String dIP;
    private String sPort;
    private String dPort;
    private String Type;
    private int pNum;
    private int pSize;

    public boolean equals(Object o) {
        if (!(o instanceof Flow)) {
            return false;
        }
        Flow right = (Flow) o;
        return getsIP().equals(right.getsIP())
                && getdIP().equals(right.getdIP())
                && (getsPort() == null || getsPort().equals(right.getsPort()))
                && (getdPort() == null || getdPort().equals(right.getdPort()))
                && (getType() == null || getType().equals(right.getType()));
    }

    public Date getbTime() {
        return bTime;
    }

    public void setbTime(Date bTime) {
        this.bTime = bTime;
    }

    public Date geteTime() {
        return eTime;
    }

    public void seteTime(Date eTime) {
        this.eTime = eTime;
    }

    public String getsIP() {
        return sIP;
    }

    public void setsIP(String sIP) {
        this.sIP = sIP;
    }

    public String getdIP() {
        return dIP;
    }

    public void setdIP(String dIP) {
        this.dIP = dIP;
    }

    public String getsPort() {
        return sPort;
    }

    public void setsPort(String sPort) {
        this.sPort = sPort;
    }

    public String getdPort() {
        return dPort;
    }

    public void setdPort(String dPort) {
        this.dPort = dPort;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public int getpNum() {
        return pNum;
    }

    public void setpNum(int pNum) {
        this.pNum = pNum;
    }

    public int getpSize() {
        return pSize;
    }

    public void setpSize(int pSize) {
        this.pSize = pSize;
    }

    public static Flow parseDocument(Document document) {
        Flow temp = new Flow();
        temp.setbTime(document.getDate("BeginTime"));
        temp.seteTime(document.getDate("EndTime"));
        temp.setsIP(document.getString("SrcIP"));
        temp.setdIP(document.getString("DstIP"));
        temp.setsPort(document.getString("SrcPort"));
        temp.setdPort(document.getString("DstPort"));
        temp.setType(document.getString("Type"));
        temp.setpNum(document.getInteger("PacketNum"));
        temp.setpSize(document.getInteger("PacketSize"));
        return temp;
    }

    public static Document toDocument(Flow item) {
        return new Document()
                .append("BeginTime", item.getbTime())
                .append("EndTime", item.geteTime())
                .append("SrcIP", item.getsIP())
                .append("DstIP", item.getdIP())
                .append("SrcPort", item.getsPort())
                .append("DstPort", item.getdPort())
                .append("Type", item.getType())
                .append("PacketNum", item.getpNum())
                .append("PacketSize", item.getpSize());
    }
}
