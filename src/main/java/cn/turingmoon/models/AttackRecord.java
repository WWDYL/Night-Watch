package cn.turingmoon.models;

import cn.turingmoon.constants.AttackType;

import java.util.Date;

public class AttackRecord {
    public Date BeginTime;
    public Long Duration;
    public String Src;
    public String Dst;
    public String Protocol;
    public String Description;

    public AttackRecord(Flow flow, AttackType type) {
        BeginTime = flow.getbTime();
        Duration = 0L;
        Src = flow.getsIP();
        Dst = flow.getdIP();
        Protocol = flow.getType();
        Description = type.name();
    }

    public AttackRecord(int type, String key, TrafficPattern tp, String desc) {
        BeginTime = tp.getBeginTime();
        Duration = tp.getDuration();
        if (type == 1) {
            // key是攻击者
            Src = key;
            Dst = "*";
        } else {
            // key是被攻击者
            Src = "*";
            Dst = key;
        }
        Protocol = tp.getProto();
        Description = desc;
    }
}
