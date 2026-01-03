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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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
    @Operation(summary = "Get public profile by Clerk ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/public/{clerkId}")
    public ResponseEntity<UserServ.PublicProfile> getPublicProfile(@PathVariable String clerkId) {
        UserServ.PublicProfile profile = userServ.getPublicProfileByClerkId(clerkId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    // Public endpoint to resolve nickname to Clerk ID
    @Operation(summary = "Get Clerk ID by nickname")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/public/by-nickname/{nickname}")
    public ResponseEntity<String> getClerkIdByNickname(@PathVariable String nickname) {
        String clerkId = userServ.getClerkIdByNickname(nickname);
        if (clerkId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clerkId);
    }

    // Create or return existing user using clerkId from JWT
    // Authorization: Bearer <Clerk JWT Token>. The token will be verified by JwtInterceptor
    // and extracted usid will be available as request attribute.
    // Wallet is now retrieved directly from Clerk on frontend, not stored in database.
    @Operation(summary = "Create or get user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "200", description = "User exists"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
    })
    @PostMapping
    public ResponseEntity<User> createOrGetUser(@RequestAttribute(name = "X-User-Id", required = false) String usid) {
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
    @Operation(summary = "Get nickname")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/nickname")
    public ResponseEntity<String> getNickname(@RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String nickname = userServ.getNicknameByClerk(userId);
        if (nickname == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nickname);
    }

    // GET /users/themes - returns themes for authenticated user
    @Operation(summary = "Get themes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/themes")
    public ResponseEntity<String[]> getThemes(@RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String[] themes = userServ.getThemesByClerk(userId);
        if (themes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(themes);
    }

    // POST /users/themes - set themes for authenticated user
    @Operation(summary = "Set themes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
    })
    @PostMapping("/themes")
    public ResponseEntity<Void> setThemes(@RequestBody String[] themes,
                                          @RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        if (themes == null || themes.length != 3) {
            return ResponseEntity.badRequest().build();
        }
        userServ.setThemesByClerk(userId, themes[0], themes[1], themes[2]);
        return ResponseEntity.ok().build();
    }

    // POST /users/nickname - set nickname for authenticated user
    @Operation(summary = "Set nickname")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
    })
    @PostMapping("/nickname")
    public ResponseEntity<Void> setNickname(@RequestBody Map<String, String> body,
                                            @RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String nickname = body.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userServ.setNicknameByClerk(userId, nickname);
        return ResponseEntity.ok().build();
    }

    // DELETE user - delete authenticated user
    @Operation(summary = "Delete user")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        userServ.deleteUserByClerkId(userId);
        return ResponseEntity.noContent().build();
    }

    //POST /users/verify-wallet - verify wallet for authenticated user
    @Operation(summary = "Verify Polygon wallet")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wallet verified"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/pol-verify-wallet")
    public ResponseEntity<Void> verifyPolygonWallet(@RequestBody Map<String, String> body, @RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        String walletAddress = body.get("walletAddress");
        String message = body.get("message");
        String signature = body.get("signature");
        if (walletAddress == null || message == null || signature == null ||
            walletAddress.isBlank() || message.isBlank() || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            userServ.verifyPolygonWallet(userId, walletAddress, message, signature);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @Operation(summary = "Get Polygon wallet verification status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "404", description = "User not found") 
    })
    @GetMapping("/pol-wallet-status")
    public ResponseEntity<Boolean> getPolygonWalletStatus(@RequestAttribute(name = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        Boolean status = userServ.getPolygonWalletStatus(userId);
        if (status == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Get public Polygon wallet address")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/public/pol-wallet-addres")
    public ResponseEntity<String> getPublicPolygonWalletAddress(@RequestParam String clerkId) {
        String walletAddress = userServ.getPublicPolygonWalletAddress(clerkId);
        if (walletAddress == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(walletAddress);
    }
}
