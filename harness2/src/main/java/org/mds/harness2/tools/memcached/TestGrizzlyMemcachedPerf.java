package org.mds.harness2.tools.memcached;


import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.memcached.MemcachedCache;
import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.mds.hprocessor.memcache.*;
import org.mds.hprocessor.memcache.utils.GrizzlyMemcacheConfig;
import org.mds.hprocessor.memcache.utils.GrizzlyMemcacheManager;
import org.mds.hprocessor.memcache.utils.MemcacheClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by modongsong on 14-8-4.
 */
public class TestGrizzlyMemcachedPerf extends DsmRunner<TestMemcachedConfiguration> {
    private final static Logger log = LoggerFactory.getLogger(TestGrizzlyMemcachedPerf.class);

    private static String KEY_PREFIX = "key-";
    private static String DATA_PREFIX = "DATA-";
    private MemcachedCache<String, Object> memcachedClient;

    private MemcachedCache<String, Object> getClient(TestMemcachedConfiguration configuration) throws IOException {
        GrizzlyMemcacheManager.init(
                new GrizzlyMemcacheConfig()
                        .setServers(configuration.memcachedAddress));
        return GrizzlyMemcacheManager.build("TestCache");
    }

    public void beforeRun(final TestMemcachedConfiguration conf) throws IOException {
        memcachedClient = this.getClient(conf);
        KEY_PREFIX = KEY_PREFIX + StringUtils.repeat("1", conf.keyLen);
        DATA_PREFIX = DATA_PREFIX + StringUtils.repeat("1", conf.dataLen);
    }

    public void runSet(final TestMemcachedConfiguration conf) {
        this.runSingle("Test Set of GrizzleMemcached", conf, (configuration1, index) -> {
            try {
                int vIndex = index % conf.itemCount;
                memcachedClient.set(KEY_PREFIX + vIndex,
                        conf.binary ? (DATA_PREFIX + vIndex).getBytes() : DATA_PREFIX + vIndex,
                        100000, false);
            } catch (Exception ex) {

            }
            return 1;
        });
    }

    public void runAsyncSet(final TestMemcachedConfiguration conf) {
        this.runSingle("Test asyncSet of GrizzleMemcached", conf, (configuration1, index) -> {
            try {
                int vIndex = index % conf.itemCount;
                memcachedClient.set(KEY_PREFIX + vIndex,
                        conf.binary ? (DATA_PREFIX + vIndex).getBytes() : DATA_PREFIX + vIndex,
                        100000, true);
            } catch (Exception ex) {

            }
            return 1;
        });
    }

    public void runGet(final TestMemcachedConfiguration conf) {
        this.runSingle("Test Get of GrizzleMemcached", conf, (configuration1, index) -> {
            try {
                int vIndex = index % conf.itemCount;
                memcachedClient.get(KEY_PREFIX + vIndex, false);
            } catch (Exception ex) {

            }
            return 1;
        });
    }

    public void runGetBulk(final TestMemcachedConfiguration conf) {
        this.runBatch("Test getBulk of GrizzleMemcached", conf, (configuration1, indexes) -> {
            Set<String> keys = new HashSet<String>();
            for (Long i : indexes) {
                keys.add(KEY_PREFIX + i % conf.itemCount);
            }
            try {
                memcachedClient.getMulti(keys);
            } catch (Exception ex) {

            }
            return indexes.size();
        });
    }

    public void runSetBulk(final TestMemcachedConfiguration conf) {
        this.runBatch("Test setBulk of GrizzleMemcached", conf, (configuration1, indexes) -> {
            Map<String, Object> keyValues = new HashMap();
            for (Long index : indexes) {
                keyValues.put(KEY_PREFIX + index % conf.itemCount, conf.binary ? (DATA_PREFIX + index).getBytes() : DATA_PREFIX + index);
            }
            try {
                memcachedClient.setMulti(keyValues, 100000);
            } catch (Exception ex) {

            }
            return indexes.size();
        });
    }

    public void runGetter(final TestMemcachedConfiguration conf) {
        final GrizzlyMemCache[] getters = GrizzlyMemCache.build(
                MemcacheClientUtils.createGrizzlyMemecache(
                        new GrizzlyMemcacheConfig().setServers(conf.memcachedAddress)), conf.getterCount);
        MemcacheCache memcacheCache = new MemcacheCache(new CacheConfig().setSyncThreads(4));
        final MemcacheGetProcessor getProcessor = MemcacheGetProcessor.newBuilder()
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheSetProcessor.ProcessorType.QUEUE)
                .setBatchSize(conf.batchSize).setGetters(getters)
                .setCache(conf.enableCache ? memcacheCache : null)
                .build();
        final AtomicLong counter = new AtomicLong();
        this.runSingle("Test Getter of GrizzleMemcached", conf, (configuration1, index) -> {
            int vIndex = index % conf.itemCount;
            getProcessor.get(KEY_PREFIX + vIndex, new MemcacheGetProcessor.GetCallback() {
                @Override
                public void handle(String key, Object value) {
                    counter.incrementAndGet();
                }

                @Override
                public void timeout(String key) {
                    log.info("get timeout");
                }

                @Override
                public void handleNull(String key) {

                }
            });
            return 0;
        }, counter);
    }

    public void runSetter(final TestMemcachedConfiguration conf) {
        final GrizzlyMemCache[] setters = GrizzlyMemCache.build(
                MemcacheClientUtils.createGrizzlyMemecache(
                        new GrizzlyMemcacheConfig().setServers(conf.memcachedAddress)), conf.getterCount);
        final MemcacheSetProcessor setProcessor = MemcacheSetProcessor.newBuilder()
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheSetProcessor.ProcessorType.QUEUE)
                .setSetters(setters).build();
        final AtomicLong counter = new AtomicLong();
        this.runSingle("Test Setter of GrizzleMemcached", conf, (configuration1, index) -> {
            int vIndex = index % conf.itemCount;
            setProcessor.set(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex,
                    new MemcacheSetProcessor.SetCallback() {
                        @Override
                        public void complete(String key, Object value) {
                            counter.incrementAndGet();
                        }

                        @Override
                        public void timeout(String key, Object value) {
                            log.info("set timeout");
                        }
                    });
            return 0;
        }, counter);
    }
}
