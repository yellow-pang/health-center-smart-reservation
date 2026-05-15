package egovframework.healthcenter.office.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.office.dto.ServiceTypeCreateRequest;
import egovframework.healthcenter.office.dto.ServiceTypeResponse;
import egovframework.healthcenter.office.dto.ServiceTypeUpdateRequest;
import egovframework.healthcenter.office.dto.ServiceWindowResponse;
import egovframework.healthcenter.office.dto.ServiceWindowUpsertRequest;
import egovframework.healthcenter.office.dto.StaffCreateRequest;
import egovframework.healthcenter.office.dto.StaffResponse;
import egovframework.healthcenter.office.dto.StaffUpdateRequest;
import egovframework.healthcenter.office.mapper.OfficeMapper;
import egovframework.healthcenter.office.mapper.ServiceWindowMappingVO;
import egovframework.healthcenter.office.mapper.StaffVO;
import egovframework.healthcenter.office.mapper.ServiceTypeVO;
import egovframework.let.utl.sim.service.EgovFileScrty;

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

	@Transactional
	public StaffResponse createStaff(StaffCreateRequest request) {
		validateCreateStaffRequest(request);
		try {
			officeMapper.insertStaff(
				DEFAULT_HEALTH_CENTER_ID,
				encryptPassword(request.password(), request.email()),
				request
			);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("이미 등록된 직원 이메일입니다.", e);
		}

		StaffVO staff = officeMapper.selectStaffByEmail(request.email());
		return StaffResponse.from(staff);
	}

	@Transactional
	public StaffResponse updateStaff(Long staffId, StaffUpdateRequest request) {
		validateStaffId(staffId);
		validateUpdateStaffRequest(request);

		int updated = officeMapper.updateStaff(staffId, request);
		if (updated == 0) {
			throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
		}

		return StaffResponse.from(findStaff(staffId));
	}

	@Transactional
	public StaffResponse deactivateStaff(Long staffId) {
		validateStaffId(staffId);

		int updated = officeMapper.deactivateStaff(staffId);
		if (updated == 0) {
			throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
		}

		return StaffResponse.from(findStaff(staffId));
	}

	@Transactional
	public ServiceWindowResponse createServiceWindow(ServiceWindowUpsertRequest request) {
		validateServiceWindowRequest(request);
		validateAssignableStaff(request.staffId());
		try {
			officeMapper.insertServiceWindow(DEFAULT_HEALTH_CENTER_ID, request);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("이미 등록된 창구 번호입니다.", e);
		}

		Long serviceWindowId = officeMapper.selectServiceWindowIdByNumber(
			DEFAULT_HEALTH_CENTER_ID,
			request.windowNumber()
		);
		replaceServiceWindowMappings(serviceWindowId, request.serviceTypeIds());
		return findServiceWindow(serviceWindowId);
	}

	@Transactional
	public ServiceWindowResponse updateServiceWindow(Long serviceWindowId, ServiceWindowUpsertRequest request) {
		validateServiceWindowId(serviceWindowId);
		validateServiceWindowRequest(request);
		validateAssignableStaff(request.staffId());
		try {
			int updated = officeMapper.updateServiceWindow(serviceWindowId, request);
			if (updated == 0) {
				throw new IllegalArgumentException("창구를 찾을 수 없습니다.");
			}
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("이미 등록된 창구 번호입니다.", e);
		}

		replaceServiceWindowMappings(serviceWindowId, request.serviceTypeIds());
		return findServiceWindow(serviceWindowId);
	}

	@Transactional
	public ServiceWindowResponse deactivateServiceWindow(Long serviceWindowId) {
		validateServiceWindowId(serviceWindowId);

		int updated = officeMapper.deactivateServiceWindow(serviceWindowId);
		if (updated == 0) {
			throw new IllegalArgumentException("창구를 찾을 수 없습니다.");
		}

		return findServiceWindow(serviceWindowId);
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

	private void validateCreateStaffRequest(StaffCreateRequest request) {
		if (request == null || isBlank(request.email()) || isBlank(request.password()) || isBlank(request.name())) {
			throw new IllegalArgumentException("이메일, 비밀번호, 이름을 입력하세요.");
		}
		if (isBlank(request.phone())) {
			throw new IllegalArgumentException("전화번호를 입력하세요.");
		}
	}

	private void validateUpdateStaffRequest(StaffUpdateRequest request) {
		if (request == null || isBlank(request.name())) {
			throw new IllegalArgumentException("이름을 입력하세요.");
		}
		if (isBlank(request.phone())) {
			throw new IllegalArgumentException("전화번호를 입력하세요.");
		}
		if (request.active() == null) {
			throw new IllegalArgumentException("사용 여부를 입력하세요.");
		}
	}

	private void validateStaffId(Long staffId) {
		if (staffId == null || staffId < 1) {
			throw new IllegalArgumentException("직원 ID가 올바르지 않습니다.");
		}
	}

	private StaffVO findStaff(Long staffId) {
		StaffVO staff = officeMapper.selectStaffById(staffId);
		if (staff == null) {
			throw new IllegalArgumentException("직원을 찾을 수 없습니다.");
		}
		return staff;
	}

	private void validateServiceWindowRequest(ServiceWindowUpsertRequest request) {
		if (request == null || request.windowNumber() == null || request.windowNumber() < 1) {
			throw new IllegalArgumentException("창구 번호가 올바르지 않습니다.");
		}
		if (isBlank(request.name())) {
			throw new IllegalArgumentException("창구명을 입력하세요.");
		}
		if (isBlank(request.status())) {
			throw new IllegalArgumentException("창구 상태를 입력하세요.");
		}
		if (request.active() == null) {
			throw new IllegalArgumentException("사용 여부를 입력하세요.");
		}
		if (request.serviceTypeIds() == null || request.serviceTypeIds().isEmpty()) {
			throw new IllegalArgumentException("담당 업무를 하나 이상 선택하세요.");
		}
	}

	private void validateServiceWindowId(Long serviceWindowId) {
		if (serviceWindowId == null || serviceWindowId < 1) {
			throw new IllegalArgumentException("창구 ID가 올바르지 않습니다.");
		}
	}

	private void validateAssignableStaff(Long staffId) {
		if (staffId == null) {
			return;
		}
		StaffVO staff = findStaff(staffId);
		if (!staff.isActive()) {
			throw new IllegalArgumentException("비활성 직원은 창구 담당자로 배정할 수 없습니다.");
		}
	}

	private void replaceServiceWindowMappings(Long serviceWindowId, List<Long> serviceTypeIds) {
		officeMapper.deactivateServiceWindowServiceTypes(serviceWindowId);
		for (Long serviceTypeId : serviceTypeIds) {
			if (serviceTypeId == null || serviceTypeId < 1) {
				throw new IllegalArgumentException("담당 업무 유형 ID가 올바르지 않습니다.");
			}
			officeMapper.upsertServiceWindowServiceType(serviceWindowId, serviceTypeId);
		}
	}

	private ServiceWindowResponse findServiceWindow(Long serviceWindowId) {
		Map<Long, ServiceWindowBuilder> windows = new LinkedHashMap<>();
		for (ServiceWindowMappingVO row : officeMapper.selectServiceWindowMappingsById(serviceWindowId)) {
			ServiceWindowBuilder builder = windows.computeIfAbsent(row.getWindowId(), id -> new ServiceWindowBuilder(row));
			if (row.getServiceTypeId() != null) {
				builder.addServiceType(row);
			}
		}

		return windows.values()
			.stream()
			.findFirst()
			.map(ServiceWindowBuilder::build)
			.orElseThrow(() -> new IllegalArgumentException("창구를 찾을 수 없습니다."));
	}

	private static class ServiceWindowBuilder {

		private final ServiceWindowMappingVO window;
		private final List<ServiceTypeResponse> serviceTypes = new ArrayList<>();

		ServiceWindowBuilder(ServiceWindowMappingVO window) {
			this.window = window;
		}

		void addServiceType(ServiceWindowMappingVO row) {
			serviceTypes.add(new ServiceTypeResponse(
				row.getServiceTypeId(),
				row.getHealthCenterId(),
				row.getServiceTypeCode(),
				row.getServiceTypeName(),
				row.getServiceTypeDescription(),
				row.getDefaultCapacity(),
				row.isServiceTypeActive()
			));
		}

		ServiceWindowResponse build() {
			return new ServiceWindowResponse(
				window.getWindowId(),
				window.getHealthCenterId(),
				window.getWindowNumber(),
				window.getWindowName(),
				window.getStatus(),
				window.isWindowActive(),
				window.getStaffId(),
				window.getStaffName(),
				List.copyOf(serviceTypes)
			);
		}
	}

	private String encryptPassword(String rawPassword, String email) {
		try {
			return EgovFileScrty.encryptPassword(rawPassword, email);
		} catch (Exception e) {
			throw new IllegalStateException("직원 비밀번호 암호화 중 오류가 발생했습니다.", e);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
