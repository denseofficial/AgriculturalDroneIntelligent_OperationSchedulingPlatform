package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.service.AuthService;
import com.agrodrone.vo.LoginRequest;
import com.agrodrone.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
