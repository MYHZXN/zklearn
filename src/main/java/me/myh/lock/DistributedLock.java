package me.myh.lock;


import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 *
 * @author mayanhao
 * @date 2017/11/25
 */
public class DistributedLock implements Lock {

    private static final String LOCK_PATH;
    private static final int SESSION_TIMEOUT = 30000;
    private static final int CONNECT_TIMEOUT = 10000;
    private static final String SPLIT_STR;
    private static final String SLASH;
    static {
        LOCK_PATH = "/locks";
        SPLIT_STR = "_lock_";
        SLASH = "/";
    }



    private ZkClient client;
    /**
     *   锁名
     */
    private String lockName;
    /**
     *   当前锁节点
     */
    private String currentNode;
    /**
     *   等待前锁节点
     */
    private String waitNode;

    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        LockException(String e) {
            super(e);
        }

    }

    DistributedLock(String lockName, String connectStr) {
        this.lockName = lockName;
        this.client = new ZkClient(connectStr, SESSION_TIMEOUT, CONNECT_TIMEOUT, new SerializableSerializer());
        synchronized (DistributedLock.class) {
            if (!client.exists(LOCK_PATH)) {
                client.create(LOCK_PATH, "lock", CreateMode.PERSISTENT);
            }
        }
    }

    
    public void lock() {
        if (tryLock()) {
            System.out.println("Thread |" + Thread.currentThread().getId() + "| " + currentNode + " get lock ~");
        } else {
            waitForLock(this.waitNode, -1, null);
        }
    }

    private boolean waitForLock(final String frontLock, long time, TimeUnit unit) {
        if (client.exists(LOCK_PATH + SLASH + frontLock)) {
            final CountDownLatch latch = new CountDownLatch(1);
            client.subscribeDataChanges(LOCK_PATH + SLASH + frontLock, new IZkDataListener() {
                public void handleDataChange(String s, Object o) throws Exception {

                }

                public void handleDataDeleted(String s) throws Exception {
                    latch.countDown();
                    System.out.println("LOCK_PATH" + SLASH +  frontLock + " release");
                }
            });
            System.out.println("Thread |" + Thread.currentThread().getId() + "| waiting for " + LOCK_PATH + SLASH + frontLock);
            try {
                if (time == -1) {
                    latch.await();
                } else {
                    latch.await(time, unit);
                    if (client.exists(LOCK_PATH + SLASH + frontLock)) {
                        System.out.println("Thread |" + Thread.currentThread().getId() + "| " + currentNode + " get lock timeout");
                        return false;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread |" + Thread.currentThread().getId() + "| " + currentNode + " get lock true");
        }
        return true;
    }

    
    public void lockInterruptibly() throws InterruptedException {

    }

    
    public boolean tryLock() {
        if (SPLIT_STR.contains(this.lockName)) {
            throw new LockException("lockName can not contains \\u000B");
        }

        currentNode = client.create(LOCK_PATH + "/" + lockName + SPLIT_STR, new byte[0], CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Thread |" + Thread.currentThread().getId() +"| " + currentNode + " is created !");

        List<String> children = client.getChildren(LOCK_PATH);
        List<String> lockNodes = new ArrayList<String>();
        for (String node : children) {
            String nodeName = node.split(SPLIT_STR)[0];
            if (nodeName.equals(lockName)) {
                lockNodes.add(node);
            }
        }
        Collections.sort(lockNodes);
        if (currentNode.equals(LOCK_PATH + SLASH + lockNodes.get(0))) {
            System.out.println("Thread |" + Thread.currentThread().getId() +"| " + currentNode + " first get lcok !");
            return true;
        }
        String subStr = currentNode.substring(currentNode.lastIndexOf(SLASH) + 1);
        this.waitNode = lockNodes.get(Collections.binarySearch(lockNodes, subStr) - 1);
        return false;
    }

    
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (this.lockName.contains(SPLIT_STR)) {
            throw new LockException("lockName can not contains \\u000B");
        }

        currentNode = client.create(LOCK_PATH + "/" + lockName + SPLIT_STR, new byte[0], CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Thread |" + Thread.currentThread().getId() +"| " + currentNode + " is created !");

        List<String> children = client.getChildren(LOCK_PATH);
        List<String> lockNodes = new ArrayList<String>();
        for (String node : children) {
            String nodeName = node.split(SPLIT_STR)[0];
            if (lockName.equals(nodeName)) {
                lockNodes.add(node);
            }
        }
        Collections.sort(lockNodes);
        if (currentNode.equals(LOCK_PATH + SLASH + lockNodes.get(0))) {
            System.out.println("Thread |" + Thread.currentThread().getId() +"| " + currentNode + " first get lcok !");
            return true;
        }
        String subStr = currentNode.substring(currentNode.lastIndexOf(SLASH) + 1);
        this.waitNode = lockNodes.get(Collections.binarySearch(lockNodes, subStr) - 1);
        return waitForLock(waitNode, time, unit);
    }

    
    public void unlock() {
        System.out.println("unlock " + currentNode);
        client.delete(currentNode, -1);
        currentNode = null;
        client.close();
    }

    
    public Condition newCondition() {
        return null;
    }
}
