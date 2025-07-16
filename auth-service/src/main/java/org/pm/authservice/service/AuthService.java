package org.pm.authservice.service;

import org.pm.authservice.dto.LoginrequestDTO;

import org.pm.authservice.model.User;
import org.pm.authservice.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil ;

    public AuthService(UserService userService,PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = new jwtUtil;
    }

    public Optional<String> authenticate(LoginrequestDTO loginrequestDTO){
        Optional<String> token = userService
                .findByEmail(loginrequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginrequestDTO.getPassword(),
                        u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(),u.getRole()));
        return token;
    }


}
