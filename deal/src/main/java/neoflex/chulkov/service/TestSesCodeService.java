package neoflex.chulkov.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"standalone", "test"})
public class TestSesCodeService implements SesCodeService {
    @Override
    public String generateSesCode() {
        return "123456";
    }
}
