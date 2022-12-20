package io.juneqqq.core.filter;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.juneqqq.config.XssProperties;
import io.juneqqq.core.wrapper.XssHttpServletRequestWrapper;
import io.juneqqq.pojo.dto.PageResult;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 防止 XSS 攻击的过滤器
 */
@Component
@ConditionalOnProperty(value = "system.xss.enabled", havingValue = "true")
@WebFilter(urlPatterns = "/*", filterName = "xssFilter")
@EnableConfigurationProperties(value = {XssProperties.class})
public class XssFilter implements Filter {

    @Resource
    XssProperties xssProperties;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String url = req.getServletPath();
        for (String exclude : xssProperties.excludes()) {
            if (url.contains(exclude)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        // exclusive url里没有和当前url匹配的，那么就走wrapper逻辑
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
                (HttpServletRequest) servletRequest);
        filterChain.doFilter(xssRequest, servletResponse);
    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}