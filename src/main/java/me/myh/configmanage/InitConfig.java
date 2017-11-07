package me.myh.configmanage;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * Created by mayanhao on 2017/11/7.
 */
public class InitConfig {

    private static final String ZK_CONNECT_STRING = "127.0.0.1:2181";
    private static final String CONFIG_PATH = "/config";

    //eg database config
    private static final String CONFIG_DATABASE_PATH = "/config/database";

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/bootlearn?useSSL=false";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    private static final String AUTH_TYPE = "digest";
    private static final String AUTH_PASS = "root";

    public static void main(String[] args) {

        ZkClient zkClient = new ZkClient(ZK_CONNECT_STRING, 10000, 10000, new SerializableSerializer());
        zkClient.addAuthInfo(AUTH_TYPE, AUTH_PASS.getBytes());

        if (! zkClient.exists(CONFIG_PATH)) {
            zkClient.create(CONFIG_PATH, "config".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
        }

        if (! zkClient.exists(CONFIG_DATABASE_PATH)) {
            DataBaseConfig dataBaseConfig = new DataBaseConfig(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            zkClient.create(CONFIG_DATABASE_PATH, dataBaseConfig, ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
        }

        zkClient.close();

    }

}
