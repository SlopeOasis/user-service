package com.slopeoasis.user.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.slopeoasis.user.clerk.ClerkJwtVerifier;
import com.slopeoasis.user.clerk.ClerkTokenPayload;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Uses Clerk verifier for proper JWT validation Sets X-User-Id (usid) and X-Wallet-Address in request attributes
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private ClerkJwtVerifier clerkJwtVerifier;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip OPTIONS requests (CORS preflight) - they don't have Authorization header
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            ClerkTokenPayload payload = clerkJwtVerifier.verify(token);
            
            // Set user ID and wallet in request attributes for controllers to use
            request.setAttribute("X-User-Id", payload.getUsid());
            if (payload.getWallet() != null && !payload.getWallet().isBlank()) {
                request.setAttribute("X-Wallet-Address", payload.getWallet());
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("JWT verification failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return false;
        }
    }
}

