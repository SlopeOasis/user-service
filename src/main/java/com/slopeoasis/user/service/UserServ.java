package com.slopeoasis.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.slopeoasis.user.entity.User;
import com.slopeoasis.user.repository.UserRepo;

@Service
public class UserServ {
    private final UserRepo userRepo;

    public UserServ(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    // Return existing user with given clerkId or create and return a new one.
    // Optionally set or update the walletAddress when creating/updating the record.
    // Returns a UserCreationResult that indicates whether the user was created.
    public UserCreationResult createOrGetByClerkId(String clerkId, String walletAddress) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            // update wallet address if missing and provided
            if ((u.getWalletAddress() == null || u.getWalletAddress().isBlank()) && walletAddress != null && !walletAddress.isBlank()) {
                u.setWalletAddress(walletAddress);
                userRepo.save(u);
            }
            return new UserCreationResult(u, false);
        }
        User u = new User(clerkId);
        if (walletAddress != null && !walletAddress.isBlank()) {
            u.setWalletAddress(walletAddress);
        }
        User saved = userRepo.save(u);
        return new UserCreationResult(saved, true);
    }

    //get nickname by clerkId
    public String getNicknameByClerk(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            return existing.get().getNickname();
        }
        return null;
    }

    //get themes by clerkId
    public String[] getThemesByClerk(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            return new String[]{u.getTheme1(), u.getTheme2(), u.getTheme3()};
        }
        return null;
    }

    //set themes by clerkId
    public void setThemesByClerk(String clerkId, String theme1, String theme2, String theme3) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            u.setTheme1(theme1);
            u.setTheme2(theme2);
            u.setTheme3(theme3);
            userRepo.save(u);
        }
    }
    
    //set nickname by clerkId
    public void setNicknameByClerk(String clerkId, String nickname) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            u.setNickname(nickname);
            userRepo.save(u);
        }
    }

    //delete user by clerkId
    public void deleteUserByClerkId(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            userRepo.delete(existing.get());
        }
    }
}
