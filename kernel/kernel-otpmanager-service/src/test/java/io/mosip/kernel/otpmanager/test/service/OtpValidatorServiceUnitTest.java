package io.mosip.kernel.otpmanager.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.otpmanager.dto.OtpValidatorResponseDto;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.exception.RequiredKeyNotFoundException;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.service.impl.OtpValidatorServiceImpl;
import io.mosip.kernel.otpmanager.test.OtpmanagerTestBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OtpmanagerTestBootApplication.class)
public class OtpValidatorServiceUnitTest {

    @Autowired
    private OtpValidatorServiceImpl validatorService;

    @MockBean
    private OtpRepository repository;

    // Helper: precompute hashes used elsewhere in tests (same values as other tests)
    private final String refIdHash = "15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F";
    private final String idHash = "6DB5C886D3E9375E2C7BFBCE326A708734836151585059CD38F3CF586A125732";

    @Test(expected = RequiredKeyNotFoundException.class)
    public void testRequireKeyNotFoundThrows() {
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(anyString())).thenReturn(Optional.empty());
        // should throw
        validatorService.validateOtp("testKey", "1234");
    }

    @Test
    public void testExpiryCase() {
        OtpEntity entity = new OtpEntity();
        entity.setRefId(refIdHash);
        entity.setId(idHash);
        entity.setValidationRetryCount(0);
        entity.setStatusCode("OTP_UNUSED");
        // set generatedDtimes far in past so it's expired
        entity.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minus(1000, ChronoUnit.SECONDS));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var responseEntity = validatorService.validateOtp("testKey", "1234");
        OtpValidatorResponseDto body = responseEntity.getBody();
        assertEquals("failure", body.getStatus());
        assertEquals("OTP_EXPIRED", body.getMessage());
    }

    @Test
    public void testIncreaseValidationAttemptCount() {
        OtpEntity entity = new OtpEntity();
        entity.setRefId(refIdHash);
        entity.setId("someOtherId");
        entity.setValidationRetryCount(0);
        entity.setStatusCode("OTP_UNUSED");
        entity.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var response = validatorService.validateOtp("testKey", "0000");
        // should increment attempt via updateData -> repository.createQueryUpdateOrDelete called at least once
        verify(repository, times(1)).createQueryUpdateOrDelete(anyString(), any());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testFreezeWhenMaxAttemptReached() {
        OtpEntity entity = new OtpEntity();
        entity.setRefId(refIdHash);
        entity.setId("someOtherId");
        // assume threshold is 3 as in test properties, set attempt to 2 (threshold-1)
        entity.setValidationRetryCount(2);
        entity.setStatusCode("OTP_UNUSED");
        entity.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var responseEntity = validatorService.validateOtp("testKey", "0000");
        // freeze path should call createQueryUpdateOrDelete twice: increment and then freeze
        verify(repository, times(2)).createQueryUpdateOrDelete(anyString(), any());
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testUnFreezeKey_timePassed_keyMatches() {
        // Build entity that is KEY_FREEZED and updatedDtimes older than keyFreezeDuration
        OtpEntity entity = new OtpEntity();
        // id should be keyOtpHash i.e., hash(key:otp) used by other tests; we'll use the same idHash
        entity.setId(idHash);
        entity.setRefId(refIdHash);
        entity.setValidationRetryCount(3);
        entity.setStatusCode("KEY_FREEZED");
        // make updatedDtimes far in past so timeDifference > keyFreezeDuration
        entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minus(1000, ChronoUnit.SECONDS));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var responseEntity = validatorService.validateOtp("testKey", "1234");
        // should delete by id because keyOtpHash equals entity.id
        verify(repository, times(1)).deleteById(anyString());
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals("success", responseEntity.getBody().getStatus());
    }

    @Test
    public void testUnFreezeKey_timeNotPassed() {
        OtpEntity entity = new OtpEntity();
        entity.setId("someOtherId");
        entity.setRefId(refIdHash);
        entity.setValidationRetryCount(3);
        entity.setStatusCode("KEY_FREEZED");
        // recent updated time -> still frozen
        entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minus(10, ChronoUnit.SECONDS));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var responseEntity = validatorService.validateOtp("testKey", "1234");
        assertEquals(200, responseEntity.getStatusCodeValue());
        // Should be failure and freezed message
        assertEquals("failure", responseEntity.getBody().getStatus());
    }

    @Test
    public void testSuccessfulValidationDeletes() {
        OtpEntity entity = new OtpEntity();
        // id equals keyOtpHash for key=testKey otp=1234 (matches idHash used elsewhere)
        entity.setId(idHash);
        entity.setRefId(refIdHash);
        entity.setValidationRetryCount(0);
        entity.setStatusCode("OTP_UNUSED");
        entity.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var responseEntity = validatorService.validateOtp("testKey", "1234");
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals("success", responseEntity.getBody().getStatus());
        verify(repository, times(1)).deleteById(anyString());
    }

    @Test
    public void testProxyForLocalProfile_successAndFailure() {
        // Force activeProfile to 'local' so validateOtp returns via proxyForLocalProfile
        ReflectionTestUtils.setField(validatorService, "activeProfile", "local");
        ReflectionTestUtils.setField(validatorService, "localOtp", "111111");

        // success case: otp matches localOtp
        var success = validatorService.validateOtp("anyKey", "111111");
        assertEquals(200, success.getStatusCodeValue());
        assertEquals("success", success.getBody().getStatus());

        // failure case: otp does not match
        var fail = validatorService.validateOtp("anyKey", "000000");
        assertEquals(200, fail.getStatusCodeValue());
        assertEquals("failure", fail.getBody().getStatus());

        // reset activeProfile to avoid affecting other tests (optional)
        ReflectionTestUtils.setField(validatorService, "activeProfile", "test");
    }

    @Test
    public void testCreateUpdateMapPrivateMethodCoverage() {
        // call with all nulls -> empty map
        Object map1 = ReflectionTestUtils.invokeMethod(validatorService, "createUpdateMap", null, null, null, null);
        assertEquals(0, ((java.util.Map<?, ?>) map1).size());

        // call with only key
        Object map2 = ReflectionTestUtils.invokeMethod(validatorService, "createUpdateMap", refIdHash, null, null, null);
        assertEquals(1, ((java.util.Map<?, ?>) map2).size());
        assertTrue(((java.util.Map<?, ?>) map2).containsKey("refId"));

        // call with all params
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        Object map3 = ReflectionTestUtils.invokeMethod(validatorService, "createUpdateMap", refIdHash, "STATUS", Integer.valueOf(2), now);
        java.util.Map<?, ?> m3 = (java.util.Map<?, ?>) map3;
        assertEquals(4, m3.size());
        assertTrue(m3.containsKey("refId"));
        assertTrue(m3.containsKey("newOtpStatus"));
        assertTrue(m3.containsKey("newNumOfAttempt"));
        assertTrue(m3.containsKey("newValidationTime"));
    }

    @Test
    public void testUnFreezeKey_timePassed_keyNotMatch_callsUpdateData() {
        OtpEntity entity = new OtpEntity();
        entity.setId("someOtherId"); // does not match keyOtpHash
        entity.setRefId(refIdHash);
        entity.setValidationRetryCount(3);
        entity.setStatusCode("KEY_FREEZED");
        // updated far in past
        entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minus(1000, ChronoUnit.SECONDS));
        when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(refIdHash)).thenReturn(Optional.of(entity));

        var response = validatorService.validateOtp("testKey", "1234");
        // unFreezeKey should call updateData -> repository.createQueryUpdateOrDelete
        verify(repository, times(1)).createQueryUpdateOrDelete(anyString(), any());
        assertEquals(200, response.getStatusCodeValue());
    }
}
