package org.lma_it.util.config;

import ch.qos.logback.classic.Logger;
import org.lma_it.model.telegram.TelegramBot;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class TelegramBotInitializer {

    private final Logger logger = (Logger) LoggerFactory.getLogger(TelegramBotInitializer.class);

    @Autowired
    private final TelegramBot bot;

    @Autowired
    public TelegramBotInitializer(TelegramBot bot){
        logger.info("Инициализация TelegramBotInitializer.");
        this.bot = bot;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init(){
        logger.info("В блоке инициализации бота.");
        try{
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            logger.info("Инициализация бота: {}", bot.toString());
            telegramBotsApi.registerBot(bot);
            logger.info("Инициализация прошла успешно.");
        }catch(TelegramApiException e){
            logger.error("Ошибка при инициализации бота: {}", e.getMessage());
        }

    }
}
