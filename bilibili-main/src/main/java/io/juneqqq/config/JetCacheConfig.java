//package io.juneqqq.config;
//
//import com.alicp.jetcache.anno.CacheConsts;
//import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
//import com.alicp.jetcache.anno.config.EnableMethodCache;
//import com.alicp.jetcache.anno.support.GlobalCacheConfig;
//import com.alicp.jetcache.anno.support.SpringConfigProvider;
//import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
//import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
//import com.alicp.jetcache.support.FastjsonKeyConvertor;
//import com.alicp.jetcache.support.JavaValueDecoder;
//import com.alicp.jetcache.support.JavaValueEncoder;
//import io.lettuce.core.ClientOptions;
//import io.lettuce.core.RedisURI;
//import io.lettuce.core.cluster.ClusterClientOptions;
//import io.lettuce.core.cluster.RedisClusterClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableMethodCache(basePackages = "com.company.mypackage")
//@EnableCreateCacheAnnotation
//public class JetCacheConfig {
//
////    @Bean
////    public RedisClient redisClient(){
////        RedisClient client = RedisClient.create("redis://127.0.0.1");
////        client.setOptions(ClientOptions.builder().
////                disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
////                .build());
////        return client;
////    }
//
//    @Bean
//    public RedisClusterClient redisClient(){
//        ArrayList<RedisURI> list = new ArrayList<>();
//        RedisURI redisURI = new RedisURI();
//        redisURI.setHost("127.0.0.1");
//        redisURI.setPassword("XXXX@XXXX");
//        redisURI.setPort(1234);
//        list.add(redisURI);
//        RedisClusterClient client = RedisClusterClient.create(list);
//        client.setOptions(ClusterClientOptions.builder().
//                disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
//                .build());
//        return client;
//    }
//
//    @Bean
//    public SpringConfigProvider springConfigProvider() {
//        return new SpringConfigProvider();
//    }
//
//    @Bean
//    public GlobalCacheConfig config(SpringConfigProvider configProvider, RedisClusterClient redisClient){
//        Map localBuilders = new HashMap();
//        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
//                .createLinkedHashMapCacheBuilder()
//                .keyConvertor(FastjsonKeyConvertor.INSTANCE);
//        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);
//
//        Map remoteBuilders = new HashMap();
//        RedisLettuceCacheBuilder remoteCacheBuilder = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
//                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
//                .valueEncoder(JavaValueEncoder.INSTANCE)
//                .valueDecoder(JavaValueDecoder.INSTANCE)
//                .redisClient(redisClient);
//        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);
//
//        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
//        //globalCacheConfig.setConfigProvider(configProvider);//for jetcache <=2.5
//        globalCacheConfig.setLocalCacheBuilders(localBuilders);
//        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
//        globalCacheConfig.setStatIntervalMinutes(15);
//        globalCacheConfig.setAreaInCacheName(false);
//
//        return globalCacheConfig;
//    }
//}
