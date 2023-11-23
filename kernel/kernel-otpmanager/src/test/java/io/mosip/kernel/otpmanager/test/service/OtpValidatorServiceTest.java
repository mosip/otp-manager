package io.mosip.kernel.otpmanager.test.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import io.mosip.kernel.otpmanager.service.PersistenceService;
import io.mosip.kernel.otpmanager.service.impl.OtpValidatorServiceImpl;
import io.mosip.kernel.otpmanager.util.OtpManagerUtils;
import io.mosip.kernel.otpmanager.util.OtpProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.otpmanager.entity.OtpEntity;

@RunWith(SpringRunner.class)
public class OtpValidatorServiceTest {


	@Mock
    PersistenceService persistenceService;

	@Mock
	OtpProvider provider;

	@Mock
	OtpManagerUtils otpManagerUtils;

	@InjectMocks
	OtpValidatorServiceImpl otpValidatorService;


	@Test
	public void testOtpValidatorServiceWhenMaxAttemptReached() throws Exception {
		ReflectionTestUtils.setField(otpValidatorService,"activeProfile","main");
		ReflectionTestUtils.setField(otpValidatorService,"otpExpiryLimit","2000");
		ReflectionTestUtils.setField(otpValidatorService,"numberOfValidationAttemptsAllowed","3");
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("12345");
		entity.setValidationRetryCount(3);
		entity.setStatusCode("OTP_UNUSED");
		entity.setValidationRetryCount(0);
		entity.setUpdatedDtimes(LocalDateTime.now());
		Mockito.when(persistenceService.findOtpByKey(Mockito.anyString())).thenReturn(entity);
		otpValidatorService.validateOtp("12345","123456");
	}

	@Test
	public void testOtpValidatorServiceWhenKeyFreezedPositiveCase() throws Exception {
		ReflectionTestUtils.setField(otpValidatorService,"activeProfile","main");
		ReflectionTestUtils.setField(otpValidatorService,"otpExpiryLimit","20000");
		ReflectionTestUtils.setField(otpValidatorService,"numberOfValidationAttemptsAllowed","1");
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("testKey");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("KEY_FREEZED");
		entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minus(1, ChronoUnit.MINUTES));
		Mockito.when(persistenceService.findOtpByKey(Mockito.anyString())).thenReturn(entity);
		otpValidatorService.validateOtp("12345","123456");
	}

	@Test
	public void testOtpValidatorServiceWhenKeyFreezedPostiveCase() throws Exception {
		ReflectionTestUtils.setField(otpValidatorService,"activeProfile","main");
		ReflectionTestUtils.setField(otpValidatorService,"otpExpiryLimit","20000");
		ReflectionTestUtils.setField(otpValidatorService,"numberOfValidationAttemptsAllowed","0");
		ReflectionTestUtils.setField(otpValidatorService,"keyFreezeDuration","0");
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("12345");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("KEY_FREEZED");
		entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		Mockito.when(persistenceService.findOtpByKey(Mockito.anyString())).thenReturn(entity);
		otpValidatorService.validateOtp("12345","1234");
		}

	@Test
	public void testOtpValidatorServiceWhenKeyUnUsedNegativeCase() throws Exception {
		ReflectionTestUtils.setField(otpValidatorService,"activeProfile","main");
		ReflectionTestUtils.setField(otpValidatorService,"otpExpiryLimit","20000");
		ReflectionTestUtils.setField(otpValidatorService,"numberOfValidationAttemptsAllowed","0");
		ReflectionTestUtils.setField(otpValidatorService,"keyFreezeDuration","0");
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("12345");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("OTP_UNUSED");
		entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		Mockito.when(persistenceService.findOtpByKey(Mockito.anyString())).thenReturn(entity);
		otpValidatorService.validateOtp("12345","1234");
	}

	@Test
	public void testOtpValidatorServiceWithLocalProfile() throws Exception {

		ReflectionTestUtils.setField(otpValidatorService,"otpExpiryLimit","20000");
		ReflectionTestUtils.setField(otpValidatorService,"numberOfValidationAttemptsAllowed","0");
		ReflectionTestUtils.setField(otpValidatorService,"activeProfile","local");
		ReflectionTestUtils.setField(otpValidatorService,"localOtp","111111");
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("12345");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("OTP_UNUSED");
		entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		Mockito.when(persistenceService.findOtpByKey(Mockito.anyString())).thenReturn(entity);
		otpValidatorService.validateOtp("12345","1234");
		entity.setOtp("111111");
		otpValidatorService.validateOtp("12345","111111");
	}
}
