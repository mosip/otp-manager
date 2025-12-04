package io.mosip.kernel.otpmanager.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.mosip.kernel.otpmanager.exception.OtpIOException;
import io.mosip.kernel.otpmanager.util.PasscodeGenerator;

public class PasscodeGeneratorTest {

    @Test
    public void testPadOutputAddsLeadingZerosViaGenerate() throws Exception {
        // Create signer that returns 5 bytes: first 4 bytes = int 123, last byte (offset) = 0
        PasscodeGenerator.Signer signer = data -> new byte[] { 0, 0, 0, 123, 0 };
        PasscodeGenerator pc = new PasscodeGenerator(signer, 6);
        String out = pc.generateResponseCode(new byte[] { 0 });
        // generateResponseCode should produce pinValue 123 and pad to 6 digits
        assertEquals("000123", out);
    }

    @Test(expected = OtpIOException.class)
    public void testHashToIntIOExceptionIsWrappedAsOtpIOException() throws Exception {
        // Signer returns a single byte; offset will be 1 -> ByteArrayInputStream length 0 -> readInt throws EOFException
        PasscodeGenerator.Signer signer = data -> new byte[] { 1 };
        PasscodeGenerator pc = new PasscodeGenerator(signer, 6);
        // Call generateResponseCode with any input; signer.sign will return single-byte array and cause hashToInt to throw
        pc.generateResponseCode(new byte[] { 0 });
    }
}
