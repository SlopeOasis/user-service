package com.slopeoasis.user.clerk;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP-based Clerk verifier. This implementation expects the environment property
 * CLERK_VERIFY_URL to be set to a Clerk endpoint that can validate a session token
 * (for example an endpoint in the Clerk API). It will call the URL with
 * Authorization: Bearer <token> and attempt to extract a wallet address from the response.
 *
 * NOTE: Clerk's exact verification endpoints and response shape may differ; adapt
 * CLERK_VERIFY_URL and the parsing logic below to match Clerk's API shape.
 */
@Component
public class HttpClerkVerifier implements ClerkVerifier {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String verifyUrl;
    private final boolean disabled;

    public HttpClerkVerifier() {
        // Configure via environment variables so deploy-time config can point to Clerk
        this.verifyUrl = System.getenv("CLERK_VERIFY_URL");
        this.disabled = "true".equalsIgnoreCase(System.getenv("CLERK_VERIFY_DISABLED"));
    }

    @Override
    public ClerkUser verify(String token) throws Exception {
        if (disabled) {
            throw new IllegalStateException("Clerk verification is disabled (CLERK_VERIFY_DISABLED=true)");
        }
        if (verifyUrl == null || verifyUrl.isBlank()) {
            throw new IllegalStateException("CLERK_VERIFY_URL is not configured. Set it to Clerk's verification endpoint.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = rest.exchange(verifyUrl, HttpMethod.GET, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Clerk token verification failed: " + resp.getStatusCode());
        }

        JsonNode root = mapper.readTree(resp.getBody());
        // Attempt to extract user id and wallet address from common fields. Adjust as needed.
        String userId = root.path("user_id").asText(null);
        String wallet = null;
        // Try common locations
        if (root.has("primary_wallet_address")) {
            wallet = root.path("primary_wallet_address").asText(null);
        }
        if (wallet == null && root.has("external_accounts") && root.path("external_accounts").isArray()) {
            JsonNode arr = root.path("external_accounts");
            if (arr.size() > 0) {
                wallet = arr.get(0).path("address").asText(null);
            }
        }

        return new ClerkUser(userId, wallet);
    }
}
