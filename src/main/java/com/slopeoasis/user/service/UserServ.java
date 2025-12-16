package com.slopeoasis.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.slopeoasis.user.entity.User;
import com.slopeoasis.user.repository.UserRepo;
import com.slopeoasis.user.web3j.WalletSignatureVerifier;

@Service
public class UserServ {
    private final UserRepo userRepo;
    private final WalletSignatureVerifier walletSignatureVerifier;

    public UserServ(UserRepo userRepo) {
        this.userRepo = userRepo;
        this.walletSignatureVerifier = new WalletSignatureVerifier();
    }

    // Return existing user with given clerkId or create and return a new one.
    // Wallet is no longer stored in database (retrieved from Clerk on frontend).
    // Returns a UserCreationResult that indicates whether the user was created.
    public UserCreationResult createOrGetByClerkId(String clerkId, String walletAddress) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            return new UserCreationResult(existing.get(), false);
        }
        User u = new User(clerkId);
        User saved = userRepo.save(u);
        return new UserCreationResult(saved, true);
    }

    public PublicProfile getPublicProfileByClerkId(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            return new PublicProfile(u.getNickname());
        }
        return null;
    }

    //get nickname by clerkId
    public String getNicknameByClerk(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            return existing.get().getNickname();
        }
        return null;
    }

    public String getClerkIdByNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) return null;
        Optional<User> existing = userRepo.findByNickname(nickname);
        return existing.map(User::getClerkId).orElse(null);
    }

    //get themes by clerkId
    public String[] getThemesByClerk(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            return new String[]{
                u.getTheme1() != null ? u.getTheme1().name() : null,
                u.getTheme2() != null ? u.getTheme2().name() : null,
                u.getTheme3() != null ? u.getTheme3().name() : null
            };
        }
        return null;
    }

    //set themes by clerkId
    public void setThemesByClerk(String clerkId, String theme1, String theme2, String theme3) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            u.setTheme1(isValidTag(theme1) ? User.Tag.valueOf(theme1) : null);
            u.setTheme2(isValidTag(theme2) ? User.Tag.valueOf(theme2) : null);
            u.setTheme3(isValidTag(theme3) ? User.Tag.valueOf(theme3) : null);
            userRepo.save(u);
        }
    }

    private boolean isValidTag(String tag) {
        if (tag == null || tag.isBlank() || tag.equalsIgnoreCase("null")) {
            return false;
        }
        try {
            User.Tag.valueOf(tag);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
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

    public void verifyPolygonWallet(String clerkId,  String walletAddress, String message, String signature) {
        User user = userRepo.findByClerkId(clerkId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        boolean valid = walletSignatureVerifier.verify(message, signature, walletAddress);

        if (!valid) {
            throw new IllegalArgumentException("Wallet ownership verification failed");
        }

        user.setPolygonWalletAddress(walletAddress);
        user.setPolygonWalletVerified(true);
        userRepo.save(user);
    }

    public Boolean getPolygonWalletStatus(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            return u.getPolygonWalletVerified();
        }
        return null;
    }

    public String getPublicPolygonWalletAddress(String clerkId) {
        Optional<User> existing = userRepo.findByClerkId(clerkId);
        if (existing.isPresent()) {
            User u = existing.get();
            return u.getPolygonWalletAddress();
        }
        return null;
    }


    public record PublicProfile(String nickname) { }
}
