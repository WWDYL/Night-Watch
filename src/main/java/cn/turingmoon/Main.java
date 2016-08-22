package cn.turingmoon;

public class Main {
    private static void parse_command_line() {

    }

    public static void main(String[] args) {
        parse_command_line();
        new Thread(new Runnable() {
            public void run() {
                PacketCapturer capturer = new PacketCapturer();
                capturer.start();
            }
        }).start();
        FlowStore store = new FlowStore();
        store.run();
    }
}
