package org.lma_it.model.telegram;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.lma_it.model.User;
import org.lma_it.services.TelegramBotService;
import org.lma_it.services.UserService;
import org.lma_it.util.config.TelegramBotConfig;
import org.lma_it.util.document.UserDataToWordDocument;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final Logger logger = (Logger) LoggerFactory.getLogger(TelegramBot.class);

    @Autowired
    private UserService service;

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private TelegramBotConfig config;

    private String chatId;

    private boolean isUserAcceptToProcessingHisPersonalData = false;

    private final User user = new User();

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserDataToWordDocument userDataToWordDocument = new UserDataToWordDocument(logger);


    private final HashMap<String, String> personalDataFromUser = new HashMap<>(Map.of(
            "name", "NO_NAME",
            "secondName", "NO_SECOND_NAME",
            "lastName", "NO_LAST_NAME",
            "birthDate", "NO_BIRTH_DATE",
            "gender", "NO_GENDER"
    ));

    @Override
    public String getBotUsername() {
        logger.info("Имя бота: {}", config.getBotName());
        return config.getBotName();
    }


    @Autowired
    public TelegramBot(@Value("${bot.token}") String botToken){
        super(botToken);
        logger.info("В конструкторе бота. Токен {}.", botToken);
    }


    @Override
    public void onUpdateReceived(Update update) {

        if(!isUserAcceptToProcessingHisPersonalData){
            if (update.hasMessage() && update.getMessage().getText() != null) {
                logger.info("Получено сообщение из update: {}", update.getMessage().getText());
                String message = update.getMessage().getText();
                chatId = update.getMessage().getChatId().toString();

                String[] parts = message.split(" ", 2);
                logger.info("Содержимое в parts: {}", Arrays.toString(parts));

                if(message.contains("utm")){
                    sendMessage(chatId, "Вы перешли по ссылке.");
                    SendPhoto photoForProcessingOfPersonalData = new SendPhoto();
                    ClassPathResource resource = new ClassPathResource("SNP_Technology.png");

                    logger.info("Значение в chatId: {}", chatId);
                    photoForProcessingOfPersonalData.setChatId(chatId);
                    photoForProcessingOfPersonalData.setReplyMarkup(telegramBotService.registration());
                    try{
                        java.io.File file = resource.getFile();
                        photoForProcessingOfPersonalData.setPhoto(new InputFile(file));
                        execute(photoForProcessingOfPersonalData);
                    }catch (TelegramApiException | IOException e){
                        logger.error("Ошибка при попытке отправить сообщение с inline кнопками. Тип исключения: {}. Причина: {}", e.getClass().getSimpleName(), e.getMessage());
                    }
                }

            }else if(update.hasCallbackQuery() && !update.hasMessage()){
                CallbackQuery callbackQuery = update.getCallbackQuery();
                if("processingOfPersonalData".equals(callbackQuery.getData())){
                    logger.info("Значение в callbackQuery: {}", update.getCallbackQuery().getData());
                    sendMessage(chatId, "Приступим к регистрации для получения оффера в SNP Technology.\nВведите пожалуйста свое имя:");
                    isUserAcceptToProcessingHisPersonalData = true;
                }
            }
        }else if((update.hasMessage() && update.getMessage().hasText()) && !update.getMessage().getText().contains("/")) { //Если есть сообщение и в сообщении текст, то сюда
            String message = update.getMessage().getText();

            if(personalDataFromUser.get("name").equals("NO_NAME")){
                if(message.length() < 2){
                    sendMessage(chatId, "Имя слишком короткое. Попробуйте снова.");
                }else{
                    personalDataFromUser.put("name", message);
                    sendMessage(chatId, "Имя зарегистрировано.\nВаше имя: " + message + "\nВведите фамилию.");
                }
            }else if(personalDataFromUser.get("secondName").equals("NO_SECOND_NAME")){
                if(message.length() < 2){
                    sendMessage(chatId, "Фамилия слишком короткая. Попробуйте снова.");
                }else{
                    personalDataFromUser.put("secondName", message);
                    sendMessage(chatId, "Фамилия зарегистрирована.\nВаша фамилия: " + message + "\nВведите отчество.*");
                    sendMessage(chatId, "\n*Не обязательное поле, можно ввести /skip для пропуска.");
                }
            }else if(personalDataFromUser.get("lastName").equals("NO_LAST_NAME")){
                if(!message.isEmpty()){
                    personalDataFromUser.put("lastName", message);
                    sendMessage(chatId, "Отчество зарегистрировано.\nВаше отчество: " + message + "\nВведите дату рождения в формате*: 'дд.мм.гггг.'.\n\n*Очень важно соблюдать формат.");
                }
            }else if(personalDataFromUser.get("birthDate").equals("NO_BIRTH_DATE")){
                try{
                    LocalDate birthDate = LocalDate.parse(message, FORMATTER);
                    personalDataFromUser.put("birthDate", birthDate.toString());
                    sendMessage(chatId, "Ваша дата рождения: " + birthDate);
                    if (personalDataFromUser.get("gender").equals("NO_GENDER")){
                        logger.info("Лог из блока регистрации пола.");
                        SendMessage gender = new SendMessage();
                        gender.setChatId(chatId);
                        gender.setText("Выберите Ваш пол");
                        gender.setReplyMarkup(telegramBotService.chooseGender());
                        try {
                            execute(gender);
                        }catch (TelegramApiException e){
                            logger.error("Ошибка при попытке отправить сообщение с кнопками выбора пола. Причина: {}", e.getMessage());
                        }
                    }
                }catch (DateTimeParseException e){
                    sendMessage(chatId, "Вы ввели не верный формат для даты рождения. Требуемый формат дд.мм.гггг, Ваш формат: " + message + "\nПопробуйте заново.\nВведите дату рождения в формате дд.мм.гггг.");
                }
            }

        }else if(update.hasCallbackQuery() && !update.hasMessage()){ // Если нет сообщения и нет текста, проверяем наличие callbackQuery
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if("male".equals(callbackQuery.getData())){
                sendMessage(chatId, "Ваш пол успешно сохранен.\nВаш пол: Мужчина.\nЗагрузите Ваше фото.");
                personalDataFromUser.put("gender", "Мужчина");
            }else if("female".equals(callbackQuery.getData())){
                sendMessage(chatId, "Ваш пол успешно сохранен.\nВаш пол: Женщина.\nЗагрузите Ваше фото.");
                personalDataFromUser.put("gender", "Женщина");
            }
        }else if(update.hasMessage() && update.getMessage().hasText()){
            switch (update.getMessage().getText()){
                case "/start" -> sendMessage(chatId, "Привет! Я бот помогающий получить оффер в SNP Technology.");
                case "/help" -> showHelp(chatId);
                case "/continue" -> continueRegister();
                case "/stop" -> stopBot();
                case "/skip" -> {
                    personalDataFromUser.put("lastName", "");
                    sendMessage(chatId, "Введите дату рождения в формате*: 'дд.мм.гггг.'.\n\n*Очень важно соблюдать формат.");
                }
                default -> sendMessage(chatId, "Вы ввели не верную команду.\n\nВведите: /stop для завершения работы с ботом\n\n/continue для продолжения регистрации.");
            }
        }else if(update.getMessage().hasPhoto() || update.getMessage().hasDocument()){
            if(update.getMessage().hasPhoto()){
                logger.info("В блоке обработки фото, после успешных предыдущих пунктов.");
                int count = update.getMessage().getPhoto().size();
                PhotoSize photo = update.getMessage().getPhoto().get(count - 1);
                GetFile getFile = new GetFile();
                getFile.setFileId(photo.getFileId());
                List<PhotoSize> photos = update.getMessage().getPhoto();
                PhotoSize largestPhoto = photos.get(photos.size() - 1);
                byte[] image = downloadPhoto(largestPhoto.getFileId(), this);
                isUserAcceptToProcessingHisPersonalData = false;
                logger.info("Лог из блока сохранения пользователя в БД (если файл фото).");
                user.setId(Long.valueOf(chatId));
                user.setName(personalDataFromUser.get("name"));
                user.setSecondName(personalDataFromUser.get("secondName"));
                user.setLastName(personalDataFromUser.get("lastName"));
                user.setBirthDate(LocalDate.parse(personalDataFromUser.get("birthDate")));
                user.setGender(personalDataFromUser.get("gender"));
                user.setProfileImage(image);
                service.createUser(user);
                SendDocument document1 = new SendDocument();
                document1.setChatId(chatId);
                java.io.File template = new java.io.File("src/main/resources/template.docx");
                logger.info("Размер загруженного файла: {}. Пустой ли template: {}", template.length(), template.exists());
                document1.setDocument(new InputFile(userDataToWordDocument.generateUserDocument(user, logger, template, image)));
                try{
                    execute(document1);
                }catch (TelegramApiException e){
                    logger.error("Ошибка при попытке отправить файл пользователю (из блока с фото). Причина: {}", e.getMessage());
                }
            }else if(update.getMessage().hasDocument()){
                logger.info("Мы в блоке документа, потому что фото передано как документ.");
                Document document = update.getMessage().getDocument();
                String fileName = document.getFileName().toLowerCase();
                GetFile getFile = new GetFile();
                getFile.setFileId(document.getFileId());
                if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")){
                    byte[] image = convertPngToJpg(downloadPhoto(getFile.getFileId(), this));
                    isUserAcceptToProcessingHisPersonalData = false;
                    user.setId(Long.valueOf(chatId));
                    user.setName(personalDataFromUser.get("name"));
                    user.setSecondName(personalDataFromUser.get("secondName"));
                    user.setLastName(personalDataFromUser.get("lastName"));
                    user.setBirthDate(LocalDate.parse(personalDataFromUser.get("birthDate")));
                    user.setGender(personalDataFromUser.get("gender"));
                    user.setProfileImage(image);
                    service.createUser(user);
                    SendDocument document1 = new SendDocument();
                    document1.setChatId(chatId);
                    java.io.File template = new java.io.File("/app/tmp/template.docx");
                    document1.setDocument(new InputFile(userDataToWordDocument.generateUserDocument(user, logger, template, image)));
                    try{
                        execute(document1);
                    }catch (TelegramApiException e){
                        logger.error("Ошибка при попытке отправить файл пользователю (из блока с документом). Причина: {}", e.getMessage());
                    }

                }
            }

        }

    }

    private void continueRegister(){
        isUserAcceptToProcessingHisPersonalData = true;
        if(personalDataFromUser.get("name").equals("NO_NAME")){
            sendMessage(chatId, "Введите имя");
        }else if(personalDataFromUser.get("secondName").equals("NO_SECOND_NAME")){
            sendMessage(chatId,"Введите фамилию");
        }else if (personalDataFromUser.get("lastName").equals("NO_LAST_NAME")){
            sendMessage(chatId, "Введите отчество.*\n\n*Не обязательное поле, можно ввести /skip для пропуска.");
        }else if(personalDataFromUser.get("birthDate").equals("NO_BIRTH_DATE")){
            sendMessage(chatId, "Введите дату рождения в формате*: 'дд.мм.гггг.'.\n\n*Очень важно соблюдать формат.");
        }else if(personalDataFromUser.get("gender").equals("NO_GENDER")){
            sendMessage(chatId, "Выберите свой пол.");
        }
    }


    private void stopBot(){
        isUserAcceptToProcessingHisPersonalData = false;
        sendMessage(chatId, "Вы остановили бота.");
    }

    private void showHelp(String chatId){
        try{
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Список команд:\n/start - Вывод приветственного сообщения.\n/help - Вывод списка команд.\n/stop - Остановить бота.\n");
            logger.info("Отправка ответа пользователю из showHelp. Сообщение: {}", message.getText());
            execute(message);
        }catch (TelegramApiException e){
            System.out.println(e.getMessage());
        }
    }


    private void sendMessage(String chatId, String textToSend){

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        logger.info("Отправка ответа пользователю из sendMessage. Сообщение: {}", textToSend);
        try {
            execute(message);
        }catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public byte[] downloadPhoto(String fileId, TelegramBot bot) {
        try {

            File file = bot.execute(new GetFile(fileId));
            String filePath = file.getFilePath();

            try(InputStream inputStream = bot.downloadFileAsStream(filePath)){
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                return byteArrayOutputStream.toByteArray();
            }

        } catch (TelegramApiException | IOException e) {
            logger.error("Ошибка в методе downloadPhoto. Тип исключения: {}. Причина: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    public byte[] convertPngToJpg(byte[] pngData) {
        try{
            InputStream inputStream = new ByteArrayInputStream(pngData);
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new IOException("Ошибка: Не удалось прочитать PNG-изображение!");
            }

            // Создаём новое изображение в формате JPEG
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(image, 0, 0, Color.WHITE, null);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(newImage, "jpg", baos);
            return baos.toByteArray();
        }catch (IOException e){
            logger.error("Ошибка при конвертации изображения из png в jpg. Причина: {}", e.getMessage());
            return null;
        }
    }

}
