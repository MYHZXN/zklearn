package me.myh.mastersel;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayanhao on 2017/11/5.
 */
public class LeaderSelectorZkClient {

    private static final int CLIENT_QTY = 3;
    private static final String ZOOKEEPER_SERVER = "127.0.0.1:2181";

    public static void main(String[] args) throws Exception {
        List<ZkClient> clients = new ArrayList<ZkClient>();

        List<WorkServer> workServers = new ArrayList<WorkServer>();

        try {
            for (int i = 0; i < CLIENT_QTY; i++) {
                ZkClient zkClient = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
                clients.add(zkClient);

                //创建ServerData
                RunningData runningData = new RunningData();
                runningData.setCid(i);
                runningData.setName("client #" + i);

                WorkServer workServer = new WorkServer(runningData);
                workServer.setZkClient(zkClient);

                workServers.add(workServer);
                workServer.start();
            }

            System.out.println("回车结束");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            System.out.println("Shutting down ...");
            for ( WorkServer workServer : workServers) {
                try {
                    workServer.stop();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            for (ZkClient zkClient : clients) {
                try {
                    zkClient.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }
}
