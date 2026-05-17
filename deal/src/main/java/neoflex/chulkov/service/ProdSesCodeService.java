package neoflex.chulkov.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@Profile("prod")
public class ProdSesCodeService implements SesCodeService{
    @Override
    public String generateSesCode() {
        SecureRandom rnd = new SecureRandom();
        log.info("сгенерирован ses code");
        return String.valueOf(100_000 + rnd.nextInt(900_000));
    }
}
