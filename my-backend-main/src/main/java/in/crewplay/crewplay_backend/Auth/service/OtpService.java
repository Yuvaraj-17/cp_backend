package in.crewplay.crewplay_backend.Auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final int OTP_TTL_MINUTES = 5;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndStoreOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        redisTemplate.opsForValue()
                .set("otp:" + email, otp, Duration.ofMinutes(OTP_TTL_MINUTES));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = "otp:" + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key); // one-time use
            return true;
        }
        return false;
    }
}
