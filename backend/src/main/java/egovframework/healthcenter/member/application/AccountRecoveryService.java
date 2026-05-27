package egovframework.healthcenter.member.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.common.exception.BusinessException;
import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.member.dto.FindIdRequest;
import egovframework.healthcenter.member.dto.FindIdResponse;
import egovframework.healthcenter.member.dto.PasswordResetConfirmRequest;
import egovframework.healthcenter.member.dto.PasswordResetConfirmResponse;
import egovframework.healthcenter.member.dto.PasswordResetRequest;
import egovframework.healthcenter.member.dto.PasswordResetRequestResponse;
import egovframework.healthcenter.member.mapper.MemberMapper;
import egovframework.healthcenter.member.mapper.MemberVO;
import egovframework.let.utl.sim.service.EgovFileScrty;

@Service
public class AccountRecoveryService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int TOKEN_BYTE_LENGTH = 32;

	private final MemberMapper memberMapper;
	private final long resetTokenValiditySeconds;
	private final boolean exposeDevelopmentResetToken;

	public AccountRecoveryService(
		MemberMapper memberMapper,
		@Value("${Healthcenter.AccountRecovery.ResetTokenValiditySeconds:1800}") long resetTokenValiditySeconds,
		@Value("${Healthcenter.AccountRecovery.ExposeDevelopmentResetToken:true}") boolean exposeDevelopmentResetToken
	) {
		this.memberMapper = memberMapper;
		this.resetTokenValiditySeconds = resetTokenValiditySeconds;
		this.exposeDevelopmentResetToken = exposeDevelopmentResetToken;
	}

	@Transactional(readOnly = true)
	public FindIdResponse findId(FindIdRequest request) {
		if (request == null || isBlank(request.name()) || isBlank(request.phone())) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "이름과 휴대폰 번호를 입력하세요.");
		}

		MemberVO member = memberMapper.selectActiveMemberByNameAndPhone(
			request.name().trim(),
			normalizePhone(request.phone())
		);
		if (member == null) {
			return new FindIdResponse(false, null);
		}
		return new FindIdResponse(true, maskEmail(member.getEmail()));
	}

	@Transactional
	public PasswordResetRequestResponse requestPasswordReset(PasswordResetRequest request) {
		if (request == null || isBlank(request.email()) || isBlank(request.phone())) {
			throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_INVALID_REQUEST, "이메일과 휴대폰 번호를 입력하세요.");
		}

		MemberVO member = memberMapper.selectActiveMemberByEmailAndPhone(
			request.email().trim(),
			normalizePhone(request.phone())
		);
		if (member == null) {
			return new PasswordResetRequestResponse(true, null);
		}

		String resetToken = generateToken();
		memberMapper.markMemberPasswordResetTokensUsed(member.getId());
		memberMapper.insertPasswordResetToken(
			member.getId(),
			hashToken(resetToken),
			LocalDateTime.now().plusSeconds(resetTokenValiditySeconds)
		);

		String developmentResetToken = exposeDevelopmentResetToken ? resetToken : null;
		return new PasswordResetRequestResponse(true, developmentResetToken);
	}

	@Transactional
	public PasswordResetConfirmResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
		validateConfirmRequest(request);

		String tokenHash = hashToken(request.resetToken());
		MemberVO member = memberMapper.selectActiveMemberByPasswordResetToken(tokenHash);
		if (member == null) {
			throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID);
		}

		String encryptedPassword = encryptPassword(request.newPassword(), member.getEmail());
		int updated = memberMapper.updateMemberPassword(member.getId(), encryptedPassword);
		int used = memberMapper.markPasswordResetTokenUsed(tokenHash);
		if (updated == 0 || used == 0) {
			throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_INVALID);
		}
		memberMapper.revokeMemberRefreshTokens(member.getId());

		return new PasswordResetConfirmResponse(true);
	}

	private void validateConfirmRequest(PasswordResetConfirmRequest request) {
		if (request == null || isBlank(request.resetToken()) || isBlank(request.newPassword())
			|| isBlank(request.newPasswordConfirm())) {
			throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_INVALID_REQUEST, "재설정 토큰과 새 비밀번호를 입력하세요.");
		}
		if (!request.newPassword().equals(request.newPasswordConfirm())) {
			throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_INVALID_REQUEST, "새 비밀번호 확인이 일치하지 않습니다.");
		}
		if (request.newPassword().length() < 8) {
			throw new BusinessException(ErrorCode.AUTH_PASSWORD_RESET_INVALID_REQUEST, "새 비밀번호는 8자 이상이어야 합니다.");
		}
	}

	private String generateToken() {
		byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashed);
		} catch (Exception e) {
			throw new IllegalStateException("비밀번호 재설정 토큰 처리 중 오류가 발생했습니다.", e);
		}
	}

	private String encryptPassword(String rawPassword, String email) {
		try {
			return EgovFileScrty.encryptPassword(rawPassword, email);
		} catch (Exception e) {
			throw new IllegalStateException("비밀번호 암호화 중 오류가 발생했습니다.", e);
		}
	}

	private String maskEmail(String email) {
		if (isBlank(email) || !email.contains("@")) {
			return "";
		}
		String[] parts = email.split("@", 2);
		String local = parts[0];
		String domain = parts[1];
		if (local.isBlank()) {
			return "***@" + domain;
		}
		if (local.length() <= 2) {
			return local.charAt(0) + "***@" + domain;
		}
		return local.substring(0, 2) + "***@" + domain;
	}

	private String normalizePhone(String phone) {
		return phone == null ? "" : phone.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
