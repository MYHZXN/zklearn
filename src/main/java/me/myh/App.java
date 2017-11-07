package me.myh;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args){
        ZkClient client = new ZkClient("127.0.0.1:2181", 10000, 10000, new SerializableSerializer());

        String path = client.create("/test", "first create", CreateMode.EPHEMERAL);

        System.out.println("create path :" + path);

        client.close();
    }
}
