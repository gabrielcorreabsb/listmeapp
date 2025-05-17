package com.listme.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String type;
    private com.listme.dto.UserDTO user;

    public LoginResponse(String token, String type, com.listme.dto.UserDTO user) {
        this.token = token;
        this.type = type;
        this.user = user;
    }
}