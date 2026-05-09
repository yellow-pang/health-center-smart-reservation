package egovframework.healthcenter.member.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.member.dto.LoginRequest;
import egovframework.healthcenter.member.dto.LoginResponse;
import egovframework.healthcenter.member.dto.MemberResponse;
import egovframework.healthcenter.member.mapper.MemberMapper;
import egovframework.healthcenter.member.mapper.MemberVO;
import egovframework.healthcenter.member.security.HealthcenterJwtTokenProvider;
import egovframework.let.utl.sim.service.EgovFileScrty;

@Service
public class AuthCommandService {

	private static final int REFRESH_TOKEN_VALIDITY_DAYS = 14;

	private final MemberMapper memberMapper;
	private final HealthcenterJwtTokenProvider jwtTokenProvider;

	public AuthCommandService(MemberMapper memberMapper, HealthcenterJwtTokenProvider jwtTokenProvider) {
		this.memberMapper = memberMapper;
		this.jwtTokenProvider = jwtTokenProvider;
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
			LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
		);

		return new LoginResponse(accessToken, refreshToken, MemberResponse.from(member));
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
