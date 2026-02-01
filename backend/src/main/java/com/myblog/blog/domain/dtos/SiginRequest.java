package com.myblog.blog.domain.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SiginRequest {
    @NotNull
    private String name;
    @Email
    @NotNull
    private String email;
    @NotNull
    private String password;
}
