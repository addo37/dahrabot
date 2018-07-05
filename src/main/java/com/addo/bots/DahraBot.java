package com.addo.bots;

import com.addo.bots.objects.Dahra;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.EndUser;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.Map;
import java.util.StringJoiner;

import static com.addo.bots.Application.CREATOR_ID;
import static com.addo.bots.util.Emoji.*;
import static java.lang.String.format;
import static org.telegram.abilitybots.api.objects.Ability.builder;
import static org.telegram.abilitybots.api.objects.Locality.GROUP;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class DahraBot extends AbilityBot {

  public static final String DAHRA_BOT_TOKEN = "<BOT TOKEN>";
  public static final String DAHRA_BOT_USERNAME = "<BOT NICKNAME>";

  public static final String DB_DAHRA = "DB_DAHRA";
  public static final String IN_DAHRA = "in";
  public static final String MEENJAY = "meenjay";
  public static final String MEENJAY_INFO = "Meen jay?";
  public static final String DHARNA = "dharna";
  public static final String DHARNA_INFO = "ends the current dahra";
  public static final String OUT_DAHRA = "out";
  public static final String OUT_DAHRA_INFO = "Opts you out the current dahra";
  public static final String IN_DAHRA_INFO = "Opts you in the current dahra";
  public static final String DAHRA = "dahra";
  public static final String DAHRA_INFO = "Creates a new dahra";
  public static final String OPT_IN_DAHRA = "%s, you've opted in! " + WHITE_HEAVY_CHECK_MARK;
  public static final String DAHRA_NO_TITLE = "Please add a useful description to your dahra! " + HAPPY_PERSON_RAISING_ONE_HAND;
  public static final String DAHRA_IN_PROGRESS = "A dahra is already in progress. please /dharna before starting a new dahra.";
  public static final String DAHRA_CREATED = "A new dahra *%s* has been created.";
  public static final String NO_DAHRA = "There is no dahra atm. " + SAD_FACE;
  public static final String OPT_OUT_DAHRA = "%s, you've opted out! " + CROSS_MARK;
  public static final String ALREADY_OPTED_OUT_DAHRA = "%s, you've already opted out. " + SAD_FACE;
  public static final String DAHRA_ENDED = "Dahra *%s* has ended.";
  public static final String ALREADY_OPTED_IN_DAHRA = "%s, you've already opted in. ^^";

  public DahraBot(DBContext db) {
    super(DAHRA_BOT_TOKEN, DAHRA_BOT_USERNAME, db);
  }

  public DahraBot() {
    super(DAHRA_BOT_TOKEN, DAHRA_BOT_USERNAME);
  }

  @Override
  public int creatorId() {
    return CREATOR_ID;
  }

  public Ability createDahra() {
    return builder()
        .name(DAHRA)
        .info(DAHRA_INFO)
        .privacy(ADMIN)
        .locality(GROUP)
        .input(0)
        .action(ctx -> {
          if (ctx.arguments().length == 0)
            silent.send(DAHRA_NO_TITLE, ctx.chatId());
          else {
            if (db.<Long, Dahra>getMap(DB_DAHRA).get(ctx.chatId()) != null)
              silent.send(DAHRA_IN_PROGRESS, ctx.chatId());
            else {
              Dahra dahra = Dahra.newDahra(Joiner.on(" ").join(ctx.arguments()));
              db.<Long, Dahra>getMap(DB_DAHRA).put(ctx.chatId(), dahra);
              silent.sendMd(format(DAHRA_CREATED, dahra.getName()), ctx.chatId());
            }
          }
        })
        .build();
  }

  public Ability optInDahra() {
    return builder()
        .name(IN_DAHRA)
        .info(IN_DAHRA_INFO)
        .privacy(PUBLIC)
        .locality(GROUP)
        .input(0)
        .action(ctx -> {
          Dahra dahra = db.<Long, Dahra>getMap(DB_DAHRA).get(ctx.chatId());
          if (dahra == null) {
            silent.send(NO_DAHRA, ctx.chatId());
          } else {
            if (dahra.in(ctx.user().id())) {
              db.<Long, Dahra>getMap(DB_DAHRA).put(ctx.chatId(), dahra);
              silent.send(format(OPT_IN_DAHRA, ctx.user().shortName()), ctx.chatId());
            } else
              silent.send(format(ALREADY_OPTED_IN_DAHRA, ctx.user().shortName()), ctx.chatId());
          }
        })
        .build();
  }

  public Ability optOutDahra() {
    return builder()
        .name(OUT_DAHRA)
        .info(OUT_DAHRA_INFO)
        .privacy(PUBLIC)
        .locality(GROUP)
        .input(0)
        .action(ctx -> {
          Dahra dahra = db.<Long, Dahra>getMap(DB_DAHRA).get(ctx.chatId());
          if (dahra == null) {
            silent.send(NO_DAHRA, ctx.chatId());
          } else {
            if (dahra.out(ctx.user().id())) {
              db.<Long, Dahra>getMap(DB_DAHRA).put(ctx.chatId(), dahra);
              silent.send(format(OPT_OUT_DAHRA, ctx.user().shortName()), ctx.chatId());
            } else
              silent.send(format(ALREADY_OPTED_OUT_DAHRA, ctx.user().shortName()), ctx.chatId());
          }
        })
        .build();
  }

  public Ability endDahra() {
    return builder()
        .name(DHARNA)
        .info(DHARNA_INFO)
        .privacy(PUBLIC)
        .locality(GROUP)
        .input(0)
        .action(ctx -> {
          Map<Long, Dahra> dahrat = db.getMap(DB_DAHRA);
          if (dahrat.containsKey(ctx.chatId())) {
            Dahra removedDahra = dahrat.remove(ctx.chatId());
            silent.sendMd(format(DAHRA_ENDED, removedDahra.getName()), ctx.chatId());
          } else {
            silent.send(NO_DAHRA, ctx.chatId());
          }
        })
        .build();
  }

  public Ability viewDahra() {
    return builder()
        .name(MEENJAY)
        .info(MEENJAY_INFO)
        .privacy(PUBLIC)
        .locality(GROUP)
        .input(0)
        .action(ctx -> {
          Dahra dahra = db.<Long, Dahra>getMap(DB_DAHRA).get(ctx.chatId());
          if (dahra == null) {
            silent.send(NO_DAHRA, ctx.chatId());
          } else {
            String ins = dahra.getIn().stream()
                .reduce(new StringJoiner("\n"),
                    (in, attendee) -> in.add(format("%s %s", WHITE_HEAVY_CHECK_MARK, getUser(attendee).fullName())),
                    StringJoiner::merge)
                .toString();

            String outs = dahra.getOut().stream()
                .reduce(new StringJoiner("\n"),
                    (in, attendee) -> in.add(format("%s %s", CROSS_MARK, getUser(attendee).fullName())),
                    StringJoiner::merge)
                .toString();

            silent.sendMd(format("*%s*%n%n%s%n%s", dahra.getName(), ins, outs), ctx.chatId());
          }
        })
        .build();
  }

  @VisibleForTesting
  void setSender(MessageSender sender) {
    this.sender = sender;
  }

  @VisibleForTesting
  public void setSilent(SilentSender silent) {
    this.silent = silent;
  }

  @VisibleForTesting
  Map<Integer, EndUser> getUsers() {
    return users();
  }

  @VisibleForTesting
  Map<String, Integer> getUserIds() {
    return userIds();
  }

}
