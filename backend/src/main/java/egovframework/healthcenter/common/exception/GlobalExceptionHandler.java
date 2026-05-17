package egovframework.healthcenter.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import egovframework.healthcenter.common.logging.AuditLogSupport;
import egovframework.healthcenter.common.response.ApiResponse;

@RestControllerAdvice(basePackages = "egovframework.healthcenter")
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.errorCode();
		log.warn(
			"event=api.business_exception traceId={} errorCode={} message={}",
			AuditLogSupport.traceId(),
			errorCode.code(),
			exception.getMessage()
		);
		return ResponseEntity.status(errorCode.status())
			.body(ApiResponse.failure(errorCode, exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
		ErrorCode errorCode = ErrorCode.fromIllegalArgument(exception);
		log.warn(
			"event=api.invalid_request traceId={} errorCode={} message={}",
			AuditLogSupport.traceId(),
			errorCode.code(),
			exception.getMessage()
		);
		return ResponseEntity.status(errorCode.status())
			.body(ApiResponse.failure(errorCode, exception.getMessage()));
	}

	@ExceptionHandler({
		MissingServletRequestParameterException.class,
		MethodArgumentTypeMismatchException.class,
		HttpMessageNotReadableException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
		log.warn(
			"event=api.bad_request traceId={} errorCode={} message={}",
			AuditLogSupport.traceId(),
			ErrorCode.INVALID_REQUEST.code(),
			exception.getMessage()
		);
		return ResponseEntity.status(ErrorCode.INVALID_REQUEST.status())
			.body(ApiResponse.failure(ErrorCode.INVALID_REQUEST));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		log.error(
			"event=api.unhandled_exception traceId={} errorCode={}",
			AuditLogSupport.traceId(),
			ErrorCode.INTERNAL_SERVER_ERROR.code(),
			exception
		);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status())
			.body(ApiResponse.failure(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
