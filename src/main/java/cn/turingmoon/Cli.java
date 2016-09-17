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
    static private Options options = new Options();

    static {
        options.addOption("h", "help", false, "The command help");
        options.addOption("m", "mode", true, "Running mode");
        options.addOption("f", "file", true, "filename");
        options.addOption("s", "server", false, "Start a web presenter");
        options.addOption("v", "version", false, "Display software version");
        options.addOption("d", "debug", false, "Debug mode");
    }

    static private void printVersion() {
        System.out.println("Night Watch\nAbnormal Traffic Detection System\nVersion: 1.0");
    }

    static private void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("Night Watch", options);
    }

    static private void debugMode() {
        LocalStorage.CYCLE_TIME = 30;
    }

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("v")) {
                // 显示版本
                printVersion();
                return;
            }
            if (cl.hasOption("h")) {
                // 显示帮助
                printHelp();
                return;
            }

            if (cl.hasOption("d")) {
                // 调试模式
                LocalStorage.DEBUG = true;
            }

            if (LocalStorage.DEBUG) {
                debugMode();
            }

            if (cl.hasOption("m")) {
                LocalStorage.RUNNING_MODE = cl.getOptionValue("m");
                if (LocalStorage.RUNNING_MODE.equals("live")) {
                    // 即时抓包模式
                    new Thread(new Runnable() {
                        public void run() {
                            PacketCapturer capturer = new PacketCapturer(null);
                            capturer.start();
                        }
                    }).start();
                } else if (LocalStorage.RUNNING_MODE.equals("file")) {
                    // 离线文件模式
                    final String filename = cl.getOptionValue("file");
                    new Thread(new Runnable() {
                        public void run() {
                            PacketCapturer capturer = new PacketCapturer(filename);
                            capturer.start();
                        }
                    }).start();
                } else {
                    System.out.println("Invalid argument");
                    return;
                }
                // 流存储程序启动
                FlowStore store = new FlowStore();
                store.run();

                // 流头检测程序启动
                FlowHeaderDetector flowDect = new FlowHeaderDetector();
                flowDect.run();

                // 流量模式检测系统启动
                TrafficPatternDetector trafficDect = new TrafficPatternDetector();
                trafficDect.run();
            } else {
                System.out.println("You must choose a running mode!");
            }

            if (cl.hasOption("s")) {
                // 运行nodejs服务器作为展示
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
