package me.myh.mastersel;

import lombok.Getter;
import lombok.Setter;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.*;

/**
 * Created by mayanhao on 2017/11/5.
 */
@Setter
@Getter
public class WorkServer {

    private volatile boolean running = false;

    private ZkClient zkClient;

    private static final String MASTER_PATH = "/master";

    private IZkDataListener dataListener;

    private RunningData serverData;

    private RunningData masterData;

    private ScheduledExecutorService delayExcutor =  new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
    private int delayTime = 1;

    public WorkServer(RunningData rd) {
        this.serverData = rd;
        this.dataListener = new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
//                System.out.println("master 改变");
//                takeMaster();
//                if (masterData != null && masterData.getName().equals(serverData.getName())) {
//                    takeMaster();
//                } else {
//                    delayExcutor.schedule(new Runnable() {
//                        public void run() {
//                            takeMaster();
//                        }
//                    }, delayTime, TimeUnit.SECONDS);
//                }
            }

            public void handleDataDeleted(String s) throws Exception {
                System.out.println("master 删除");
                takeMaster();
            }
        };
    }

    public void start() throws Exception {
        //检查是否已启动
        if (running) {
            throw new Exception("server has start up");
        }
        running = true;
        //注册master节点的数据监听
        zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
        takeMaster();
    }

    public void stop() throws Exception {
        if (!running) {
            throw new Exception("server has stoped");
        }
        running = false;
        //取消master节点的的数据监听
        zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
        //释放master节点权利
        releaseMaster();
    }

    private void takeMaster() {
        //检查服务器的状态
        if (!running) {
            return;
        }
        try {
            zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
            masterData = serverData;
            delayExcutor.schedule(new Runnable() {
                public void run() {
                    releaseMaster();
                }
            }, delayTime, TimeUnit.SECONDS);
        } catch (ZkNodeExistsException e) {
            //存在master节点，读取master节点的信息
            RunningData runningData = zkClient.readData(MASTER_PATH, true);
            if (runningData == null) {
                takeMaster();
            } else {
                masterData = runningData;
                System.out.println(masterData.getName() + "is master");
            }
        } catch (ZkException e) {
            e.printStackTrace();
        }
    }

    private void releaseMaster() {
        if (checKMaster()) {
            System.out.println("释放master");
            zkClient.delete(MASTER_PATH);
        }
    }

    private boolean checKMaster() {
        try {
            RunningData eventData = zkClient.readData(MASTER_PATH);
            masterData = eventData;
            if (masterData.getName().equals(serverData.getName())) {
                return true;
            }
        } catch (ZkNoNodeException e) {
            return false;
        } catch (ZkInterruptedException e) {
            return checKMaster();
        } catch (ZkException e) {
           e.printStackTrace();
        }
        return false;
    }
}
