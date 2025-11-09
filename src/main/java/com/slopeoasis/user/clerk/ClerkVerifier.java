package com.slopeoasis.user.clerk;

public interface ClerkVerifier {
    /**
     * Verify the given Clerk token and return a ClerkUser with at least the wallet address.
     * Implementations should throw an exception or return null when verification fails.
     */
    ClerkUser verify(String token) throws Exception;
}
