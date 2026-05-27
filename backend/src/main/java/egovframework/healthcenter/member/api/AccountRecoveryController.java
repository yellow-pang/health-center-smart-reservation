package egovframework.healthcenter.member.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.healthcenter.common.response.ApiResponse;
import egovframework.healthcenter.member.application.AccountRecoveryService;
import egovframework.healthcenter.member.dto.FindIdRequest;
import egovframework.healthcenter.member.dto.FindIdResponse;
import egovframework.healthcenter.member.dto.PasswordResetConfirmRequest;
import egovframework.healthcenter.member.dto.PasswordResetConfirmResponse;
import egovframework.healthcenter.member.dto.PasswordResetRequest;
import egovframework.healthcenter.member.dto.PasswordResetRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "AccountRecoveryController", description = "보건소 계정 찾기")
public class AccountRecoveryController {

	private final AccountRecoveryService accountRecoveryService;

	public AccountRecoveryController(AccountRecoveryService accountRecoveryService) {
		this.accountRecoveryService = accountRecoveryService;
	}

	@PostMapping("/find-id")
	@Operation(summary = "아이디 찾기", description = "이름과 휴대폰 번호로 가입 이메일을 마스킹해 조회한다.")
	public ApiResponse<FindIdResponse> findId(@RequestBody FindIdRequest request) {
		return ApiResponse.success(accountRecoveryService.findId(request));
	}

	@PostMapping("/password-reset/request")
	@Operation(summary = "비밀번호 재설정 요청", description = "가입 이메일과 휴대폰 번호를 확인하고 비밀번호 재설정 토큰을 발급한다.")
	public ApiResponse<PasswordResetRequestResponse> requestPasswordReset(@RequestBody PasswordResetRequest request) {
		return ApiResponse.success(accountRecoveryService.requestPasswordReset(request));
	}

	@PostMapping("/password-reset/confirm")
	@Operation(summary = "비밀번호 재설정 완료", description = "재설정 토큰을 검증하고 새 비밀번호로 변경한다.")
	public ApiResponse<PasswordResetConfirmResponse> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
		return ApiResponse.success(accountRecoveryService.confirmPasswordReset(request));
	}
}
