package egovframework.healthcenter.common.response;

public record ApiError(
	String code,
	String message,
	String traceId
) {
}
