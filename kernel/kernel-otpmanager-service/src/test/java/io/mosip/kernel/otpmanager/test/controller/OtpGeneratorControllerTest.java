package io.mosip.kernel.otpmanager.test.controller;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.otpmanager.controller.OtpGeneratorController;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorRequestDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorResponseDto;
import io.mosip.kernel.otpmanager.service.impl.OtpGeneratorServiceImpl;

@RunWith(SpringRunner.class)
public class OtpGeneratorControllerTest {

	@Mock
	private OtpGeneratorServiceImpl service;

	@InjectMocks
	private OtpGeneratorController otpGeneratorController;

	@Test
	public void testOtpGenerationController() throws Exception {
		OtpGeneratorResponseDto dto = new OtpGeneratorResponseDto();
		dto.setOtp("3124");
		Mockito.when(service.getOtp(Mockito.any())).thenReturn(dto);
		OtpGeneratorRequestDto requestDto = new OtpGeneratorRequestDto();
		RequestWrapper<OtpGeneratorRequestDto> requestWrapper = new RequestWrapper<OtpGeneratorRequestDto>();
		requestWrapper.setRequest(requestDto);
		OtpGeneratorResponseDto responseDto = new OtpGeneratorResponseDto();
		responseDto.setOtp("3124");
		ResponseWrapper<OtpGeneratorResponseDto> actualResponseWrapper = otpGeneratorController.generateOtp(requestWrapper);
		Assert.assertEquals(responseDto, actualResponseWrapper.getResponse());	}

}
