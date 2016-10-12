package com.hazelcast.zookeeper;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class HazelcastIntegrationTest {

    private TestingServer zkTestServer;

    @Before
    public void setUp() throws Exception {
        zkTestServer = new TestingServer(5000);
        Thread.sleep(5000);
    }

    @After
    public void tearDown() throws IOException {
        zkTestServer.close();
    }

    @Test
    public void testIntegration() throws InterruptedException {
        String zookeeperURL = zkTestServer.getConnectString();
        System.out.println("URL: " + zookeeperURL);
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.setProperty("hazelcast.discovery.enabled", "true");

        DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(new ZookeeperDiscoveryStrategyFactory());
        discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), zookeeperURL);
        config.getNetworkConfig().getJoin().getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        System.out.println("starting1");
        config.getNetworkConfig().getInterfaces().setEnabled(true);
        config.getNetworkConfig().getInterfaces().addInterface("127.0.0.1");
        config.getNetworkConfig().setPort(6000);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        config.getNetworkConfig().setPort(7000);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);
        System.out.println("sleeping1");
        Thread.sleep(15000);
        int instance1Size = instance1.getCluster().getMembers().size();
        assertEquals(2, instance1Size);
        int instance2Size = instance2.getCluster().getMembers().size();
        assertEquals(2, instance2Size);
    }
}
