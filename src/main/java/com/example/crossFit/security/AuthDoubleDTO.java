package com.example.crossFit.security;

public class AuthDoubleDTO {
    private String code;

    public AuthDoubleDTO(String code) {
        this.code = code;
    }

    public AuthDoubleDTO() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
