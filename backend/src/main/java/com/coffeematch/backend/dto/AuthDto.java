package com.coffeematch.backend.dto;

import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class SignupRequest {
        private String email;
        private String password;
        private String nickname;
    }

    @Getter
    @Setter
    public static class JwtResponse {
        private String token;
        private String nickname;
        private String role;

        public JwtResponse(String token, String nickname, String role) {
            this.token = token;
            this.nickname = nickname;
            this.role = role;
        }
    }
}
