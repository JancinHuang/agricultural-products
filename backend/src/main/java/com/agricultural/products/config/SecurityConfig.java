package com.agricultural.products.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离使用 JWT）
            .csrf(csrf -> csrf.disable())
            // 启用 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 无状态会话管理
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开接口：登录、注册
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // 公开接口：商品浏览（首页热销、分类等需要未登录也能访问）
                .requestMatchers("/api/product/hot").permitAll()
                .requestMatchers("/api/product/search").permitAll()
                .requestMatchers("/api/product/page").permitAll()
                .requestMatchers("/api/product/category/**").permitAll()
                .requestMatchers("/api/product/count").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/product/*").permitAll()
                // 公开接口：分类列表（首页需要）
                .requestMatchers("/api/category/list").permitAll()
                .requestMatchers("/api/banner/list").permitAll()
                // 公开接口：评论查看和统计
                .requestMatchers("/api/review/product/**").permitAll()
                .requestMatchers("/api/review/stats/**").permitAll()
                .requestMatchers("/api/review/count/**").permitAll()
                // 公开接口：收藏计数
                .requestMatchers("/api/favorite/count/**").permitAll()
                // 公开接口：统计（图表数据）
                .requestMatchers("/api/statistics/**").permitAll()
                // 公开接口：AI购物助手（未登录也可使用，登录后个性化推荐）
                .requestMatchers("/api/ai/chat").permitAll()
                // 所有其他请求需要认证
                .anyRequest().authenticated()
            )
            // 异常处理
            .exceptionHandling(exceptions -> exceptions
                // 未认证返回 401
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    ObjectMapper mapper = new ObjectMapper();
                    response.getWriter().write(mapper.writeValueAsString(
                            java.util.Map.of("code", 401, "message", "未登录或登录已过期")
                    ));
                })
                // 无权限返回 403
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    ObjectMapper mapper = new ObjectMapper();
                    response.getWriter().write(mapper.writeValueAsString(
                            java.util.Map.of("code", 403, "message", "无权限访问")
                    ));
                })
            )
            // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
