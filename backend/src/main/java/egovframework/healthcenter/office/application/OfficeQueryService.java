package egovframework.healthcenter.office.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.healthcenter.office.dto.ServiceTypeResponse;
import egovframework.healthcenter.office.dto.ServiceWindowResponse;
import egovframework.healthcenter.office.dto.StaffResponse;
import egovframework.healthcenter.office.mapper.OfficeMapper;
import egovframework.healthcenter.office.mapper.ServiceWindowMappingVO;

@Service
@Transactional(readOnly = true)
public class OfficeQueryService {

	private final OfficeMapper officeMapper;

	public OfficeQueryService(OfficeMapper officeMapper) {
		this.officeMapper = officeMapper;
	}

	public List<ServiceTypeResponse> findActiveServiceTypes() {
		return officeMapper.selectActiveServiceTypes()
			.stream()
			.map(ServiceTypeResponse::from)
			.toList();
	}

	public List<ServiceTypeResponse> findAllServiceTypes() {
		return officeMapper.selectAllServiceTypes()
			.stream()
			.map(ServiceTypeResponse::from)
			.toList();
	}

	public List<StaffResponse> findActiveStaff() {
		return officeMapper.selectActiveStaff()
			.stream()
			.map(StaffResponse::from)
			.toList();
	}

	public List<ServiceWindowResponse> findServiceWindows() {
		Map<Long, ServiceWindowBuilder> windows = new LinkedHashMap<>();

		for (ServiceWindowMappingVO row : officeMapper.selectServiceWindowMappings()) {
			ServiceWindowBuilder builder = windows.computeIfAbsent(row.getWindowId(), id -> new ServiceWindowBuilder(row));
			if (row.getServiceTypeId() != null) {
				builder.addServiceType(row);
			}
		}

		return windows.values()
			.stream()
			.map(ServiceWindowBuilder::build)
			.toList();
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
}
