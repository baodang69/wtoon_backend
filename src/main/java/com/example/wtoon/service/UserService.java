package com.example.wtoon.service;

import com.example.wtoon.dto.request.UserLogin;
import com.example.wtoon.dto.request.UserRegister;
import com.example.wtoon.entity.Role;
import com.example.wtoon.entity.User;
import com.example.wtoon.enums.UserStatus;
import com.example.wtoon.mapper.UserMapper;
import com.example.wtoon.repository.RoleRepository;
import com.example.wtoon.repository.UserRepository;
import com.example.wtoon.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private void validateRegister(UserRegister request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }
    }

    private void validateLogin(UserLogin request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }


    @Transactional
    public String register(UserRegister userDTO) {
        this.validateRegister(userDTO);

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.");
        }

        User user = userMapper.toEntity(userDTO);
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Role không tồn tại"));
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        userRepository.save(user);

        return jwtService.generateToken(user.getUsername());
    }

    public String login(UserLogin userDTO) {
        this.validateLogin(userDTO);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDTO.getUsername(),
                userDTO.getPassword()
        );
        try {
            Authentication authentication = authenticationManager.authenticate(authToken);

            return jwtService.generateToken(userDTO.getUsername());

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Tên đăng nhập / mật khẩu không chính xác");
        }
    }
}
