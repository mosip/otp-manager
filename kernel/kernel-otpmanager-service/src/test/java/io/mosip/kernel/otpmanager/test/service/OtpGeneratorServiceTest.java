package io.mosip.kernel.otpmanager.test.service;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorRequestDto;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.test.OtpmanagerTestBootApplication;
import io.mosip.kernel.otpmanager.util.OtpProvider;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = OtpmanagerTestBootApplication.class)
public class OtpGeneratorServiceTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	OtpRepository repository;

	@MockBean
	OtpProvider otpProvider;

	@WithUserDetails("individual")
	@Test
	public void testOtpGeneratorServicePositiveCase() throws Exception {
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		RequestWrapper<OtpGeneratorRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(otpGeneratorRequestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);

		// No existing entity -> generation successful
		when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(anyString())).thenReturn(java.util.Optional.empty());
		when(otpProvider.computeOtp(anyString(), org.mockito.ArgumentMatchers.anyInt(), anyString())).thenReturn("111111");

		mockMvc.perform(post("/otp/generate").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn();
	}

	@WithUserDetails("individual")
	@Test
	public void testOtpGenerationFreezedCase() throws Exception {
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		RequestWrapper<OtpGeneratorRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(otpGeneratorRequestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("testKey");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("KEY_FREEZED");
		entity.setUpdatedDtimes(LocalDateTime.now());

		when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(anyString())).thenReturn(java.util.Optional.of(entity));

		mockMvc.perform(post("/otp/generate").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn();
	}

	@WithUserDetails("individual")
	@Test
	public void testOtpGenerationWithExistingEntityDeletesOld() throws Exception {
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		RequestWrapper<OtpGeneratorRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(otpGeneratorRequestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		OtpEntity entity = new OtpEntity();
		entity.setOtp("1234");
		entity.setId("testKey");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("UNUSED_OTP");
		// set updated time far in past so key-freeze condition won't hold
		entity.setUpdatedDtimes(LocalDateTime.now().minus(1000, ChronoUnit.SECONDS));

		when(repository.findFirstByRefIdOrderByGeneratedDtimesDesc(anyString())).thenReturn(java.util.Optional.of(entity));
		when(otpProvider.computeOtp(anyString(), org.mockito.ArgumentMatchers.anyInt(), anyString())).thenReturn("222222");

		mockMvc.perform(post("/otp/generate").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn();

		// verify that old entity was deleted
		verify(repository).delete(entity);
	}
}
