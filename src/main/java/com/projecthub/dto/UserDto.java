package com.projecthub.dto;

import com.projecthub.entity.Role;
import com.projecthub.entity.User;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Role role;

    public UserDto() {
    }

    public UserDto(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static UserDto fromEntity(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
