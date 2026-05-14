package egovframework.healthcenter.office.application;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.office.dto.ServiceTypeCreateRequest;
import egovframework.healthcenter.office.dto.ServiceTypeResponse;
import egovframework.healthcenter.office.dto.ServiceTypeUpdateRequest;
import egovframework.healthcenter.office.mapper.OfficeMapper;
import egovframework.healthcenter.office.mapper.ServiceTypeVO;

@Service
public class OfficeCommandService {

	private static final long DEFAULT_HEALTH_CENTER_ID = 1L;

	private final OfficeMapper officeMapper;

	public OfficeCommandService(OfficeMapper officeMapper) {
		this.officeMapper = officeMapper;
	}

	@Transactional
	public ServiceTypeResponse createServiceType(ServiceTypeCreateRequest request) {
		validateCreateRequest(request);
		try {
			officeMapper.insertServiceType(DEFAULT_HEALTH_CENTER_ID, request);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("이미 등록된 업무 코드입니다.", e);
		}

		ServiceTypeVO serviceType = officeMapper.selectServiceTypeById(selectCreatedServiceTypeId(request.code()));
		return ServiceTypeResponse.from(serviceType);
	}

	@Transactional
	public ServiceTypeResponse updateServiceType(Long serviceTypeId, ServiceTypeUpdateRequest request) {
		validateServiceTypeId(serviceTypeId);
		validateUpdateRequest(request);

		int updated = officeMapper.updateServiceType(serviceTypeId, request);
		if (updated == 0) {
			throw new IllegalArgumentException("업무 유형을 찾을 수 없습니다.");
		}

		return ServiceTypeResponse.from(officeMapper.selectServiceTypeById(serviceTypeId));
	}

	@Transactional
	public ServiceTypeResponse deactivateServiceType(Long serviceTypeId) {
		validateServiceTypeId(serviceTypeId);

		int updated = officeMapper.deactivateServiceType(serviceTypeId);
		if (updated == 0) {
			throw new IllegalArgumentException("업무 유형을 찾을 수 없습니다.");
		}

		return ServiceTypeResponse.from(officeMapper.selectServiceTypeById(serviceTypeId));
	}

	@Transactional
	public ServiceTypeResponse activateServiceType(Long serviceTypeId) {
		validateServiceTypeId(serviceTypeId);

		int updated = officeMapper.activateServiceType(serviceTypeId);
		if (updated == 0) {
			throw new IllegalArgumentException("업무 유형을 찾을 수 없습니다.");
		}

		return ServiceTypeResponse.from(officeMapper.selectServiceTypeById(serviceTypeId));
	}

	private Long selectCreatedServiceTypeId(String code) {
		return officeMapper.selectAllServiceTypes()
			.stream()
			.filter(serviceType -> code.equals(serviceType.getCode()))
			.findFirst()
			.map(ServiceTypeVO::getId)
			.orElseThrow(() -> new IllegalStateException("생성된 업무 유형을 조회할 수 없습니다."));
	}

	private void validateCreateRequest(ServiceTypeCreateRequest request) {
		if (request == null || isBlank(request.code()) || isBlank(request.name())) {
			throw new IllegalArgumentException("업무 코드와 업무명을 입력하세요.");
		}
		validateDefaultCapacity(request.defaultCapacity());
	}

	private void validateUpdateRequest(ServiceTypeUpdateRequest request) {
		if (request == null || isBlank(request.name())) {
			throw new IllegalArgumentException("업무명을 입력하세요.");
		}
		if (request.active() == null) {
			throw new IllegalArgumentException("사용 여부를 입력하세요.");
		}
		validateDefaultCapacity(request.defaultCapacity());
	}

	private void validateDefaultCapacity(Integer defaultCapacity) {
		if (defaultCapacity == null || defaultCapacity < 1) {
			throw new IllegalArgumentException("기본 예약 가능 인원은 1명 이상이어야 합니다.");
		}
	}

	private void validateServiceTypeId(Long serviceTypeId) {
		if (serviceTypeId == null || serviceTypeId < 1) {
			throw new IllegalArgumentException("업무 유형 ID가 올바르지 않습니다.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
