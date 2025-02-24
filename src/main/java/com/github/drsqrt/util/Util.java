package com.github.drsqrt.util;

import com.github.drsqrt.config.CommandLineArgs;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Util {

  private static final char TILDE = '~';

  public static Options createOptions() {
    Options options = new Options();
    options.addOption(new Option("s", "search", true, "search keyword"));
    options.addOption(new Option("f", "file", true, "file or directory path"));
    options.addOption(new Option("v", "verbose", false, "verbose output"));
    return options;
  }

  public static CommandLineArgs parseCommandLineArgs(CommandLine cli) {
    String searchKeyword = cli.getOptionValue('s');
    String filePath = cli.getOptionValue('f');
    boolean verbose = cli.hasOption('v');
    return CommandLineArgs.builder()
      .searchKeyword(searchKeyword)
      .filePath(filePath)
      .verbose(verbose)
      .build();
  }

  public static void validateCommandLineArguments(CommandLineArgs cli) {
    String searchKeyword = cli.getSearchKeyword();
    if (searchKeyword == null || searchKeyword.isEmpty()) {
      throw new IllegalArgumentException("Please provide search keyword with -s flag");
    }
    String filePath = cli.getFilePath();
    if (filePath == null || filePath.isEmpty()) {
      filePath = CommandLineArgs.DEFAULT_FILE_PATH;
      cli.setFilePath(filePath);
    }
    if (filePath.toCharArray()[0] == TILDE) {
      filePath = filePath.replace(TILDE + "", System.getProperty("user.home"));
      cli.setFilePath(filePath);
    }
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("File Path does not exists : " + filePath);
    }
  }

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
