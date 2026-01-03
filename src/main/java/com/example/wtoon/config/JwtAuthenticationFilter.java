package com.example.wtoon.config;

import com.example.wtoon.service.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = null;
        String username = null; // <--- SỬA 1: Phải khai báo biến này trước

        // CÁCH 1: Lấy từ Header (Ưu tiên Header trước để test Postman cho dễ)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // CÁCH 2: Nếu Header không có, thì mới tìm trong Cookie
        if (jwt == null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // Nếu cả 2 nơi đều không có Token -> Cho qua (để SecurityConfig chặn sau)
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // SỬA 2: Kiểm tra lại tên hàm bên JwtService của bạn
        // Trong code JwtService bạn gửi trước đó, tên hàm là extractUserName (chữ N viết hoa)
        try {
            username = jwtService.extractUserName(jwt);
        } catch (Exception e) {
            // Nếu token rác/hết hạn thì không extract được -> bỏ qua
            filterChain.doFilter(request, response);
            return;
        }

        // Validate và Set Context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Đánh dấu request này đã đăng nhập thành công
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}