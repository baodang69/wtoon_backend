package com.example.wtoon.controller;

import com.example.wtoon.dto.request.UserLogin;
import com.example.wtoon.dto.request.UserRegister;
import com.example.wtoon.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegister request) {
        String token = userService.register(request);

        ResponseCookie cookie = createCookie(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Đăng ký thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLogin request) {
        String token = userService.login(request);

        ResponseCookie cookie = createCookie(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Đăng nhập thành công!");
    }

    private ResponseCookie createCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 30)
                .sameSite("Strict")
                .build();
    }
}
