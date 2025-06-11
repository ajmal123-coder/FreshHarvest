package com.Fresh_harvest.Backend;

import com.Fresh_harvest.Backend.model.ERole;
import com.Fresh_harvest.Backend.model.Role;
import com.Fresh_harvest.Backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	@Bean
	public CommandLineRunner run(RoleRepository roleRepository) {
		return args -> {
			if (!roleRepository.findByName(ERole.ROLE_ADMIN).isPresent()) {
				roleRepository.save(new Role(ERole.ROLE_ADMIN));
			}
			if (!roleRepository.findByName(ERole.ROLE_SELLER).isPresent()) {
				roleRepository.save(new Role(ERole.ROLE_SELLER));
			}
			if (!roleRepository.findByName(ERole.ROLE_CUSTOMER).isPresent()) {
				roleRepository.save(new Role(ERole.ROLE_CUSTOMER));
			}
		};
	}
}
