package com.agrodrone.service;

import com.agrodrone.entity.SysUser;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {
    private static final String SECRET = "agro-drone-scheduler-token-secret";
    private static final long EXPIRE_SECONDS = 12 * 60 * 60;

    public String createToken(SysUser user) {
        long expireAt = Instant.now().getEpochSecond() + EXPIRE_SECONDS;
        String payload = user.getId() + ":" + user.getUsername() + ":" + user.getRole() + ":" + expireAt;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public boolean validate(String token) {
        if (token == null || token.isBlank() || !token.contains(".")) {
            return false;
        }
        String[] parts = token.split("\\.", 2);
        if (!sign(parts[0]).equals(parts[1])) {
            return false;
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split(":");
        if (values.length != 4) {
            return false;
        }
        long expireAt = Long.parseLong(values[3]);
        return expireAt > Instant.now().getEpochSecond();
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Token 签名失败", exception);
        }
    }
}
