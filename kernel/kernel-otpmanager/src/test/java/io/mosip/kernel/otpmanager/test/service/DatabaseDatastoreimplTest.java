package io.mosip.kernel.otpmanager.test.service;

import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.service.impl.DatabaseDatastoreImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DatabaseDatastoreimplTest {

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private DatabaseDatastoreImpl databaseDatastore;

    @Test
    public void updateOtpTest() {
        databaseDatastore.updateOtp("updateOtpStatus", null);
        databaseDatastore.updateOtp("updateOtpAttempt", null);
    }

}
