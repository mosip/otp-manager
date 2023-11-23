package io.mosip.kernel.otpmanager.test.service;

import io.mosip.kernel.otpmanager.constant.SqlQueryConstants;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.service.impl.DatabasePersistenceServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
public class DatabasePersistenceServiceImplTest {

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private DatabasePersistenceServiceImpl databaseDatastore;

    @Test
    public void saveTest(){
        OtpEntity otpEntity=new OtpEntity();
        otpEntity.setId("key");
        otpEntity.setOtp("123456");
        Mockito.when(otpRepository.save(otpEntity)).thenReturn(otpEntity);
        databaseDatastore.saveOtp(otpEntity);
    }

    @Test
    public void findOtpByKeyTest(){
        OtpEntity otpEntity=new OtpEntity();
        otpEntity.setId("key");
        otpEntity.setOtp("123456");
        Mockito.when(otpRepository.findById(OtpEntity.class,"key")).thenReturn(otpEntity);
        databaseDatastore.findOtpByKey("key");
    }

    @Test
    public void deleteOtpByKeyTest(){
        databaseDatastore.deleteOtpByKey("key");
    }

    @Test
    public void updateOtpTest() {
        Map<String,Object> updateMap = new HashMap<>();
        updateMap.put(SqlQueryConstants.ID.getProperty(), "123");
        updateMap.put(SqlQueryConstants.NEW_NUM_OF_ATTEMPT.getProperty(), "3");
        updateMap.put(SqlQueryConstants.NEW_VALIDATION_TIME.getProperty(), LocalDateTime.now());
        databaseDatastore.updateOtp(updateMap);
        updateMap.put(SqlQueryConstants.NEW_OTP_STATUS.getProperty(), "ACTIVE");
        databaseDatastore.updateOtp(updateMap);
    }

}
