package io.mosip.kernel.otpmanager.service;

import io.mosip.kernel.otpmanager.entity.OtpEntity;

import java.util.Map;

public interface PersistenceService {

    public void saveOtp(OtpEntity otpEntity);

    public OtpEntity findOtpByKey(String key);

    public void deleteOtpByKey(String key);

    public void updateOtp(Map<String,Object> updateMap);
}
