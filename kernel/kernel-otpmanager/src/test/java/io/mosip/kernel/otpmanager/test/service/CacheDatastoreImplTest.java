package io.mosip.kernel.otpmanager.test.service;


import io.mosip.kernel.otpmanager.constant.SqlQueryConstants;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.service.impl.CacheDatastoreImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
public class CacheDatastoreImplTest {

    @Mock
    CacheManager cacheManager;

    @InjectMocks
    CacheDatastoreImpl cacheDatastore;

    @Mock
    Cache cache;

    @Test
    public void testUpdateOtp() {
        ReflectionTestUtils.setField(cacheDatastore,"cache","123");
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setId("123");
        Mockito.when(cache.get("123", OtpEntity.class)).thenReturn(otpEntity);

        Map<String,Object> updateMap = new HashMap<>();
        updateMap.put(SqlQueryConstants.ID.getProperty(), "123");
        updateMap.put(SqlQueryConstants.NEW_OTP_STATUS.getProperty(), "ACTIVE");
        updateMap.put(SqlQueryConstants.NEW_NUM_OF_ATTEMPT.getProperty(), "3");
        updateMap.put(SqlQueryConstants.NEW_VALIDATION_TIME.getProperty(), LocalDateTime.now());

        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(cache.get("123",OtpEntity.class)).thenReturn(otpEntity);
        cacheDatastore.updateOtp("UPDATE OTP", updateMap);
    }
}
