package io.mosip.kernel.otpmanager.test.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.mosip.kernel.otpmanager.entity.OtpEntity;

public class PlainJpaOtpRepositoryTest {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    @BeforeAll
    public static void setup() {
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        props.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS OTP\\;SET SCHEMA OTP");
        props.put("jakarta.persistence.jdbc.user", "sa");
        props.put("jakarta.persistence.jdbc.password", "");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        emf = Persistence.createEntityManagerFactory("test-pu", props);
        em = emf.createEntityManager();
    }

    @AfterAll
    public static void tearDown() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    @Test
    public void testManualQueryFindLatestByRefId() {
        String refId = "ref-" + UUID.randomUUID().toString();

        OtpEntity e1 = new OtpEntity();
        e1.setId(UUID.randomUUID().toString());
        e1.setRefId(refId);
        e1.setOtp("111111");
        e1.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(30));

        OtpEntity e2 = new OtpEntity();
        e2.setId(UUID.randomUUID().toString());
        e2.setRefId(refId);
        e2.setOtp("222222");
        e2.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(10));

        OtpEntity e3 = new OtpEntity();
        e3.setId(UUID.randomUUID().toString());
        e3.setRefId(refId);
        e3.setOtp("333333");
        e3.setGeneratedDtimes(LocalDateTime.now(ZoneId.of("UTC")));

        em.getTransaction().begin();
        em.persist(e1);
        em.persist(e2);
        em.persist(e3);
        em.getTransaction().commit();

        List<OtpEntity> result = em.createQuery("SELECT o FROM OtpEntity o WHERE o.refId = :refId ORDER BY o.generatedDtimes DESC", OtpEntity.class)
                .setParameter("refId", refId)
                .setMaxResults(1)
                .getResultList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOtp()).isEqualTo("333333");
    }
}
