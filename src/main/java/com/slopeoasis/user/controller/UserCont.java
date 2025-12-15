package com.slopeoasis.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    public UserCont(UserServ userServ) {
        this.userServ = userServ;
    }

    // Public endpoint to expose nickname for a given Clerk user
    @GetMapping("/public/{clerkId}")
    public ResponseEntity<UserServ.PublicProfile> getPublicProfile(@org.springframework.web.bind.annotation.PathVariable String clerkId) {
        UserServ.PublicProfile profile = userServ.getPublicProfileByClerkId(clerkId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    // Create or return existing user using clerkId from JWT
    // Authorization: Bearer <Clerk JWT Token>. The token will be verified by JwtInterceptor
    // and extracted usid will be available as request attribute.
    // Wallet is now retrieved directly from Clerk on frontend, not stored in database.
    @PostMapping
    public ResponseEntity<User> createOrGetUser(@org.springframework.web.bind.annotation.RequestAttribute(name = "X-User-Id", required = false) String usid) {
        if (usid == null || usid.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        System.out.println("POST /users called; usid=" + usid);

        UserCreationResult result = userServ.createOrGetByClerkId(usid, null);
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
