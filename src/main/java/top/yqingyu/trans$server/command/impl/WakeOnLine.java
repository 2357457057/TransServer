package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.main.MainConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

@Command
public class WakeOnLine {
    private final static String HEADER = "FF:FF:FF:FF:FF:FF";

    @Command("wakeUp")
    public void wakeUpAll(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws IOException {
        ArrayList<String> hosts = new ArrayList<>();
        hosts.add("192.168.50.66");
        hosts.add("192.168.50.67");
        hosts.add("192.168.50.68");
        ArrayList<String> macs = new ArrayList<>();
        macs.add("50:EB:F6:78:31:DF");
        macs.add("50:EB:F6:78:31:DE");
        macs.add("DC:21:5C:B7:64:DE");
        macs.add("E0:D5:5E:8D:7A:BB");

        for (String host : hosts) {
            for (String mac : macs) {
                sendMagicPackage(host, mac);
            }
        }
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("completely");
        rtnMsg.add(clone);
    }

    public void sendMagicPackage(String host, String mac) throws IOException {
        StringBuilder sb = new StringBuilder(HEADER);
        for (int i = 0; i < 16; i++) {
            sb.append(":");
            sb.append(mac);
        }
        DatagramChannel channel = DatagramChannel.open();
        ByteBuffer byteBuffer = getHexByteBuffer(sb.toString());
        for (int i = 1; i < 65536; i++) {
            channel.send(byteBuffer, new InetSocketAddress(host, i));
        }
        channel.close();
    }

    static ByteBuffer getHexByteBuffer(String packet) {
        String[] split = packet.split("([:]|[-])");
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(split.length);
        for (int i = 0; i < split.length; i++) {
            byteBuffer.put(i, (byte) Integer.parseInt(split[i], 16));
        }
        return byteBuffer;
    }
}
