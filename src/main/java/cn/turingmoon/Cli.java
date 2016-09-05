package cn.turingmoon;

import cn.turingmoon.detectors.FlowHeaderDetector;
import cn.turingmoon.detectors.TrafficPatternDetector;
import cn.turingmoon.generators.FlowStore;
import cn.turingmoon.generators.PacketCapturer;
import org.apache.commons.cli.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cli {
    static Options options = new Options();

    static {
        options.addOption("h", "help", false, "The command help");
        options.addOption("f", "file", false, "filename");
        options.addOption("s", "server", false, "Express");
    }

    static void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("Network Abnormal Traffic Detector", options);
    }

    public static void main(String[] args){
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("h")) {
                printHelp();
                return;
            }

            String filename = cl.getOptionValue("file");

            new Thread(new Runnable() {
                public void run() {
                    PacketCapturer capturer = new PacketCapturer();
                    capturer.start();
                }
            }).start();
            FlowStore store = new FlowStore();
            store.run();

            FlowHeaderDetector flowDect = new FlowHeaderDetector();
            flowDect.run();

            TrafficPatternDetector trafficDect = new TrafficPatternDetector();
            trafficDect.run();

            if (cl.hasOption("s")) {
                Runtime run = Runtime.getRuntime();
                try {
                    Process p = run.exec("node.exe webserver\\bin\\www");
                    BufferedInputStream in = new BufferedInputStream(p.getInputStream());
                    BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
                    String lineStr;
                    while ((lineStr = inBr.readLine()) != null)
                        //获得命令执行后在控制台的输出信息
                        System.out.println(lineStr);// 打印输出信息
                    //检查命令是否执行失败。
                    if (p.waitFor() != 0) {
                        if (p.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束
                            System.err.println("命令执行失败!");
                    }
                    inBr.close();
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
