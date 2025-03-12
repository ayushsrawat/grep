package com.github.drsqrt.config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Configuration {
  /**
   * Configure {@code java.util.logging.Logger}
   */
  public static void configLogger() {
    Logger rootLogger = Logger.getLogger("");
    Handler[] handlers = rootLogger.getHandlers();
    for (Handler h : handlers) {
      rootLogger.removeHandler(h);
    }

    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setFormatter(new SimpleFormatter() {
      @Override
      public String format(LogRecord record) {
        return String.format("%s%n", record.getMessage());
      }
    });
    rootLogger.addHandler(consoleHandler);
  }
}
