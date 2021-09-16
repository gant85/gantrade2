package com.gant.trade;

import com.gant.trade.domain.Strategy;
import com.gant.trade.mongo.service.StrategyService;
import com.gant.trade.rest.model.BotStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Slf4j
@SpringBootApplication
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@EnableMongoRepositories
@EnableScheduling
@ComponentScan(basePackages = "com.gant.trade")
public class GanTradeApplication {

	private final Environment env;

	@Autowired
	StrategyService strategyService;

	public GanTradeApplication(Environment env) {
		this.env = env;
		logApplicationStartup(env);
	}

	public static void main(String[] args) {
		SpringApplication.run(GanTradeApplication.class, args);
	}

	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String serverPort = env.getProperty("server.port");
		String contextPath = env.getProperty("server.servlet.context-path");
		if (StringUtils.isBlank(contextPath)) {
			contextPath = "/";
		}
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}
		log.info("\n----------------------------------------------------------\n\t" +
						"Application '{}' is running! Access URLs:\n\t" +
						"Local: \t\t{}://localhost:{}{}\n\t" +
						"External: \t{}://{}:{}{}\n\t" +
						"Profile(s): \t{}\n----------------------------------------------------------",
				env.getProperty("spring.application.name"),
				protocol,
				serverPort,
				contextPath,
				protocol,
				hostAddress,
				serverPort,
				contextPath,
				env.getActiveProfiles());
	}
	/*
	@PostConstruct
	public void init() {
		List<Strategy> strategyList = strategyService.findStrategyActive();
		strategyList.forEach(strategy -> {
			BotStrategy botStrategy = new BotStrategy();
			botStrategy.setStrategyName(strategy.getName());
			//TODO Temporaneo
			botStrategy.setDebug(true);
			strategyService.stopBot(botStrategy);
			strategyService.startBot(botStrategy);
		});
	}

	 */


}
