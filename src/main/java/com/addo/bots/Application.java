package com.addo.bots;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class Application {
  public static final int CREATOR_ID = 0;

  public static void main(String[] args) throws TelegramApiRequestException {
    ApiContextInitializer.init();
    TelegramBotsApi api = new TelegramBotsApi();
    api.registerBot(new DahraBot());
  }
}
