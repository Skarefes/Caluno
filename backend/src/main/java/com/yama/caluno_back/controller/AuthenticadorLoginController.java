package com.yama.caluno_back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticadorLoginController {

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> loginUsuario(Authentication authentication) {
        return ResponseEntity.ok(Map.of("nome",  authentication.getName()));
    }
}
