package com.hap.shiro;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.hap.shiro.config.GlobalConstant;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;

@SpringBootApplication
public class ShiroApplication {

	@Inject
	private Environment env;
	private static Boolean activeProfile;

	private final static Logger log  = LoggerFactory.getLogger(ShiroApplication.class);

	@PostConstruct
	public void initilizingApp() throws IOException {
		String environment = GlobalConstant.PROFILE_PROD;
		if (env.getActiveProfiles().length == 0) {
			log.warn("No profile configured, running with default config");
		} else {
			for(String profile : env.getActiveProfiles()){
				if(GlobalConstant.PROFILE_PROD.equalsIgnoreCase(profile)){
					environment = profile;
				}
			}
			log.info("Running with Spring profile(s) : {}", Arrays.toString(env.getActiveProfiles()));
		}
		activeProfile = isProdProfileIsActive(environment);
	}

	public static void main(String[] args) {
//		SpringApplication.run(ShiroApplication.class, args);
		SpringApplication app = new SpringApplication(ShiroApplication.class);
		SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);
		addDefaultProfile(app, source);
		ApplicationContext ctx=app.run(args);
		setLogLevel(activeProfile);
	}

	private Boolean isProdProfileIsActive(String profile) {
		if(profile.equalsIgnoreCase(GlobalConstant.PROFILE_PROD)){
			return true;
		}
		return false;
	}

	private static void setLogLevel(Boolean activeProfile){
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger("ROOT").setLevel(Level.valueOf("ERROR"));
//		context.getLogger("custom.logger").setLevel(Level.valueOf("ERROR"));
	}

	private static void addDefaultProfile(SpringApplication app, SimpleCommandLinePropertySource source) {
		if (!source.containsProperty("spring.profiles.active")) {
			app.setAdditionalProfiles(GlobalConstant.PROFILE_DEV);
		}
	}

}
