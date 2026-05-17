package egovframework.healthcenter.common.response;

import org.slf4j.MDC;

import egovframework.healthcenter.common.exception.ErrorCode;
import egovframework.healthcenter.common.logging.RequestTraceConstants;

public record ApiResponse<T>(
	boolean success,
	T data,
	ApiError error
) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> failure(String code, String message) {
		return new ApiResponse<>(false, null, new ApiError(code, message, MDC.get(RequestTraceConstants.TRACE_ID)));
	}

	public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
		return failure(errorCode.code(), errorCode.message());
	}

	public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message) {
		return failure(errorCode.code(), message);
	}
}
