/*NOTE
 * Create/return user by wallet address
 * Get nickname by wallet address
 * Get themes by wallet address
 * Set themes by wallet address
 * Set nickname by wallet address
 * TO DO:
 * DELETE user ce potrebno implementiraj kasneje saj je vezan na post microservice
 */



package com.slopeoasis.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slopeoasis.user.clerk.ClerkUser;
import com.slopeoasis.user.clerk.ClerkVerifier;
import com.slopeoasis.user.entity.User;
import com.slopeoasis.user.service.UserCreationResult;
import com.slopeoasis.user.service.UserServ;

@RestController
@RequestMapping("/users")
// Allow requests from the frontend during development. The property CORS_ALLOWED_ORIGIN
// can be set in the environment; it defaults to http://localhost:3000
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGIN:http://localhost:3000}")
public class UserCont {

    private final UserServ userServ;
    private final ClerkVerifier clerkVerifier;

    @Autowired
    public UserCont(UserServ userServ, ClerkVerifier clerkVerifier) {
        this.userServ = userServ;
        this.clerkVerifier = clerkVerifier;
    }

    // Create or return existing user by wallet address
    // If CLERK_VERIFY_URL is configured and verification is enabled, the endpoint requires
    // Authorization: Bearer <ClerkToken> and will use the verified wallet address from Clerk.
    @PostMapping
    public ResponseEntity<User> createOrGetUser(@RequestHeader(name = "Authorization", required = false) String auth,
                                                @RequestBody(required = false) Map<String, String> body) {
    String clerkId = null;
    String wallet = null;
    boolean verified = false;

        // Try verification if an Authorization header was provided
        if (auth != null && !auth.isBlank()) {
            try {
                ClerkUser cuser = clerkVerifier.verify(auth);
                if (cuser == null || cuser.getClerkUserId() == null) {
                    return ResponseEntity.status(401).build();
                }
                clerkId = cuser.getClerkUserId();
                wallet = cuser.getWalletAddress();
                verified = true;
            } catch (Exception e) {
                // If the verifier signals an IllegalStateException (missing config or explicit disabled flag),
                // treat it as a dev-mode fallback and allow reading clerkId from the request body.
                if (e instanceof IllegalStateException) {
                    System.err.println("Clerk verifier threw IllegalStateException; falling back to body-provided clerkId: " + e.getMessage());
                    // swallow and allow clerkId to be read from body below
                } else {
                    // other exceptions indicate token verification failure - reject
                    System.err.println("Clerk verification failed: " + e.getMessage());
                    return ResponseEntity.status(401).build();
                }
            }
        }

        // If verification not performed, require clerkId in the body (no legacy wallet fallback).
        if (clerkId == null) {
            if (body == null) return ResponseEntity.badRequest().build();
            clerkId = body.get("clerkId");
            if (clerkId == null || clerkId.isBlank()) {
                return ResponseEntity.badRequest().build();
            }
        }

        // If the verifier didn't provide a wallet address, allow the client to supply
        // walletAddress in the request body. This supports dev-mode (when verifier is
        // disabled) and cases where the verification payload omits the wallet.
        if ((wallet == null || wallet.isBlank()) && body != null) {
            String bodyWallet = body.get("walletAddress");
            if (bodyWallet != null && !bodyWallet.isBlank()) {
                wallet = bodyWallet;//ce je bil prej blank ga tuki fixa
            }
        }

        // Debug log: show what clerkId/wallet will be used (do not log tokens)
        System.out.println("POST /users called; body=" + body + "; clerkId=" + clerkId + ", wallet=" + wallet + ", verified=" + verified);

        if ((clerkId == null || clerkId.isBlank()) && (wallet == null || wallet.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        //kreira novega
        UserCreationResult result = userServ.createOrGetByClerkId(clerkId, wallet);
        if (result.isCreated()) {
            User created = result.getUser();
            return ResponseEntity.created(java.net.URI.create("/users/" + created.getId())).body(created);
        }
        return ResponseEntity.ok(result.getUser());
    }

    // GET /users/nickname - returns nickname for authenticated user
    @GetMapping("/nickname")
    public ResponseEntity<String> getNickname(@org.springframework.web.bind.annotation.RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String nickname = userServ.getNicknameByClerk(userId);
        if (nickname == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nickname);
    }

    // GET /users/themes - returns themes for authenticated user
    @GetMapping("/themes")
    public ResponseEntity<String[]> getThemes(@org.springframework.web.bind.annotation.RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String[] themes = userServ.getThemesByClerk(userId);
        if (themes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(themes);
    }

    // POST /users/themes - set themes for authenticated user
    @PostMapping("/themes")
    public ResponseEntity<Void> setThemes(@RequestBody String[] themes,
                                          @org.springframework.web.bind.annotation.RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        if (themes == null || themes.length != 3) {
            return ResponseEntity.badRequest().build();
        }
        userServ.setThemesByClerk(userId, themes[0], themes[1], themes[2]);
        return ResponseEntity.ok().build();
    }

    // POST /users/nickname - set nickname for authenticated user
    @PostMapping("/nickname")
    public ResponseEntity<Void> setNickname(@RequestBody Map<String, String> body,
                                            @org.springframework.web.bind.annotation.RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String nickname = body.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userServ.setNicknameByClerk(userId, nickname);
        return ResponseEntity.ok().build();
    }

    // DELETE user - delete authenticated user
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@org.springframework.web.bind.annotation.RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        userServ.deleteUserByClerkId(userId);
        return ResponseEntity.noContent().build();
    }
}
