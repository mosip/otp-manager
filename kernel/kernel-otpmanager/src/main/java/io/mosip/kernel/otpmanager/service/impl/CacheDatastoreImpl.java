package io.mosip.kernel.otpmanager.service.impl;

import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.otpmanager.constant.SqlQueryConstants;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;


@ConditionalOnProperty(name = "mosip.datastore.type", havingValue = "cache")
@Component
public class CacheDatastoreImpl implements DataStore{
    @Autowired
    CacheManager cacheManager;


    @Value("${mosip.datastore.cache.name}")
    String cache;

    @Override
    public void saveOtp(OtpEntity otpEntity) {

        cacheManager.getCache(cache).put(otpEntity.getId(),otpEntity);
    }

    @Override
    public OtpEntity findOtpByKey(String key) {
        return cacheManager.getCache(cache).get(key,OtpEntity.class);
    }

    @Override
    public void deleteOtpByKey(String key) {
       cacheManager.getCache(cache).evict(key);
    }

    @Override
    public void updateOtp(String query, Map<String,Object> updateMap) {

        OtpEntity otpEntity = cacheManager.getCache(cache).get(updateMap.get(SqlQueryConstants.ID.getProperty()).toString(), OtpEntity.class);
        for(String key: updateMap.keySet()){
            if (StringUtils.equals(key,SqlQueryConstants.ID.getProperty())) {
                otpEntity.setId(key);
                continue;
            }
            if (StringUtils.equals(key,SqlQueryConstants.NEW_OTP_STATUS.getProperty())) {
                otpEntity.setStatusCode(updateMap.get(SqlQueryConstants.NEW_OTP_STATUS.getProperty()).toString());
                continue;
            }
            if (StringUtils.equals(key,SqlQueryConstants.NEW_NUM_OF_ATTEMPT.getProperty())) {
                otpEntity.setValidationRetryCount(Integer.parseInt(updateMap.get(SqlQueryConstants.NEW_NUM_OF_ATTEMPT.getProperty()).toString()));
                continue;
            }
            if (StringUtils.equals(key,SqlQueryConstants.NEW_VALIDATION_TIME.getProperty())) {
                otpEntity.setUpdatedDtimes((LocalDateTime) updateMap.get(SqlQueryConstants.NEW_VALIDATION_TIME.getProperty()));
            }
        }
        cacheManager.getCache(cache).put(otpEntity.getId(),otpEntity);
    }
}
