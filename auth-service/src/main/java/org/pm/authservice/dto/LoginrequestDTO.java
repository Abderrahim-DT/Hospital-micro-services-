package org.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginrequestDTO {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be a valid address")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be minimum 8 characters")
    private String password;


    public @NotBlank(message = "Password is mandatory") @Size(min = 8, message = "Password should be minimum 8 characters") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is mandatory") @Size(min = 8, message = "Password should be minimum 8 characters") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Email is mandatory") @Email(message = "Email should be a valid address") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is mandatory") @Email(message = "Email should be a valid address") String email) {
        this.email = email;
    }
}
