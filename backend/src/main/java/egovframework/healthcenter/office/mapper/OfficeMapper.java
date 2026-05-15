package egovframework.healthcenter.office.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import egovframework.healthcenter.office.dto.ServiceTypeCreateRequest;
import egovframework.healthcenter.office.dto.ServiceTypeUpdateRequest;
import egovframework.healthcenter.office.dto.ServiceWindowUpsertRequest;
import egovframework.healthcenter.office.dto.StaffCreateRequest;
import egovframework.healthcenter.office.dto.StaffUpdateRequest;

@Repository("officeMapper")
public class OfficeMapper extends EgovAbstractMapper {

	public List<ServiceTypeVO> selectActiveServiceTypes() {
		return selectList("OfficeMapper.selectActiveServiceTypes");
	}

	public List<ServiceTypeVO> selectAllServiceTypes() {
		return selectList("OfficeMapper.selectAllServiceTypes");
	}

	public ServiceTypeVO selectServiceTypeById(Long serviceTypeId) {
		return selectOne("OfficeMapper.selectServiceTypeById", serviceTypeId);
	}

	public void insertServiceType(Long healthCenterId, ServiceTypeCreateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("code", request.code());
		params.put("name", request.name());
		params.put("description", request.description());
		params.put("defaultCapacity", request.defaultCapacity());
		insert("OfficeMapper.insertServiceType", params);
	}

	public int updateServiceType(Long serviceTypeId, ServiceTypeUpdateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("serviceTypeId", serviceTypeId);
		params.put("name", request.name());
		params.put("description", request.description());
		params.put("defaultCapacity", request.defaultCapacity());
		params.put("active", request.active());
		return update("OfficeMapper.updateServiceType", params);
	}

	public int deactivateServiceType(Long serviceTypeId) {
		return update("OfficeMapper.deactivateServiceType", serviceTypeId);
	}

	public int activateServiceType(Long serviceTypeId) {
		return update("OfficeMapper.activateServiceType", serviceTypeId);
	}

	public List<StaffVO> selectActiveStaff() {
		return selectList("OfficeMapper.selectActiveStaff");
	}

	public StaffVO selectStaffById(Long staffId) {
		return selectOne("OfficeMapper.selectStaffById", staffId);
	}

	public StaffVO selectStaffByEmail(String email) {
		return selectOne("OfficeMapper.selectStaffByEmail", email);
	}

	public void insertStaff(Long healthCenterId, String encryptedPassword, StaffCreateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("email", request.email());
		params.put("password", encryptedPassword);
		params.put("name", request.name());
		params.put("phone", request.phone());
		insert("OfficeMapper.insertStaff", params);
	}

	public int updateStaff(Long staffId, StaffUpdateRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("staffId", staffId);
		params.put("name", request.name());
		params.put("phone", request.phone());
		params.put("active", request.active());
		return update("OfficeMapper.updateStaff", params);
	}

	public int deactivateStaff(Long staffId) {
		return update("OfficeMapper.deactivateStaff", staffId);
	}

	public List<ServiceWindowMappingVO> selectServiceWindowMappings() {
		return selectList("OfficeMapper.selectServiceWindowMappings");
	}

	public List<ServiceWindowMappingVO> selectServiceWindowMappingsById(Long serviceWindowId) {
		return selectList("OfficeMapper.selectServiceWindowMappingsById", serviceWindowId);
	}

	public Long selectServiceWindowIdByNumber(Long healthCenterId, Integer windowNumber) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("windowNumber", windowNumber);
		return selectOne("OfficeMapper.selectServiceWindowIdByNumber", params);
	}

	public void insertServiceWindow(Long healthCenterId, ServiceWindowUpsertRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("healthCenterId", healthCenterId);
		params.put("windowNumber", request.windowNumber());
		params.put("name", request.name());
		params.put("status", request.status());
		params.put("active", request.active());
		params.put("staffId", request.staffId());
		insert("OfficeMapper.insertServiceWindow", params);
	}

	public int updateServiceWindow(Long serviceWindowId, ServiceWindowUpsertRequest request) {
		Map<String, Object> params = new HashMap<>();
		params.put("serviceWindowId", serviceWindowId);
		params.put("windowNumber", request.windowNumber());
		params.put("name", request.name());
		params.put("status", request.status());
		params.put("active", request.active());
		params.put("staffId", request.staffId());
		return update("OfficeMapper.updateServiceWindow", params);
	}

	public int deactivateServiceWindow(Long serviceWindowId) {
		return update("OfficeMapper.deactivateServiceWindow", serviceWindowId);
	}

	public int deactivateServiceWindowServiceTypes(Long serviceWindowId) {
		return update("OfficeMapper.deactivateServiceWindowServiceTypes", serviceWindowId);
	}

	public void upsertServiceWindowServiceType(Long serviceWindowId, Long serviceTypeId) {
		Map<String, Object> params = new HashMap<>();
		params.put("serviceWindowId", serviceWindowId);
		params.put("serviceTypeId", serviceTypeId);
		insert("OfficeMapper.upsertServiceWindowServiceType", params);
	}
}
