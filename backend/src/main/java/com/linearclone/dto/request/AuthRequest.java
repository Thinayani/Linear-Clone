package com.linearclone.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthRequest {

    @Data
    public static class Register {
        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 8, max = 100)
        private String password;

        @NotBlank @Size(min = 2, max = 100)
        private String displayName;
    }

    @Data
    public static class Login {
        @NotBlank @Email
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class RefreshToken {
        @NotBlank
        private String refreshToken;
    }
}
