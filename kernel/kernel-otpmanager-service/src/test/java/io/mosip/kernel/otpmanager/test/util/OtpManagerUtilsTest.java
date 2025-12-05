package io.mosip.kernel.otpmanager.test.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.otpmanager.constant.OtpErrorConstants;
import io.mosip.kernel.otpmanager.exception.OtpInvalidArgumentException;
import io.mosip.kernel.otpmanager.util.OtpManagerUtils;

public class OtpManagerUtilsTest {

    @After
    public void cleanup() {
        // nothing to reset
    }

    @Test
    public void testValidateOtpRequestArguments_nullsProduceErrors() {
        OtpManagerUtils utils = new OtpManagerUtils();
        // set key length bounds to allow simple checks
        ReflectionTestUtils.setField(utils, "keyMinLength", "3");
        ReflectionTestUtils.setField(utils, "keyMaxLength", "10");

        try {
            utils.validateOtpRequestArguments(null, null);
            fail("Expected OtpInvalidArgumentException");
        } catch (OtpInvalidArgumentException e) {
            List<ServiceError> list = e.getList();
            assertTrue(list.size() >= 2);
        }
    }

    @Test
    public void testValidateOtpRequestArguments_illegalKeyLength() {
        OtpManagerUtils utils = new OtpManagerUtils();
        ReflectionTestUtils.setField(utils, "keyMinLength", "5");
        ReflectionTestUtils.setField(utils, "keyMaxLength", "5");
        String shortKey = "123"; // length 3, less than min 5
        try {
            utils.validateOtpRequestArguments(shortKey, "123456");
            fail("Expected OtpInvalidArgumentException");
        } catch (OtpInvalidArgumentException e) {
            assertTrue(e.getList().stream().anyMatch(er -> er.getErrorCode().equals(OtpErrorConstants.OTP_VAL_ILLEGAL_KEY_INPUT.getErrorCode())));
        }
    }

    @Test
    public void testValidateOtpRequestArguments_nonNumericOtp() {
        OtpManagerUtils utils = new OtpManagerUtils();
        ReflectionTestUtils.setField(utils, "keyMinLength", "1");
        ReflectionTestUtils.setField(utils, "keyMaxLength", "10");
        try {
            utils.validateOtpRequestArguments("validKey", "abcde");
            fail("Expected OtpInvalidArgumentException");
        } catch (OtpInvalidArgumentException e) {
            assertTrue(e.getList().stream().anyMatch(er -> er.getErrorCode().equals(OtpErrorConstants.OTP_VAL_ILLEGAL_OTP_INPUT.getErrorCode())));
        }
    }

    @Test
    public void testGetHash_returnsNonNull() {
        String hash = OtpManagerUtils.getHash("any");
        assertTrue(hash != null && !hash.isEmpty());
    }
}
