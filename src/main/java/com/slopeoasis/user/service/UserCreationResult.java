package com.slopeoasis.user.service;

import com.slopeoasis.user.entity.User;

public class UserCreationResult {
    private final User user;
    private final boolean created;

    public UserCreationResult(User user, boolean created) {
        this.user = user;
        this.created = created;
    }

    public User getUser() {
        return user;
    }

    public boolean isCreated() {
        return created;
    }
}
