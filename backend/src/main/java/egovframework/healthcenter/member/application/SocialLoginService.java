package egovframework.healthcenter.member.application;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.dto.LoginResponse;
import egovframework.healthcenter.member.dto.SocialSignupRequest;
import egovframework.healthcenter.member.mapper.MemberMapper;
import egovframework.healthcenter.member.mapper.MemberVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class SocialLoginService {

	private static final long COMPLETION_TOKEN_VALIDITY_MILLIS = 10 * 60 * 1000;
	private static final String COMPLETION_TOKEN_TYPE = "SOCIAL_SIGNUP";

	private final MemberMapper memberMapper;
	private final AuthCommandService authCommandService;
	private final RestTemplate restTemplate = new RestTemplate();
	private final String jwtSecret;
	private final String frontendCallbackUrl;
	private final Map<SocialProvider, ProviderProperties> providers;

	public SocialLoginService(
		MemberMapper memberMapper,
		AuthCommandService authCommandService,
		@Value("${Globals.jwt.secret}") String jwtSecret,
		@Value("${Globals.oauth.frontend-callback-url:http://localhost:3000/login/social/callback}") String frontendCallbackUrl,
		@Value("${Globals.oauth.kakao.client-id:}") String kakaoClientId,
		@Value("${Globals.oauth.kakao.client-secret:}") String kakaoClientSecret,
		@Value("${Globals.oauth.kakao.redirect-uri:http://localhost:8080/api/auth/social/kakao/callback}") String kakaoRedirectUri,
		@Value("${Globals.oauth.naver.client-id:}") String naverClientId,
		@Value("${Globals.oauth.naver.client-secret:}") String naverClientSecret,
		@Value("${Globals.oauth.naver.redirect-uri:http://localhost:8080/api/auth/social/naver/callback}") String naverRedirectUri,
		@Value("${Globals.oauth.google.client-id:}") String googleClientId,
		@Value("${Globals.oauth.google.client-secret:}") String googleClientSecret,
		@Value("${Globals.oauth.google.redirect-uri:http://localhost:8080/api/auth/social/google/callback}") String googleRedirectUri
	) {
		this.memberMapper = memberMapper;
		this.authCommandService = authCommandService;
		this.jwtSecret = jwtSecret;
		this.frontendCallbackUrl = frontendCallbackUrl;
		this.providers = Map.of(
			SocialProvider.KAKAO, new ProviderProperties(kakaoClientId, kakaoClientSecret, kakaoRedirectUri),
			SocialProvider.NAVER, new ProviderProperties(naverClientId, naverClientSecret, naverRedirectUri),
			SocialProvider.GOOGLE, new ProviderProperties(googleClientId, googleClientSecret, googleRedirectUri)
		);
	}

	public URI buildAuthorizationUri(String providerName) {
		SocialProvider provider = SocialProvider.from(providerName);
		ProviderProperties properties = requireConfigured(provider);
		String state = UUID.randomUUID().toString();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(provider.authorizationUri)
			.queryParam("response_type", "code")
			.queryParam("client_id", properties.clientId())
			.queryParam("redirect_uri", properties.redirectUri())
			.queryParam("state", state);

		if (provider == SocialProvider.KAKAO) {
			builder.queryParam("scope", "profile_nickname,account_email");
		}
		if (provider == SocialProvider.GOOGLE) {
			builder.queryParam("scope", "openid email profile")
				.queryParam("access_type", "offline")
				.queryParam("prompt", "select_account");
		}

		return builder.build().toUri();
	}

	@Transactional
	public URI loginWithAuthorizationCode(String providerName, String code, String error) {
		if (error != null && !error.isBlank()) {
			return buildFrontendErrorRedirect("소셜 로그인 인증이 취소되었습니다.");
		}
		if (code == null || code.isBlank()) {
			return buildFrontendErrorRedirect("소셜 로그인 인증 코드가 없습니다.");
		}

		SocialProvider provider = SocialProvider.from(providerName);
		ProviderProperties properties = requireConfigured(provider);
		String providerAccessToken = requestAccessToken(provider, properties, code);
		SocialProfile profile = requestProfile(provider, providerAccessToken);
		MemberVO member = findOrCreateMember(provider, profile);
		if (member == null) {
			return buildFrontendProfileRequiredRedirect(provider, profile);
		}
		LoginResponse loginResponse = authCommandService.issueTokenFor(member);

		String fragment = UriComponentsBuilder.newInstance()
			.queryParam("accessToken", loginResponse.accessToken())
			.queryParam("refreshToken", loginResponse.refreshToken())
			.queryParam("role", loginResponse.member().role())
			.build()
			.getQuery();

		return URI.create(frontendCallbackUrl + "#" + fragment);
	}

	@Transactional
	public LoginResponse completeSignup(SocialSignupRequest request) {
		if (request == null || request.completionToken() == null || request.completionToken().isBlank()) {
			throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_TOKEN_INVALID, "소셜 회원가입 완료 토큰이 없습니다.");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new BusinessException(ErrorCode.SOCIAL_EMAIL_REQUIRED);
		}

		PendingSocialProfile profile = parseCompletionToken(request.completionToken());
		SocialProvider provider = SocialProvider.from(profile.provider());
		MemberVO linkedMember = memberMapper.selectActiveMemberBySocialAccount(provider.name(), profile.providerUserId());
		if (linkedMember != null) {
			return authCommandService.issueTokenFor(linkedMember);
		}

		String email = request.email().trim();
		if (memberMapper.selectActiveMemberByEmail(email) != null) {
			throw new BusinessException(
				ErrorCode.SOCIAL_EMAIL_DUPLICATED,
				"이미 가입된 이메일입니다. 이메일 로그인 후 계정 연결 기능을 이용해 주세요."
			);
		}

		String name = stringValue(request.name()).isBlank()
			? defaultName(profile.name())
			: request.name().trim();
		memberMapper.insertSocialMember(email, "{SOCIAL}" + UUID.randomUUID(), name);
		MemberVO member = memberMapper.selectActiveMemberByEmail(email);
		if (member == null) {
			throw new IllegalStateException("소셜 로그인 회원 생성에 실패했습니다.");
		}

		memberMapper.insertSocialAccount(
			member.getId(),
			provider.name(),
			profile.providerUserId(),
			email,
			LocalDateTime.now()
		);
		return authCommandService.issueTokenFor(member);
	}

	private String requestAccessToken(SocialProvider provider, ProviderProperties properties, String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", properties.clientId());
		body.add("redirect_uri", properties.redirectUri());
		body.add("code", code);
		if (!properties.clientSecret().isBlank()) {
			body.add("client_secret", properties.clientSecret());
		}

		ResponseEntity<Map> response = restTemplate.exchange(
			provider.tokenUri,
			HttpMethod.POST,
			new HttpEntity<>(body, headers),
			Map.class
		);

		Object accessToken = response.getBody() == null ? null : response.getBody().get("access_token");
		if (accessToken == null || accessToken.toString().isBlank()) {
			throw new IllegalStateException("소셜 로그인 토큰 발급에 실패했습니다.");
		}
		return accessToken.toString();
	}

	private SocialProfile requestProfile(SocialProvider provider, String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		ResponseEntity<Map> response = restTemplate.exchange(
			provider.profileUri,
			HttpMethod.GET,
			new HttpEntity<>(headers),
			Map.class
		);
		Map<?, ?> body = response.getBody();
		if (body == null) {
			throw new IllegalStateException("소셜 로그인 프로필 조회에 실패했습니다.");
		}

		return switch (provider) {
			case KAKAO -> toKakaoProfile(body);
			case NAVER -> toNaverProfile(body);
			case GOOGLE -> toGoogleProfile(body);
		};
	}

	private SocialProfile toKakaoProfile(Map<?, ?> body) {
		String providerUserId = stringValue(body.get("id"));
		Map<?, ?> account = mapValue(body.get("kakao_account"));
		Map<?, ?> profile = mapValue(account.get("profile"));
		return new SocialProfile(providerUserId, stringValue(account.get("email")), stringValue(profile.get("nickname")));
	}

	private SocialProfile toNaverProfile(Map<?, ?> body) {
		Map<?, ?> response = mapValue(body.get("response"));
		return new SocialProfile(
			stringValue(response.get("id")),
			stringValue(response.get("email")),
			stringValue(response.get("name"))
		);
	}

	private SocialProfile toGoogleProfile(Map<?, ?> body) {
		return new SocialProfile(
			stringValue(body.get("sub")),
			stringValue(body.get("email")),
			stringValue(body.get("name"))
		);
	}

	private MemberVO findOrCreateMember(SocialProvider provider, SocialProfile profile) {
		if (profile.providerUserId().isBlank()) {
			throw new IllegalStateException("소셜 로그인 사용자 식별자를 확인할 수 없습니다.");
		}

		MemberVO member = memberMapper.selectActiveMemberBySocialAccount(provider.name(), profile.providerUserId());
		if (member != null) {
			return member;
		}

		if (profile.email().isBlank()) {
			return null;
		}

		String email = profile.email();
		member = memberMapper.selectActiveMemberByEmail(email);
		if (member == null) {
			memberMapper.insertSocialMember(
				email,
				"{SOCIAL}" + UUID.randomUUID(),
				defaultName(profile.name())
			);
			member = memberMapper.selectActiveMemberByEmail(email);
		}
		if (member == null) {
			throw new IllegalStateException("소셜 로그인 회원 생성에 실패했습니다.");
		}

		memberMapper.insertSocialAccount(
			member.getId(),
			provider.name(),
			profile.providerUserId(),
			email,
			LocalDateTime.now()
		);
		return member;
	}

	private ProviderProperties requireConfigured(SocialProvider provider) {
		ProviderProperties properties = providers.get(provider);
		if (properties.clientId().isBlank()) {
			throw new IllegalStateException(provider.displayName + " 로그인 Client ID가 설정되지 않았습니다.");
		}
		if (provider.requiresClientSecret && properties.clientSecret().isBlank()) {
			throw new IllegalStateException(provider.displayName + " 로그인 Client Secret이 설정되지 않았습니다.");
		}
		return properties;
	}

	public URI buildFrontendErrorRedirect(String message) {
		String fragment = UriComponentsBuilder.newInstance()
			.queryParam("error", message)
			.build()
			.getQuery();
		return URI.create(frontendCallbackUrl + "#" + fragment);
	}

	private URI buildFrontendProfileRequiredRedirect(SocialProvider provider, SocialProfile profile) {
		String fragment = UriComponentsBuilder.newInstance()
			.queryParam("profileRequired", "true")
			.queryParam("provider", provider.pathName)
			.queryParam("name", profile.name())
			.queryParam("completionToken", generateCompletionToken(provider, profile))
			.build()
			.getQuery();
		return URI.create(frontendCallbackUrl + "#" + fragment);
	}

	private String generateCompletionToken(SocialProvider provider, SocialProfile profile) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("type", COMPLETION_TOKEN_TYPE);
		claims.put("provider", provider.pathName);
		claims.put("providerUserId", profile.providerUserId());
		claims.put("name", profile.name());

		return Jwts.builder()
			.claims(claims)
			.subject(profile.providerUserId())
			.issuedAt(new java.util.Date(System.currentTimeMillis()))
			.expiration(new java.util.Date(System.currentTimeMillis() + COMPLETION_TOKEN_VALIDITY_MILLIS))
			.signWith(getSecretKey())
			.compact();
	}

	private PendingSocialProfile parseCompletionToken(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

			if (!COMPLETION_TOKEN_TYPE.equals(stringValue(claims.get("type")))) {
				throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_TOKEN_INVALID);
			}

			String provider = stringValue(claims.get("provider"));
			String providerUserId = stringValue(claims.get("providerUserId"));
			if (provider.isBlank() || providerUserId.isBlank()) {
				throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_TOKEN_INVALID, "소셜 회원가입 완료 토큰 정보가 부족합니다.");
			}
			return new PendingSocialProfile(provider, providerUserId, stringValue(claims.get("name")));
		} catch (JwtException | BusinessException e) {
			throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_TOKEN_INVALID, "소셜 회원가입 완료 토큰을 확인할 수 없습니다.", e);
		}
	}

	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	private String defaultName(String name) {
		return name.isBlank() ? "소셜 로그인 사용자" : name;
	}

	private Map<?, ?> mapValue(Object value) {
		if (value instanceof Map<?, ?> map) {
			return map;
		}
		return new HashMap<>();
	}

	private String stringValue(Object value) {
		return value == null ? "" : value.toString();
	}

	private record ProviderProperties(String clientId, String clientSecret, String redirectUri) {
	}

	private record SocialProfile(String providerUserId, String email, String name) {
	}

	private record PendingSocialProfile(String provider, String providerUserId, String name) {
	}

	private enum SocialProvider {
		KAKAO(
			"kakao",
			"카카오",
			"https://kauth.kakao.com/oauth/authorize",
			"https://kauth.kakao.com/oauth/token",
			"https://kapi.kakao.com/v2/user/me",
			false
		),
		NAVER(
			"naver",
			"네이버",
			"https://nid.naver.com/oauth2.0/authorize",
			"https://nid.naver.com/oauth2.0/token",
			"https://openapi.naver.com/v1/nid/me",
			true
		),
		GOOGLE(
			"google",
			"구글",
			"https://accounts.google.com/o/oauth2/v2/auth",
			"https://oauth2.googleapis.com/token",
			"https://openidconnect.googleapis.com/v1/userinfo",
			true
		);

		private final String pathName;
		private final String displayName;
		private final String authorizationUri;
		private final String tokenUri;
		private final String profileUri;
		private final boolean requiresClientSecret;

		SocialProvider(
			String pathName,
			String displayName,
			String authorizationUri,
			String tokenUri,
			String profileUri,
			boolean requiresClientSecret
		) {
			this.pathName = pathName;
			this.displayName = displayName;
			this.authorizationUri = authorizationUri;
			this.tokenUri = tokenUri;
			this.profileUri = profileUri;
			this.requiresClientSecret = requiresClientSecret;
		}

		private static SocialProvider from(String providerName) {
			for (SocialProvider provider : values()) {
				if (provider.pathName.equalsIgnoreCase(providerName)) {
					return provider;
				}
			}
			throw new BusinessException(ErrorCode.SOCIAL_PROVIDER_UNSUPPORTED);
		}
	}
}
