package org.pm.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.pm.authservice.dto.LoginResponseDTO;
import org.pm.authservice.dto.LoginrequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController {

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            LoginrequestDTO loginrequestDTO) {

        Optional<String> tokenOptional = authService.authenticate(loginrequestDTO);

        if(tokenOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = tokenOptional.get();
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

}
