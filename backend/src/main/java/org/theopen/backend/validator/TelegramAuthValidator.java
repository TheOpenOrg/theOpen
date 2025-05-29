package org.theopen.backend.validator;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.theopen.backend.dto.TelegramAuthRequest;

import java.nio.charset.StandardCharsets;

@Component
public class TelegramAuthValidator {
    @Value("${telegram.bot.token}")
    private String BOT_TOKEN;

    public boolean isValid(TelegramAuthRequest request) {
        String secret = DigestUtils.sha256Hex(BOT_TOKEN.getBytes(StandardCharsets.UTF_8));
        String dataCheckString = String.format("auth_date=%d\nfirst_name=%s\nid=%d\nusername=%s",
            request.getAuth_date(), request.getFirst_name(), request.getId(), request.getUsername());
        String hash = request.getHash();
        String expectedHash = Hex.encodeHexString(HmacUtils.hmacSha256(secret, dataCheckString));
        return expectedHash.equals(hash);
    }
}