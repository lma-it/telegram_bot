package org.lma_it.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Service
public class TelegramBotService {

    public InlineKeyboardMarkup registration(){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineInlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineButtons = new ArrayList<>();
        InlineKeyboardButton leaveBot = new InlineKeyboardButton();
        leaveBot.setText("Покинуть бота.");
        leaveBot.setUrl("https://snptech.ru");
        InlineKeyboardButton acceptProcessingOfPersonalData = new InlineKeyboardButton();
        acceptProcessingOfPersonalData.setText("Принять условия");
        acceptProcessingOfPersonalData.setCallbackData("processingOfPersonalData");
        inlineButtons.add(leaveBot);
        inlineButtons.add(acceptProcessingOfPersonalData);
        inlineInlineButtons.add(inlineButtons);
        markup.setKeyboard(inlineInlineButtons);
        return markup;
    }


    public InlineKeyboardMarkup chooseGender(){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineGenderButtons = new ArrayList<>();
        List<InlineKeyboardButton> genderButtons = new ArrayList<>();
        InlineKeyboardButton maleButton = new InlineKeyboardButton();
        maleButton.setText("Мужчина");
        maleButton.setCallbackData("male");
        InlineKeyboardButton femaleButton = new InlineKeyboardButton();
        femaleButton.setText("Женщина");
        femaleButton.setCallbackData("female");
        genderButtons.add(maleButton);
        genderButtons.add(femaleButton);
        inlineGenderButtons.add(genderButtons);
        markup.setKeyboard(inlineGenderButtons);
        return markup;

    }

}
