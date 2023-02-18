import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.common.qymsg.extra.bean.KeyValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;


/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/24 0:02
 * @description
 * @
 */
@Slf4j
public class test {


    public static void main(String[] args) throws Exception {
      String a = "a llll";
        System.out.println(a.indexOf("a"));

//
//                String aa = "java.util.concurrent.ExecutionException: java.lang.NullPointerException: Cannot invoke \"java.lang.Boolean.booleanValue()\" because the return value of \"java.util.Hashtable.get(Object)\"";
//
//        System.out.println(aa.matches(".*(NumberFormatException|Cannot.*Boolean[.]booleanValue).*"));
////        Socket socket = new Socket("42.192.75.54", 4731);
//        Socket socket = new Socket("127.0.0.1", 4731);
//        IoUtil.writeMessage(socket,"5334a129-690e-4a82-8056-ef71b3c34fba");
//        IoUtil.writeMessage(socket,"test");
//        QyMsg QyMsg = new QyMsg();
//        QyDataMsg msg = new QyDataMsg();
//
//        DataMap msgMap = new DataMap();
//
//
//        QyMsg.setClazz(QyDataMsg.class);
//        QyMsg.setFrom("8b449ea1-5637-4720-a3f6-7b38035619bc");
//        QyMsg.setMsgType(1);
//
//        QyMsg.setBody(msg);
//
//        msg.setMsg(msgMap);
//
//
//        msgMap.put("id","22c9258b-223f-4ce2-87b3-6e7004868a17");
//        msgMap.put("name","qy");
//
//
//        IoUtil.writeMessage(socket,QyMsg);
//        Thread.sleep(10000);


//        ServerSocket serverSocket = new ServerSocket(5566);
//
//        Socket accept = serverSocket.accept();
//
//
//        InputStream inputStream = accept.getInputStream();
//
//        long i = 1;
//
//        ArrayList<Byte> bytes = new ArrayList<>();
//
//        while (true) {
//
//            int read = inputStream.read();
//
//
//            if (read >= 0) {
//                bytes.add((byte) read);
//            }
//
//            System.out.println(JSON.toJSONString(bytes));


//
//            if (read < 0 || i/8 == 1) {
//
//
//                byte[] bbs = new byte[bytes.size()];
//
//                for (int i1 = 0; i1 < bytes.size(); i1++) {
//                    bbs[i1] = bytes.get(i1);
//                }
//
//                String s = new String(bbs, StandardCharsets.UTF_8);
//                System.out.println(i + " ===== "+ s);
//                bytes = new ArrayList<>();
//            }
//            i++;
//        }


//        }
/*
        ThreadPoolExecutor qyFixedThreadPool = ThreadUtil.createQyFixedThreadPool(1000, null, null);


        HashMap<Integer, String> map = new HashMap<>();

        ArrayList<Integer> port = new ArrayList<>();


        for (int i = 1; i <= 10; i++) {

            final int j = i;
            qyFixedThreadPool.execute(() -> {
                Socket socket = null;
                try {
                    socket = new Socket("117.131.55.218", 8443);
                    port.add(j);
                    OutputStream outputStream = socket.getOutputStream();
                    byte[] b = {3, 0, 0, 19, 14, -32, 0, 0, 0, 0, 0, 1, 0, 8, 0, 11, 0, 0, 0};
                    outputStream.write(b);
                    outputStream.flush();

                    byte[] bytes = IoUtil.readBytes(socket.getInputStream(), 1024, 1000);

                    map.put(j, new String(bytes, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.error("",e);
                }
            });

        }

        while (true) {


            int queueSize = qyFixedThreadPool.getQueue().size();
            log.info("当前排队线程数：{}", queueSize);

            int activeCount = qyFixedThreadPool.getActiveCount();
            log.info(" 当前活动线程数：{}", activeCount);

            long  completedTaskCount = qyFixedThreadPool.getCompletedTaskCount();
            log.info(" 执行完成线程数：{}", completedTaskCount);

            long taskCount = qyFixedThreadPool.getTaskCount();
            log.info(" 总线程数：{}", taskCount);

            if (activeCount == 0)
                break;
            Thread.sleep(3000);
        }


        map.forEach((p, msg) -> {

            log.info("{} : {}", p, msg);

        });

        port.forEach((p) -> {
            log.info("{}", p);
        });

        System.exit(1);*/

    }
}
