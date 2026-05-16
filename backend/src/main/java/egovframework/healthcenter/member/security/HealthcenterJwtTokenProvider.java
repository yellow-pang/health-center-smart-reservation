package egovframework.healthcenter.member.security;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import egovframework.com.jwt.InvalidJwtException;
import egovframework.healthcenter.member.domain.MemberRole;
import egovframework.healthcenter.member.mapper.MemberVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class HealthcenterJwtTokenProvider {

	private final String secretKeyString;
	private final long accessTokenValiditySeconds;

	public HealthcenterJwtTokenProvider(
		@Value("${Globals.jwt.secret}") String secretKeyString,
		@Value("${Globals.jwt.access-token-validity-seconds:3600}") long accessTokenValiditySeconds
	) {
		this.secretKeyString = secretKeyString;
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	public String generateAccessToken(MemberVO member) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("memberId", member.getId());
		claims.put("healthCenterId", member.getHealthCenterId());
		claims.put("email", member.getEmail());
		claims.put("name", member.getName());
		claims.put("role", member.getRole());
		claims.put("type", "ACCESS");

		return Jwts.builder()
			.claims(claims)
			.subject(member.getEmail())
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + accessTokenValiditySeconds * 1000))
			.signWith(getSecretKey())
			.compact();
	}

	public MemberPrincipal getPrincipalFromToken(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

			Object memberId = claims.get("memberId");
			Object role = claims.get("role");
			if (memberId == null || role == null) {
				throw new InvalidJwtException("Missing member principal claims");
			}

			return new MemberPrincipal(
				Long.valueOf(memberId.toString()),
				toLongOrNull(claims.get("healthCenterId")),
				stringValue(claims.get("email")),
				stringValue(claims.get("name")),
				MemberRole.valueOf(role.toString())
			);
		} catch (IllegalArgumentException | JwtException e) {
			throw new InvalidJwtException("Unable to verify healthcenter JWT Token: " + e.getMessage());
		}
	}

	public LocalDateTime accessTokenExpiresAt() {
		return LocalDateTime.now().plusSeconds(accessTokenValiditySeconds);
	}

	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
	}

	private Long toLongOrNull(Object value) {
		if (value == null) {
			return null;
		}
		return Long.valueOf(value.toString());
	}

	private String stringValue(Object value) {
		return value == null ? null : value.toString();
	}
}
