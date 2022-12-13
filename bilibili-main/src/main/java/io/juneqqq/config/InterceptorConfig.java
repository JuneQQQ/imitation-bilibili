package io.juneqqq.config;

import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.core.interceptor.AuthInterceptor;
import io.juneqqq.core.interceptor.FlowLimitInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web Mvc 相关配置不要加 @EnableWebMvc 注解，否则会导致 jackson 的全局配置失效。因为 @EnableWebMvc 注解会导致
 * WebMvcAutoConfiguration 自动配置失效
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    FlowLimitInterceptor flowLimitInterceptor;
    //    @Resource FileInterceptor fileInterceptor;
//    @Resource
//    TokenParseInterceptor tokenParseInterceptor;
    @Resource
    AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // sentinel 流量限制拦截器
        registry.addInterceptor(flowLimitInterceptor)
                .addPathPatterns("/**")
                .order(0);
//
//        // 文件访问拦截
//        registry.addInterceptor(fileInterceptor)
//            .addPathPatterns(SystemConfigConstant.IMAGE_UPLOAD_DIRECTORY + "**")
//            .order(1);

        // 权限认证拦截
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        // 拦截前台所有相关请求接口
                        ApiRouterConstant.API_FRONT_URL_PREFIX + "/**",
                        // 拦截平台后台相关请求接口
                        ApiRouterConstant.API_ADMIN_URL_PREFIX + "/**")
                // 放行登录注册相关请求接口
                .excludePathPatterns(
                        ApiRouterConstant.API_FRONT_USER_URL_PREFIX + "/register",
                        ApiRouterConstant.API_FRONT_USER_URL_PREFIX + "/login",
                        ApiRouterConstant.API_ADMIN_URL_PREFIX + "/login")
                .order(2);

        // Token 解析拦截器
//        registry.addInterceptor(tokenParseInterceptor)
//                // 拦截小说内容查询接口，需要解析 token 以判断该用户是否有权阅读该章节（付费章节是否已购买）
//                .addPathPatterns(ApiRouterConstant.API_FRONT_BOOK_URL_PREFIX + "/content/*")
//                .order(3);

    }
}
