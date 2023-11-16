package io.mosip.kernel.otpmanager.service.impl;

import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConditionalOnProperty(name = "mosip.datastore.type", havingValue = "postgres")
@Component
public class DatabaseDatastoreImpl implements DataStore{

    @Autowired
    private OtpRepository otpRepository;

    @Override
    public void saveOtp(OtpEntity otpEntity) {
        otpRepository.save(otpEntity);
    }

    @Override
    public OtpEntity findOtpByKey(String key) {
        return otpRepository.findById(OtpEntity.class,key);
    }

    @Override
    public void deleteOtpByKey(String key) {

    }

    @Override
    public void updateOtp(String querry, Map<String,Object> updateMap) {

    }
}
