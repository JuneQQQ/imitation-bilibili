package com.juneqqq.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域解决配置
 * <p>
 * 跨域概念：
 * 出于浏览器的同源策略限制，同源策略会阻止一个域的javascript脚本和另外一个域的内容进行交互。
 * 所谓同源就是指两个页面具有相同的协议（protocol），主机（host）和端口号（port）
 * <p>
 * 非同源的限制：
 * 【1】无法读取非同源网页的 Cookie、LocalStorage 和 IndexedDB
 * 【2】无法接触非同源网页的 DOM
 * 【3】无法向非同源地址发送 AJAX 请求
 * <p>
 * spingboot解决跨域方案：CORS 是跨域资源分享（Cross-Origin Resource Sharing）的缩写。
 * 它是 W3C 标准，属于跨源 AJAX 请求的根本解决方法。
 * <p>
 * <p>
 * Filter是用来过滤任务的，既可以被使用在请求资源，也可以是资源响应，或者二者都有
 * Filter使用doFilter方法进行过滤
 */

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        //允许所有域名进行跨域调用
        config.addAllowedOriginPattern("*");//替换这个
        //允许跨越发送cookie
        config.setAllowCredentials(true);
        //放行全部原始头信息
        config.addAllowedHeader("*");
        //允许所有请求方法跨域调用
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
