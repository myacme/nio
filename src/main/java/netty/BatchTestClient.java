package netty;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 批量测试客户端
 * 用于模拟多个客户端同时连接服务器
 */
public class BatchTestClient {

    /**
     * 测试单个客户端
     */
    private static class TestClient implements Runnable {
        private final int clientId;
        private final String host;
        private final int port;

        public TestClient(int clientId, String host, int port) {
            this.clientId = clientId;
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                System.out.println("启动测试客户端 #" + clientId);

                NettyClient client = new NettyClient(host, port);

                if (client.connect()) {
                    // 发送测试消息
                    for (int i = 1; i <= 5; i++) {
                        String message = String.format("客户端%d-消息%d", clientId, i);
                        client.sendMessage(message);

                        // 随机等待
                        Thread.sleep((long) (Math.random() * 1000));
                    }

                    // 断开连接
                    client.sendMessage("quit");
                    Thread.sleep(500);
                    client.disconnect();

                    System.out.println("测试客户端 #" + clientId + " 完成");
                }

            } catch (Exception e) {
                System.err.println("测试客户端 #" + clientId + " 异常: " + e.getMessage());
            }
        }
    }

    /**
     * 主方法 - 启动批量测试
     */
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 8888;
        int clientCount = 5; // 并发客户端数量

        System.out.println("=".repeat(50));
        System.out.println("批量测试客户端");
        System.out.println("服务器: " + host + ":" + port);
        System.out.println("客户端数量: " + clientCount);
        System.out.println("=".repeat(50));

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        // 启动所有测试客户端
        for (int i = 1; i <= clientCount; i++) {
            executor.submit(new TestClient(i, host, port));
            Thread.sleep(100); // 间隔启动
        }

        // 关闭线程池
        executor.shutdown();

        // 等待所有任务完成
        boolean completed = executor.awaitTermination(30, TimeUnit.SECONDS);
        if (!completed) {
            System.err.println("测试超时，强制关闭");
            executor.shutdownNow();
        }

        System.out.println("批量测试完成");
    }
}