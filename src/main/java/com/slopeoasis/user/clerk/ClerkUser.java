package com.slopeoasis.user.clerk;

/**
 * Minimal representation of a verified Clerk user.
 * This class is populated by the Clerk verifier implementation.
 */
public class ClerkUser {
    private final String clerkUserId;
    private final String walletAddress;

    public ClerkUser(String clerkUserId, String walletAddress) {
        this.clerkUserId = clerkUserId;
        this.walletAddress = walletAddress;
    }

    public String getClerkUserId() {
        return clerkUserId;
    }

    public String getWalletAddress() {
        return walletAddress;
    }
}
