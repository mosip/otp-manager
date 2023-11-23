package io.mosip.kernel.otpmanager.test.service;


import io.mosip.kernel.otpmanager.constant.SqlQueryConstants;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.service.impl.CachePersistenceServiceImpl;
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
public class CachePersistenceServiceImplTest {

    @Mock
    CacheManager cacheManager;

    @InjectMocks
    CachePersistenceServiceImpl cacheDatastore;

    @Mock
    Cache cache;


    @Test
    public void saveTest(){
        ReflectionTestUtils.setField(cacheDatastore,"cache","otp");
        OtpEntity otpEntity=new OtpEntity();
        otpEntity.setId("key");
        otpEntity.setOtp("123456");
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Cache cache1=Mockito.mock(Cache.class);
        Mockito.doNothing().when(cache1).put(Mockito.anyString(), Mockito.any());
        cacheDatastore.saveOtp(otpEntity);

    }

    @Test
    public void findOtpByKeyTest(){
        ReflectionTestUtils.setField(cacheDatastore,"cache","otp");
        OtpEntity otpEntity=new OtpEntity();
        otpEntity.setId("key");
        otpEntity.setOtp("123456");
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);
        Mockito.when(cache.get("key", OtpEntity.class)).thenReturn(otpEntity);

        cacheDatastore.findOtpByKey("key");
    }

    @Test
    public void deleteOtpByKeyTest(){

        ReflectionTestUtils.setField(cacheDatastore,"cache","otp");
        OtpEntity otpEntity=new OtpEntity();
        otpEntity.setId("key");
        otpEntity.setOtp("123456");
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        Cache cache1=Mockito.mock(Cache.class);
        Mockito.doNothing().when(cache1).evict(Mockito.anyString());
        cacheDatastore.findOtpByKey("key");
    }

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
        cacheDatastore.updateOtp(updateMap);
    }
}
