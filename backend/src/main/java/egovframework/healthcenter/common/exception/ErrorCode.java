package egovframework.healthcenter.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 값이 올바르지 않습니다."),
	AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "로그인이 필요합니다."),
	AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
	AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_INVALID", "Refresh Token이 유효하지 않습니다."),
	AUTH_PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_RESET_TOKEN_INVALID", "비밀번호 재설정 토큰이 올바르지 않습니다."),
	AUTH_PASSWORD_RESET_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_RESET_INVALID_REQUEST", "비밀번호 재설정 요청 값이 올바르지 않습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "요청을 처리할 권한이 없습니다."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 정보를 찾을 수 없습니다."),
	CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "현재 상태에서는 요청을 처리할 수 없습니다."),
	SERVICE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "SERVICE_TYPE_NOT_FOUND", "업무 유형을 찾을 수 없습니다."),
	SERVICE_TYPE_DUPLICATED(HttpStatus.CONFLICT, "SERVICE_TYPE_DUPLICATED", "이미 등록된 업무 코드입니다."),
	SERVICE_TYPE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "SERVICE_TYPE_INVALID_REQUEST", "업무 유형 요청 값이 올바르지 않습니다."),
	STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND", "직원을 찾을 수 없습니다."),
	STAFF_DUPLICATED(HttpStatus.CONFLICT, "STAFF_DUPLICATED", "이미 등록된 직원 이메일입니다."),
	STAFF_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "STAFF_INVALID_REQUEST", "직원 요청 값이 올바르지 않습니다."),
	SERVICE_WINDOW_NOT_FOUND(HttpStatus.NOT_FOUND, "SERVICE_WINDOW_NOT_FOUND", "창구를 찾을 수 없습니다."),
	SERVICE_WINDOW_DUPLICATED(HttpStatus.CONFLICT, "SERVICE_WINDOW_DUPLICATED", "이미 등록된 창구 번호입니다."),
	SERVICE_WINDOW_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "SERVICE_WINDOW_INVALID_REQUEST", "창구 요청 값이 올바르지 않습니다."),
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "예약 정보를 찾을 수 없습니다."),
	RESERVATION_SLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_SLOT_NOT_FOUND", "예약 슬롯을 찾을 수 없습니다."),
	RESERVATION_SLOT_FULL(HttpStatus.CONFLICT, "RESERVATION_SLOT_FULL", "선택한 시간대의 예약이 마감되었습니다."),
	RESERVATION_DUPLICATED(HttpStatus.CONFLICT, "RESERVATION_DUPLICATED", "동일 시간대에 이미 예약이 존재합니다."),
	RESERVATION_CANCEL_TIME_EXPIRED(HttpStatus.CONFLICT, "RESERVATION_CANCEL_TIME_EXPIRED", "예약 취소 가능 시간이 지났습니다."),
	RESERVATION_CANCEL_INVALID_STATUS(HttpStatus.CONFLICT, "RESERVATION_CANCEL_INVALID_STATUS", "현재 상태에서는 예약을 취소할 수 없습니다."),
	RESERVATION_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "RESERVATION_INVALID_REQUEST", "예약 요청 값이 올바르지 않습니다."),
	RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "RESERVATION_FORBIDDEN", "예약을 처리할 권한이 없습니다."),
	RESERVATION_SLOT_DUPLICATED(HttpStatus.CONFLICT, "RESERVATION_SLOT_DUPLICATED", "이미 등록된 예약 슬롯입니다."),
	RESERVATION_SLOT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "RESERVATION_SLOT_INVALID_REQUEST", "예약 슬롯 요청 값이 올바르지 않습니다."),
	VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "방문 정보를 찾을 수 없습니다."),
	VISIT_FORBIDDEN(HttpStatus.FORBIDDEN, "VISIT_FORBIDDEN", "방문을 처리할 권한이 없습니다."),
	VISIT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "VISIT_INVALID_REQUEST", "방문 요청 값이 올바르지 않습니다."),
	VISIT_ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "VISIT_ALREADY_CHECKED_IN", "이미 체크인했거나 체크인할 수 없는 예약입니다."),
	ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "이미 체크인했거나 체크인할 수 없는 예약입니다."),
	QUEUE_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "QUEUE_TICKET_NOT_FOUND", "대기표 정보를 찾을 수 없습니다."),
	QUEUE_FORBIDDEN(HttpStatus.FORBIDDEN, "QUEUE_FORBIDDEN", "대기열을 처리할 권한이 없습니다."),
	QUEUE_INVALID_STATUS(HttpStatus.CONFLICT, "QUEUE_INVALID_STATUS", "현재 대기 상태에서는 요청을 처리할 수 없습니다."),
	QUEUE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "QUEUE_INVALID_REQUEST", "대기열 요청 값이 올바르지 않습니다."),
	DASHBOARD_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "DASHBOARD_INVALID_REQUEST", "대시보드 요청 값이 올바르지 않습니다."),
	DASHBOARD_FORBIDDEN(HttpStatus.FORBIDDEN, "DASHBOARD_FORBIDDEN", "대시보드 조회 권한이 없습니다."),
	SOCIAL_SIGNUP_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "SOCIAL_SIGNUP_TOKEN_INVALID", "소셜 회원가입 완료 토큰이 올바르지 않습니다."),
	SOCIAL_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "SOCIAL_EMAIL_REQUIRED", "이메일을 입력해 주세요."),
	SOCIAL_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "SOCIAL_EMAIL_DUPLICATED", "이미 가입된 이메일입니다."),
	SOCIAL_PROVIDER_UNSUPPORTED(HttpStatus.BAD_REQUEST, "SOCIAL_PROVIDER_UNSUPPORTED", "지원하지 않는 소셜 로그인 제공자입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public String message() {
		return message;
	}

	public static ErrorCode fromIllegalArgument(IllegalArgumentException exception) {
		return fromMessage(exception.getMessage());
	}

	public static ErrorCode fromMessage(String message) {
		if (contains(message, "로그인이 필요")) {
			return AUTH_REQUIRED;
		}
		if (contains(message, "예약") && contains(message, "권한")) {
			return RESERVATION_FORBIDDEN;
		}
		if ((contains(message, "방문") || contains(message, "체크인") || contains(message, "현장 접수")) && contains(message, "권한")) {
			return VISIT_FORBIDDEN;
		}
		if ((contains(message, "대기표") || contains(message, "대기열")) && contains(message, "권한")) {
			return QUEUE_FORBIDDEN;
		}
		if (contains(message, "대시보드") && contains(message, "권한")) {
			return DASHBOARD_FORBIDDEN;
		}
		if (contains(message, "권한")) {
			return FORBIDDEN;
		}
		if (contains(message, "이메일 또는 비밀번호") || contains(message, "이메일과 비밀번호")) {
			return AUTH_INVALID_CREDENTIALS;
		}
		if (contains(message, "Refresh Token")) {
			return AUTH_REFRESH_TOKEN_INVALID;
		}
		if (contains(message, "비밀번호 재설정 토큰")) {
			return AUTH_PASSWORD_RESET_TOKEN_INVALID;
		}
		if (contains(message, "비밀번호 재설정") || contains(message, "새 비밀번호")) {
			return AUTH_PASSWORD_RESET_INVALID_REQUEST;
		}
		if (contains(message, "이미 등록된 업무 코드")) {
			return SERVICE_TYPE_DUPLICATED;
		}
		if (contains(message, "업무 유형을 찾을 수 없습니다")) {
			return SERVICE_TYPE_NOT_FOUND;
		}
		if (contains(message, "업무 유형")) {
			return SERVICE_TYPE_INVALID_REQUEST;
		}
		if (contains(message, "이미 등록된 직원 이메일")) {
			return STAFF_DUPLICATED;
		}
		if (contains(message, "직원을 찾을 수 없습니다")) {
			return STAFF_NOT_FOUND;
		}
		if (contains(message, "직원")) {
			return STAFF_INVALID_REQUEST;
		}
		if (contains(message, "이미 등록된 창구 번호")) {
			return SERVICE_WINDOW_DUPLICATED;
		}
		if (contains(message, "창구를 찾을 수 없습니다")) {
			return SERVICE_WINDOW_NOT_FOUND;
		}
		if (contains(message, "창구")) {
			return SERVICE_WINDOW_INVALID_REQUEST;
		}
		if (contains(message, "예약 정보를 찾을 수 없습니다")) {
			return RESERVATION_NOT_FOUND;
		}
		if (contains(message, "예약 슬롯을 찾을 수 없습니다")) {
			return RESERVATION_SLOT_NOT_FOUND;
		}
		if (contains(message, "이미 등록된 예약 슬롯")) {
			return RESERVATION_SLOT_DUPLICATED;
		}
		if (contains(message, "마감")) {
			return RESERVATION_SLOT_FULL;
		}
		if (contains(message, "이미 예약") || contains(message, "동일 시간대")) {
			return RESERVATION_DUPLICATED;
		}
		if (contains(message, "1시간 전")) {
			return RESERVATION_CANCEL_TIME_EXPIRED;
		}
		if (contains(message, "예약을 취소") || contains(message, "취소할 수 없는")) {
			return RESERVATION_CANCEL_INVALID_STATUS;
		}
		if (contains(message, "예약 슬롯")) {
			return RESERVATION_SLOT_INVALID_REQUEST;
		}
		if (contains(message, "예약")) {
			return RESERVATION_INVALID_REQUEST;
		}
		if (contains(message, "이미 체크인")) {
			return VISIT_ALREADY_CHECKED_IN;
		}
		if (contains(message, "방문 정보를 찾을 수 없습니다")) {
			return VISIT_NOT_FOUND;
		}
		if (contains(message, "방문") || contains(message, "체크인") || contains(message, "현장 접수")) {
			return VISIT_INVALID_REQUEST;
		}
		if (contains(message, "대기표 정보를 찾을 수 없습니다") || contains(message, "대기표를 처리할")) {
			return QUEUE_TICKET_NOT_FOUND;
		}
		if (contains(message, "대기 상태")) {
			return QUEUE_INVALID_STATUS;
		}
		if (contains(message, "대기표") || contains(message, "대기열")) {
			return QUEUE_INVALID_REQUEST;
		}
		if (contains(message, "대시보드") || contains(message, "보건소 ID")) {
			return DASHBOARD_INVALID_REQUEST;
		}
		if (contains(message, "소셜 회원가입 완료 토큰")) {
			return SOCIAL_SIGNUP_TOKEN_INVALID;
		}
		if (contains(message, "이메일을 입력")) {
			return SOCIAL_EMAIL_REQUIRED;
		}
		if (contains(message, "이미 가입된 이메일")) {
			return SOCIAL_EMAIL_DUPLICATED;
		}
		if (contains(message, "지원하지 않는 소셜 로그인 제공자")) {
			return SOCIAL_PROVIDER_UNSUPPORTED;
		}
		if (contains(message, "찾을 수 없습니다")) {
			return RESOURCE_NOT_FOUND;
		}
		if (contains(message, "상태")) {
			return CONFLICT;
		}
		return INVALID_REQUEST;
	}

	private static boolean contains(String message, String pattern) {
		return message != null && message.contains(pattern);
	}
}
