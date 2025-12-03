package io.mosip.kernel.otpmanager.test.util;

import io.mosip.kernel.otpmanager.exception.OtpServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.otpmanager.exception.CryptoFailureException;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.util.OtpProvider;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OtpProviderTest {

	@Autowired
	private OtpProvider provider;
	
	@MockBean
	private OtpRepository otpRepository;

	@Test(expected = CryptoFailureException.class)
	public void getSigningExceptionTest() {
		provider.computeOtp("98989898999", 6, "nsjkbdcdj");
	}

    @Test
    public void testComputeOtpProducesNumericOfRequestedLength() {
        OtpProvider provider = new OtpProvider();
        String otp = provider.computeOtp("mySecretKey", 6, "HmacSHA256");
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d+"));
    }

    @Test(expected = OtpServiceException.class)
    public void testGetSigningThrowsForInvalidAlgorithm() {
        // Invalid algorithm should cause getSigning to throw OtpServiceException
        OtpProvider.getSigning("any", "InvalidAlgoName");
    }
}
