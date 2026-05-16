package egovframework.healthcenter.member.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.dto.LoginRequest;
import egovframework.healthcenter.member.dto.LoginResponse;
import egovframework.healthcenter.member.dto.LogoutRequest;
import egovframework.healthcenter.member.dto.MemberResponse;
import egovframework.healthcenter.member.dto.ReissueTokenRequest;
import egovframework.healthcenter.member.mapper.MemberMapper;
import egovframework.healthcenter.member.mapper.MemberVO;
import egovframework.healthcenter.member.security.MemberPrincipal;
import egovframework.healthcenter.member.security.HealthcenterJwtTokenProvider;
import egovframework.let.utl.sim.service.EgovFileScrty;

@Service
public class AuthCommandService {

	private final MemberMapper memberMapper;
	private final HealthcenterJwtTokenProvider jwtTokenProvider;
	private final long refreshTokenValiditySeconds;

	public AuthCommandService(
		MemberMapper memberMapper,
		HealthcenterJwtTokenProvider jwtTokenProvider,
		@Value("${Globals.jwt.refresh-token-validity-seconds:1209600}") long refreshTokenValiditySeconds
	) {
		this.memberMapper = memberMapper;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		if (request == null || isBlank(request.email()) || isBlank(request.password())) {
			throw new IllegalArgumentException("이메일과 비밀번호를 입력하세요.");
		}

		MemberVO member = memberMapper.selectActiveMemberByEmail(request.email());
		if (member == null || !matchesPassword(request.password(), member)) {
			throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		String accessToken = jwtTokenProvider.generateAccessToken(member);
		String refreshToken = UUID.randomUUID().toString();
		memberMapper.insertRefreshToken(
			member.getId(),
			refreshToken,
			LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds)
		);

		return new LoginResponse(accessToken, refreshToken, MemberResponse.from(member));
	}

	@Transactional
	public LoginResponse reissue(ReissueTokenRequest request) {
		if (request == null || isBlank(request.refreshToken())) {
			throw new IllegalArgumentException("Refresh Token을 입력하세요.");
		}

		MemberVO member = memberMapper.selectActiveMemberByRefreshToken(request.refreshToken());
		if (member == null) {
			throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
		}

		memberMapper.revokeRefreshTokenByToken(request.refreshToken());

		String accessToken = jwtTokenProvider.generateAccessToken(member);
		String refreshToken = UUID.randomUUID().toString();
		memberMapper.insertRefreshToken(
			member.getId(),
			refreshToken,
			LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds)
		);

		return new LoginResponse(accessToken, refreshToken, MemberResponse.from(member));
	}

	@Transactional
	public void logout(MemberPrincipal principal, LogoutRequest request) {
		if (principal == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
		if (request == null || isBlank(request.refreshToken())) {
			throw new IllegalArgumentException("Refresh Token을 입력하세요.");
		}

		int updated = memberMapper.revokeMemberRefreshToken(principal.memberId(), request.refreshToken());
		if (updated == 0) {
			throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
		}
	}

	private boolean matchesPassword(String rawPassword, MemberVO member) {
		try {
			String encryptedPassword = EgovFileScrty.encryptPassword(rawPassword, member.getEmail());
			return encryptedPassword.equals(member.getPassword());
		} catch (Exception e) {
			throw new IllegalStateException("비밀번호 검증 중 오류가 발생했습니다.", e);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
