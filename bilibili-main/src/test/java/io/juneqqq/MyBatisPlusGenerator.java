package io.juneqqq;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Collections;

/**
 * 代码生成器
 */
public class MyBatisPlusGenerator {

    private static final String USERNAME = "";

    /**
     * 项目信息
     */
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    private static final String JAVA_PATH = "/src/main/java";
    private static final String RESOURCE_PATH = "/src/main/resources";
    private static final String BASE_PACKAGE = "com.hzwq.wos";

    /**
     * 数据库信息
     */
    private static final String DATABASE_IP = "localhost";
    private static final String DATABASE_PORT = "3306";
    private static final String DATABASE_NAME = "wos";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "123456";


    public static void main(String[] args) {

        genCode("");
    }


    /**
     * 代码生成
     */
    private static void genCode(String tables) {
        // 全局配置
        FastAutoGenerator.create(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai", DATABASE_IP, DATABASE_PORT, DATABASE_NAME), DATABASE_USERNAME, DATABASE_PASSWORD)
                .globalConfig(builder -> {
                    builder.author(USERNAME) // 设置作者
                            .enableSpringdoc() // 开启 swagger 模式
                            .commentDate("yyyy-MM-dd")
                            .outputDir(PROJECT_PATH + JAVA_PATH); // 指定输出目录
                })
                // 包配置
                .packageConfig(builder -> builder.parent(BASE_PACKAGE) // 设置父包名
                        .service("service")
                        .serviceImpl("service.impl")
                        .entity("pojo.dao.entity")
                        .mapper("pojo.dao.mapper")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, PROJECT_PATH + RESOURCE_PATH + "/mapper")))
                // 模版配置
                .templateConfig(builder -> builder
//                        .disable(TemplateType.SERVICE)
//                        .disable(TemplateType.SERVICE_IMPL)
                                .disable(TemplateType.CONTROLLER)
                )
                // 策略配置
                .strategyConfig(builder -> builder
                                .entityBuilder()
                                .enableTableFieldAnnotation()
                                // 逻辑删除字段名(数据库)。
                                .logicDeleteColumnName("deleted")
                                // 会在实体类的该字段属性前加注解[@TableLogic]
                                .logicDeletePropertyName("deleted")
                                .enableLombok()
//                                .enableFileOverride()    //////////////////////////// 开启 Dao 文件覆写！！！！！
                                // 会在实体类的该字段上追加注解[@TableField(value = "create_time", fill = FieldFill.INSERT)]
                                .addTableFills(new Column("create_time", FieldFill.INSERT))
                                // 会在实体类的该字段上追加注解[@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)]
                                .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE))
                                // 阶段2：Mapper策略配置
                                .mapperBuilder()
//                                .enableFileOverride()   //////////////////////////// 开启 Mapper 文件覆写！！！！！
                                // 开启 @Mapper 注解。
                                // 会在mapper接口上添加注解[@Mapper]
                                .enableMapperAnnotation()
                                // 启用 BaseResultMap 生成。
                                // 会在mapper.xml文件生成[通用查询映射结果]配置。
                                .enableBaseResultMap()
                                // 启用 BaseColumnList。
                                // 会在mapper.xml文件生成[通用查询结果列 ]配置
                                .enableBaseColumnList()
                                // 阶段4：Controller策略配置
                                .controllerBuilder()
                                // 会在控制类中加[@RestController]注解。
                                .enableRestStyle()
                                // 开启驼峰转连字符
                                .enableHyphenStyle()
                                .build()
//                        .enableFileOverride()
                ) // 开启生成@RestController 控制器
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();

    }
}
