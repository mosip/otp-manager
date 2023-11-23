package io.mosip.kernel.otpmanager.service.impl;

import io.mosip.kernel.otpmanager.constant.SqlQueryConstants;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConditionalOnProperty(name = "mosip.datastore.type", havingValue = "postgres")
@Component
public class DatabasePersistenceServiceImpl implements PersistenceService {

    private final String UPDATE_OTP_STATUS = "updateOtpStatus";

    private final String UPDATE_OTP_ATTEMPT = "updateOtpAttempt";

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
      otpRepository.deleteById(key);
    }

    @Override
    public void updateOtp(Map<String,Object> updateMap) {
        String updateString;
        if(updateMap.containsKey(SqlQueryConstants.NEW_OTP_STATUS.getProperty())){
            updateString = SqlQueryConstants.UPDATE.getProperty() + " " + OtpEntity.class.getSimpleName()
                    + " SET status_code = :newOtpStatus," + "upd_dtimes = :newValidationTime,"
                    + "validation_retry_count = :newNumOfAttempt WHERE id=:id";
        }else{
            updateString=SqlQueryConstants.UPDATE.getProperty() + " " + OtpEntity.class.getSimpleName()
                    + " SET validation_retry_count = :newNumOfAttempt,"
                    + "upd_dtimes = :newValidationTime WHERE id=:id";
        }
        otpRepository.createQueryUpdateOrDelete(updateString, updateMap);
    }
}
