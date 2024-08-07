package com.umc.gusto.global.auth.service;

import com.umc.gusto.global.auth.model.Tokens;
import com.umc.gusto.global.config.secret.JwtConfig;
import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.GeneralException;
import com.umc.gusto.global.util.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.umc.gusto.global.config.secret.JwtConfig.ACCESS_TOKEN_VALID_TIME;
import static com.umc.gusto.global.config.secret.JwtConfig.REFRESH_TOKEN_VALID_TIME;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService implements InitializingBean {
    private static final String UUID = "uuid";
    private static SecretKey secretKey;

    private final UserDetailsService userDetailService;
    private final RedisService redisService;

    @Override
    public void afterPropertiesSet() throws Exception {
        // jwt 시크릿 키 설정
        secretKey = JwtConfig.RANDOM_SECRET_KEY;
    }

    public Tokens createToken(String userUUID) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .subject("access-token")
                .claim(UUID, userUUID)
                .issuedAt(now)
                .signWith(secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
                .subject("refresh-token")
                .claim(UUID, userUUID)
                .issuedAt(now)
                .signWith(secretKey)
                .compact();

        Tokens tokens = new Tokens(accessToken, refreshToken);

        return tokens;
    }

    public Claims getClaims(String token) {
        return (Claims) Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parse(token)
                .getPayload();
    }

    public boolean checkValidationToken(String token) {
        try {
            getClaims(token);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Authentication getAuthentication(String token) {
        String uuid = (String) getClaims(token).get(UUID);
        UserDetails user = userDetailService.loadUserByUsername(uuid);
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }

    public Tokens createAndSaveTokens(String userUUID) {
        Tokens tokens = createToken(userUUID);
        redisService.setValuesWithTimeout(tokens.getRefreshToken(), userUUID, REFRESH_TOKEN_VALID_TIME);

        return tokens;
    }

    @Transactional
    public Tokens reissueToken(String accessToken, String refreshToken) {
        String accessUuid = null;

        try {
            accessUuid = (String) getClaims(accessToken).get(UUID);
        } catch (ExpiredJwtException e) {
            accessUuid = String.valueOf(e.getClaims().get(UUID));
        }

        try {
            getClaims(refreshToken);

            String value = redisService.getValues(refreshToken)
                    .orElseThrow(() -> new GeneralException(Code.INVALID_REFRESH_TOKEN));

            if(!value.equals(accessUuid)) {
                throw new GeneralException(Code.INVALID_REFRESH_TOKEN);
            }

            // 기존 refresh token 삭제 후 재생성
            redisService.deleteValues(refreshToken);
            Tokens newTokens = createAndSaveTokens(value);

            return newTokens;
        } catch (ExpiredJwtException e) {
            throw new GeneralException(Code.EXPIRED_REFRESH_TOKEN);
        }
    }

    public void matchCheckTokens(java.util.UUID uuid, String refreshToken) {
        try {
            String refreshUuid = (String) getClaims(refreshToken).get(UUID);

            if(!refreshUuid.equals(String.valueOf(uuid))) {
                throw new GeneralException(Code.NO_MATCH_TOKENS);
            }
        } catch (ExpiredJwtException e) {
            throw new GeneralException(Code.EXPIRED_REFRESH_TOKEN);
        }
    }
}