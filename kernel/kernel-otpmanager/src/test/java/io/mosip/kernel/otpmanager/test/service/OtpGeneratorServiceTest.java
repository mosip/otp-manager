package io.mosip.kernel.otpmanager.test.service;


import java.time.LocalDateTime;

import io.mosip.kernel.otpmanager.dto.OtpGeneratorResponseDto;
import io.mosip.kernel.otpmanager.service.PersistenceService;
import io.mosip.kernel.otpmanager.service.impl.OtpGeneratorServiceImpl;
import io.mosip.kernel.otpmanager.util.OtpProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorRequestDto;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
@RunWith(SpringRunner.class)
public class OtpGeneratorServiceTest {

	@Mock
    PersistenceService persistenceService;

	@Mock
	OtpProvider otpProvider;

	@InjectMocks
	OtpGeneratorServiceImpl otpGeneratorService;

	@Test
	public void testOtpGenerationFreezedCase() throws Exception {
		ReflectionTestUtils.setField(otpGeneratorService,"keyFreezeTime","500");
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		RequestWrapper<OtpGeneratorRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(otpGeneratorRequestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");

		OtpEntity otpEntity=new OtpEntity();
		otpEntity.setId("123");
		otpEntity.setStatusCode("KEY_FREEZED");
		otpEntity.setUpdatedDtimes(LocalDateTime.now());

//		Mockito.when(repository.findById(Mockito.any(),Mockito.anyString())).thenReturn(otpEntity);
		Mockito.when(persistenceService.findOtpByKey(Mockito.anyString())).thenReturn(otpEntity);

		otpGeneratorService.getOtp(otpGeneratorRequestDto);

	}

	@Test
	public void testOtpGeneratorServicePositiveCase() throws Exception {
		ReflectionTestUtils.setField(otpGeneratorService,"macAlgorithm","SHA257");
		ReflectionTestUtils.setField(otpGeneratorService,"otpLength",6);
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");

		Mockito.when(otpProvider.computeOtp(Mockito.anyString(),Mockito.anyInt(),Mockito.anyString())).thenReturn("123456");
		OtpGeneratorResponseDto otp = otpGeneratorService.getOtp(otpGeneratorRequestDto);
		Assert.assertEquals(otp.getOtp(),"123456");
		Assert.assertEquals(otp.getStatus(),"GENERATION_SUCCESSFUL");
	}
}
