package com.addo.bots;

import com.addo.bots.objects.Dahra;
import com.addo.bots.util.Emoji;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.EndUser;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.api.objects.Update;

import java.io.IOException;

import static com.addo.bots.DahraBot.*;
import static com.addo.bots.objects.Dahra.newDahra;
import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.telegram.abilitybots.api.db.MapDBContext.offlineInstance;
import static org.telegram.abilitybots.api.objects.EndUser.endUser;
import static org.telegram.abilitybots.api.objects.MessageContext.newContext;

public class DahraBotTest {
  public static final long CHAT_ID = 10L;
  private static final EndUser USER_1 = endUser(1, "fname1", "lname1", "username1");
  private static final EndUser USER_2 = endUser(2, "fname2", "lname2", "username2");
  private static final Update UPDATE = mock(Update.class);
  private DahraBot bot;
  private DBContext db;
  private MessageSender sender;
  private SilentSender silent;

  @Before
  public void setUp() {
    db = offlineInstance("test");
    bot = new DahraBot(db);

    sender = mock(MessageSender.class);
    silent = mock(SilentSender.class);

    bot.setSender(sender);
    bot.setSilent(silent);

    bot.getUsers().put(USER_1.id(), USER_1);
    bot.getUserIds().put(USER_1.username(), USER_1.id());
  }

  @Test
  public void canCreateDahra() {
    String dahraName = "Munchease 7PM";
    MessageContext context = newContext(UPDATE, USER_1, CHAT_ID, dahraName);

    bot.createDahra().action().accept(context);

    verify(silent, times(1)).sendMd(format(DAHRA_CREATED, dahraName), CHAT_ID);
    assertTrue(db.getMap(DB_DAHRA).containsKey(context.chatId()));
    assertTrue(db.<Long, Dahra>getMap(DB_DAHRA).get(context.chatId()).getName().equals(dahraName));
  }

  @Test
  public void canOptInDahra() {
    MessageContext context = newContext(UPDATE, USER_1, CHAT_ID);
    db.getMap(DB_DAHRA).put(context.chatId(), newDahra("test Dahra"));

    bot.optInDahra().action().accept(context);

    verify(silent, times(1)).send(format(OPT_IN_DAHRA, context.user().shortName()), CHAT_ID);
    assertTrue("User was not properly opted in dahra!",
        db.<Long, Dahra>getMap(DB_DAHRA).get(context.chatId()).getIn().contains(context.user().id()));
  }

  @Test
  public void cantOptInDahraTwice() {
    MessageContext context = newContext(UPDATE, USER_1, CHAT_ID);
    db.getMap(DB_DAHRA).put(context.chatId(), newDahra("test Dahra"));

    bot.optInDahra().action().accept(context);
    bot.optInDahra().action().accept(context);

    verify(silent, times(1)).send(format(ALREADY_OPTED_IN_DAHRA, context.user().shortName()), CHAT_ID);
  }

  @Test
  public void canOptOutDahra() {
    MessageContext context = newContext(UPDATE, USER_1, CHAT_ID);
    db.getMap(DB_DAHRA).put(context.chatId(), newDahra("test Dahra"));

    bot.optInDahra().action().accept(context);
    bot.optOutDahra().action().accept(context);

    verify(silent, times(1)).send(format(OPT_OUT_DAHRA, context.user().shortName()), CHAT_ID);
    assertFalse("User was not properly opted out dahra!",
        db.<Long, Dahra>getMap(DB_DAHRA).get(context.chatId()).getIn().contains(context.user().id()));
    assertTrue("User was not properly opted out dahra!",
        db.<Long, Dahra>getMap(DB_DAHRA).get(context.chatId()).getOut().contains(context.user().id()));
  }

  @Test
  public void cantOptOutDahraTwice() {
    MessageContext context = newContext(UPDATE, USER_1, CHAT_ID);
    db.getMap(DB_DAHRA).put(context.chatId(), newDahra("test Dahra"));

    bot.optInDahra().action().accept(context);
    bot.optOutDahra().action().accept(context);
    bot.optOutDahra().action().accept(context);

    verify(silent, times(1)).send(format(ALREADY_OPTED_OUT_DAHRA, context.user().shortName()), CHAT_ID);
  }

  @Test
  public void canEndDahra() {
    MessageContext context = newContext(UPDATE, USER_1, CHAT_ID);
    db.getMap(DB_DAHRA).put(context.chatId(), newDahra("test Dahra"));

    bot.endDahra().action().accept(context);

    assertFalse(db.getMap(DB_DAHRA).containsKey(context.chatId()));
  }

  @Test
  public void canViewDahra() {
    addUsers(USER_1, USER_2);
    MessageContext context1 = newContext(UPDATE, USER_1, CHAT_ID);
    MessageContext context2 = newContext(UPDATE, USER_2, CHAT_ID);

    String dahraName = "test Dahra";
    db.getMap(DB_DAHRA).put(context1.chatId(), newDahra(dahraName));

    bot.optInDahra().action().accept(context1);
    bot.optOutDahra().action().accept(context2);
    bot.viewDahra().action().accept(context1);

    String userIN = Emoji.WHITE_HEAVY_CHECK_MARK + " " + USER_1.fullName();
    String userOUT = Emoji.CROSS_MARK + " " + USER_2.fullName();
    verify(silent, times(1)).sendMd(format("*%s*%n%n%s%n%s", dahraName, userIN, userOUT), context1.chatId());
  }

  private void addUsers(EndUser... users) {
    for (EndUser user : users) {
      bot.getUsers().put(user.id(), user);
      bot.getUserIds().put(user.username(), user.id());
    }
  }

  @After
  public void tearDown() throws IOException {
    db.clear();
    db.close();
  }
}