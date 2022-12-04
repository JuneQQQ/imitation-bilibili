package com.juneqqq.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

@EnableTransactionManagement
@Configuration
@MapperScan("com.*.dao")
public class MybatisPlusConfig implements MetaObjectHandler {

//
//    @javax.annotation.Resource
//    private DataSource dataSource;
//
//    @javax.annotation.Resource
//    private MybatisPlusProperties properties;
//
//    @javax.annotation.Resource
//    private ResourceLoader resourceLoader = new DefaultResourceLoader();
//
//    @Autowired(required = false)
//    private Interceptor[] interceptors;
//
//    @Autowired(required = false)
//    private DatabaseIdProvider databaseIdProvider;
//
//
//    @Bean
//    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean() throws IOException {
//        MybatisSqlSessionFactoryBean mybatisPlus = new MybatisSqlSessionFactoryBean();
//        mybatisPlus.setDataSource(dataSource);
//        mybatisPlus.setVfs(SpringBootVFS.class);
//        String configLocation = this.properties.getConfigLocation();
//        if (StringUtils.isNotBlank(configLocation)) {
//            mybatisPlus.setConfigLocation(this.resourceLoader.getResource(configLocation));
//        }
//        mybatisPlus.setConfiguration(properties.getConfiguration());
//        mybatisPlus.setPlugins(this.interceptors);
//        MybatisConfiguration mc = new MybatisConfiguration();
//        mc.setDefaultScriptingLanguage(MybatisXMLLanguageDriver.class);
//        mc.setMapUnderscoreToCamelCase(false);// 数据库和java都是驼峰，就不需要
//        mybatisPlus.setConfiguration(mc);
//        if (this.databaseIdProvider != null) {
//            mybatisPlus.setDatabaseIdProvider(this.databaseIdProvider);
//        }
//        mybatisPlus.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
//        mybatisPlus.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
//        mybatisPlus.setMapperLocations(this.properties.resolveMapperLocations());
//        // 设置mapper.xml文件的路径
//        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resolver.getResources("classpath:mapper/*.xml");
//        mybatisPlus.setMapperLocations(resources);
//        return mybatisPlus;
//    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        //设置属性值
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
