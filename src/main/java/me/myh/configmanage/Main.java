package me.myh.configmanage;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by mayanhao on 2017/11/7.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final String ZK_CONNECT_STRING = "127.0.0.1:2181";

    //eg database config
    private static final String CONFIG_DATABASE_PATH = "/config/database";
    private static final String AUTH_TYPE = "digest";
    private static final String AUTH_PASS = "root";

    public static void main(String[] args) throws Exception {
        Client client = null;
        try {

            client = new Client();

            log.info("第一次初始化的配置");
            client.printConfig();

            log.info("改变配置");
            ZkClient zkClient = new ZkClient(ZK_CONNECT_STRING, 10000, 10000, new SerializableSerializer());
            zkClient.addAuthInfo(AUTH_TYPE, AUTH_PASS.getBytes());
            DataBaseConfig dataBaseConfig = new DataBaseConfig("jdbc:mysql://localhost:3306/bootlearn?useSSL=false","root","root");
            zkClient.writeData(CONFIG_DATABASE_PATH, dataBaseConfig);

            System.out.println("回车结束");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

}
