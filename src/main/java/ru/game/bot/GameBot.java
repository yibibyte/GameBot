package ru.game.bot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GameBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(GameBot.class);
    private final static String USERNAME_BOT = "GameXPlayer_bot";
    private static final String TOKEN_FILE = "telegram_token.txt";

    GameBot(String TOKEN_BOT) {
        super(TOKEN_BOT);
    }

    private static String readTokenFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(TOKEN_FILE))) {
            return reader.readLine().trim();
        }
    }

    /*
            //Предварительнор нужно будет сделать userbot.txt для вставки своего бота конфиг

            private static String readUserNameFromFileStream() throws IOException {
            Path path = Paths.get(USERNAME_BOT);
            return Files.lines(path)
                    .findFirst()
                    .orElseThrow(() -> new IOException("Файл токена пуст или не найден"))
                    .trim();
        }*/
    private static String readTokenFromFileStream() throws IOException {
        Path path = Paths.get(TOKEN_FILE);
        return Files.lines(path)
                .findFirst()
                .orElseThrow(() -> new IOException("Файл токена пуст или не найден"))
                .trim();
    }

    public static String readPropertyValue(String filename, String key) {
        String value = "";
        try {
            Parameters params = new Parameters();
            File propertiesFile = new File(filename);
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.fileBased().setFile(propertiesFile));
            FileBasedConfiguration config = builder.getConfiguration();
            value = config.getString(key);
            System.out.println(key + ": " + value);

        } catch (ConfigurationException e) {
            logger.info("Нельзя прочитать значение, файл не корректен, ошибка " + e.getMessage());
        }
        return value;
    }

    public static void main(String[] args) throws IOException {

        String tokenFile = readTokenFromFileStream();
        GameBot tokenBot = new GameBot(tokenFile);

        //String TOKEN_VALUE = readPropertyValue("config.properties", USERNAME_BOT);
        //GameBot tokenBot = new GameBot(TOKEN_VALUE);

        //GameBot tokenBot = new GameBot(TOKEN_BOT);
        TelegramBotsApi botsApi;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(tokenBot);
            logger.info("Bot успешно запущен");
        } catch (TelegramApiException e) {
            logger.error("Ошибка выполнения запуска приложения" + e.getMessage() + e.getCause());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if ("/menu".equals(message.getText())) {
                sendMenu(message.getChatId());
            } else if ("Меню".equals(message.getText())) {
                sendMainMenu(message.getChatId());
            } else if ("dota2".equals(message.getText())) {
                sendGameLink(message.getChatId(), "https://www.dota2.com/");
            } else if ("minecraft".equals(message.getText())) {
                sendGameLink(message.getChatId(), "https://www.minecraft.net/en-us/");
            } else if ("Я в это не играю!".equals(message.getText())) {
                sendMessage(message.getChatId(), "Может, погуглим?", "https://ya.ru/");
            } else if ("Не играю".equals(message.getText())) {
                sendMessage(message.getChatId(), "Возможно, тебе стоит попробовать что-то из мира разработки.", "https://stackoverflow.com/");
            } else {
                sendResponse(message.getChatId(), "Вы ввели не неверную команду");
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if ("dota2".equals(callbackQuery.getData())) {
                sendGameLink(callbackQuery.getMessage().getChatId(), "https://www.dota2.com/");
            } else if ("minecraft".equals(callbackQuery.getData())) {
                sendGameLink(callbackQuery.getMessage().getChatId(), "https://www.minecraft.net/en-us/");
            } else if ("Я в это не играю!".equals(callbackQuery.getData())) {
                sendMessage(callbackQuery.getMessage().getChatId(), "Может, погуглим?", "https://ya.ru/");
            } else if ("Не играю".equals(callbackQuery.getData())) {
                sendMessage(callbackQuery.getMessage().getChatId(), "Возможно, тебе стоит попробовать что-то из мира разработки.", "https://stackoverflow.com/");
            }
        }
    }

    private void sendMainMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выбери что-нибудь из меню:");
        message.setReplyMarkup(getMainMenuKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Некорректная ошибка Меню: " + e.getMessage() + e.getCause());
        }
    }

    private ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardButton menuButton = new KeyboardButton("Меню");
        KeyboardButton menuSlashButton = new KeyboardButton("/menu");
        KeyboardButton dotaButton = new KeyboardButton("dota2");
        KeyboardButton minecraftButton = new KeyboardButton("minecraft");
        KeyboardButton dontKnowButton = new KeyboardButton("Я в это не играю!");
        KeyboardButton notPlayingButton = new KeyboardButton("Не играю");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(dotaButton);
        row1.add(minecraftButton);
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(dontKnowButton);
        row2.add(notPlayingButton);
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(menuButton);
        row3.add(menuSlashButton);
        keyboard.add(row3);

        return replyKeyboardMarkup;
    }

    private void sendMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привет! Что ты больше любишь?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButtonDota = new InlineKeyboardButton();
        inlineKeyboardButtonDota.setText("Dota 2");
        inlineKeyboardButtonDota.setCallbackData("dota2");
        row1.add(inlineKeyboardButtonDota);

        InlineKeyboardButton inlineKeyboardButtonMinecraft = new InlineKeyboardButton();
        inlineKeyboardButtonMinecraft.setText("Minecraft");
        inlineKeyboardButtonMinecraft.setCallbackData("minecraft");
        row1.add(inlineKeyboardButtonMinecraft);
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButtonDontKnow = new InlineKeyboardButton();
        inlineKeyboardButtonDontKnow.setText("Я в это не играю!");
        inlineKeyboardButtonDontKnow.setCallbackData("Я в это не играю!");
        row2.add(inlineKeyboardButtonDontKnow);

        InlineKeyboardButton inlineKeyboardButtonDontPlay = new InlineKeyboardButton();
        inlineKeyboardButtonDontPlay.setText("Не играю");
        inlineKeyboardButtonDontPlay.setCallbackData("Не играю");
        row2.add(inlineKeyboardButtonDontPlay);
        keyboard.add(row2);

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Некорректная ошибка Меню InlineKeyboardButton : " + e.getMessage() + e.getCause());
        }
    }

    private void sendGameLink(Long chatId, String url) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вот ссылка на игру: " + url);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при работе с SendMessage при выполнении : " + e.getMessage() + e.getCause());
        }
    }

    private void sendMessage(Long chatId, String text, String url) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Перейти");
        inlineKeyboardButton.setUrl(url);
        row.add(inlineKeyboardButton);
        keyboard.add(row);

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправки сообщения при выполнении метода sendMessage: " + e.getMessage() + e.getCause());
        }
    }

    private void sendResponse(Long chatId, String responseText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Сообщение об ошибке отправки: {}", e.getMessage());
        }
    }

    private void startBot(long chatId, String user) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привет, " + user + "! Я Простой Телеграмм Бот по Играм.");

        try {
            execute(message);
            logger.info("startBot выполнен");
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return USERNAME_BOT;
    }
}
