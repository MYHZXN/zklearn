package me.myh.configmanage;

import lombok.Getter;
import lombok.Setter;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mayanhao on 2017/11/7.
 */

@Setter
@Getter
public class Client {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private static final String ZK_CONNECT_STRING = "127.0.0.1:2181";
    private static final String CONFIG_PATH = "/config";

    //eg database config
    private static final String CONFIG_DATABASE_PATH = "/config/database";


    private static final String AUTH_TYPE = "digest";
    private static final String AUTH_PASS = "root";

    private ZkClient zkClient = null;
    private DataBaseConfig dataBaseConfig = null;


    public Client() {
        this.zkClient = new ZkClient(ZK_CONNECT_STRING, 10000, 10000, new SerializableSerializer());
        zkClient.addAuthInfo(AUTH_TYPE, AUTH_PASS.getBytes());
        initDataBaseConfig();
        watchConfigChanged();
    }

    private void watchConfigChanged() {
        zkClient.subscribeDataChanges(CONFIG_DATABASE_PATH, new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
                log.info(">>> DataChange");
                initDataBaseConfig();
                log.info("改变后的配置");
                printConfig();
            }

            public void handleDataDeleted(String s) throws Exception {
                log.error(">>> DataDeleted");
            }
        });
    }

    private void initDataBaseConfig() {
        this.dataBaseConfig = zkClient.readData(CONFIG_DATABASE_PATH);
    }

    void printConfig() {
        log.info("JDBC URL : " + dataBaseConfig.getUrl());
        log.info("JDBC USERNAME : " + dataBaseConfig.getUserName());
        log.info("JDBC PASSWORD : " + dataBaseConfig.getPassword());
    }

    void close() {
        zkClient.close();
    }
}
