package org.lma_it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.lma_it")
public class TelegramBotForSnpTechnologyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotForSnpTechnologyApplication.class, args);
		System.out.println("Приложение успешно запущено.");
	}

}
