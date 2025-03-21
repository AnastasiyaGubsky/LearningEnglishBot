package com.gubsky.LearningEnglishBot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningEnglishBotApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("BOT_USERNAME", dotenv.get("BOT_USERNAME"));
		System.setProperty("BOT_TOKEN", dotenv.get("BOT_TOKEN"));
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASS", dotenv.get("DB_PASS"));

		SpringApplication.run(LearningEnglishBotApplication.class, args);
	}
}