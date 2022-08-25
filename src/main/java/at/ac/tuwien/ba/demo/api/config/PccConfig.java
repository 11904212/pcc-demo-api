package at.ac.tuwien.ba.demo.api.config;

import io.github11904212.pcc.PlanetaryComputerClient;
import io.github11904212.pcc.impl.PCClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PccConfig {

    @Bean
    public PlanetaryComputerClient getPCClient(){
        return new PCClientImpl();
    }
}
