package io.mosip.kernel.otpmanager.service.impl;

import io.mosip.kernel.otpmanager.entity.OtpEntity;

import java.util.Map;

public interface DataStore {

    public void saveOtp(OtpEntity otpEntity);

    public OtpEntity findOtpByKey(String key);

    public void deleteOtpByKey(String key);

    public void updateOtp(String querry, Map<String,Object> updateMap);
}
