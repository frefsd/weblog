package com.blog.filter;

import com.blog.utils.JwtUtil;
import com.blog.utils.RedisConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j // 添加日志注解
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                // 如果 token 无效，这里会抛异常，被 catch 捕获后 username 保持为 null
                username = jwtUtil.extractUsername(jwt);
            }
        } catch (Exception e) {
            // 记录警告日志，但不要中断请求
            log.warn("JWT 解析失败或 Token 无效: {}", e.getMessage());
            // 此时 username 为 null，后续逻辑会跳过认证，允许请求继续（例如去执行登录操作）
            jwt = null;
            username = null;
        }

        // 2. 如果解析出了用户名，且当前未认证，则进行认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // 2.1 检查 Redis 白名单：Token 必须存在于 Redis 中才有效
                String cachedUsername = redisTemplate.opsForValue().get(RedisConstants.LOGIN_TOKEN_KEY + jwt);
                if (cachedUsername == null) {
                    log.warn("Token 不在 Redis 白名单中（已退出登录或已失效），用户：{}", username);
                    chain.doFilter(request, response);
                    return;
                }

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 再次验证 Token (检查过期时间等)
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("用户 [{}] 认证成功，权限：{}", username, userDetails.getAuthorities());
                } else {
                    log.warn("Token 验证失败 (过期或签名错误)，用户：{}", username);
                }
            } catch (UsernameNotFoundException e) {
                log.warn("Token 中的用户 [{}] 在系统中不存在", username);
            }catch (Exception e){
                log.error("认证过程中发生未知错误：{}", e.getMessage(), e);
            }
        }

        // 3. 继续执行过滤器链
        // 注意：如果认证失败，SecurityContextHolder 为空，后续的 .anyRequest().authenticated() 会拦截并返回 401
        chain.doFilter(request, response);
    }
}