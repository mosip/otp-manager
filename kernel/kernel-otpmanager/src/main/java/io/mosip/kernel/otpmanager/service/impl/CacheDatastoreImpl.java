package io.mosip.kernel.otpmanager.service.impl;

import io.mosip.kernel.otpmanager.entity.OtpEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;


@ConditionalOnProperty(name = "mosip.datastore.type", havingValue = "cache")
@Component
public class CacheDatastoreImpl implements DataStore{
    @Override
    public void saveOtp(OtpEntity otpEntity) {

    }

    @Override
    public OtpEntity findOtpByKey(String key) {
        return null;
    }

    @Override
    public void deleteOtpByKey(String key) {

    }

    @Override
    public void updateOtp(String querry, Map<String,Object> updateMap) {

    }
}
