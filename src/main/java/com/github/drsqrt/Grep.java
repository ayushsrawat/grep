package com.github.drsqrt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Grep is a search tool <br>
 * Provided the search keyword and the filePath,<br>
 * it finds all the occurrence of search keyword below provided filepath
 */
public class Grep {

  private static final Logger logger = Logger.getLogger(Grep.class.getName());
  private static final char TILDE = '~';
  private static CommandLineArguments commandLineArguments;
  private static boolean VERBOSE = false;

  static {
    configLogger();
  }

  public static void main(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options options = createOptions();
      CommandLine cli = parser.parse(options, args);

      commandLineArguments = parseCommandLineArguments(cli);
      VERBOSE = commandLineArguments.isVerbose();

      if (VERBOSE) logger.info("Initialising GREP");

      Grep grep = new Grep();
      grep.validateCommandLineArguments();
      grep.execute();
    } catch (ParseException parseException) {
      logger.severe(parseException.getMessage());
    }
  }

  private static void configLogger() {
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

  private static Options createOptions() {
    Options options = new Options();
    options.addOption(new Option("s", "search", true, "search keyword"));
    options.addOption(new Option("f", "file", true, "file or directory path"));
    options.addOption(new Option("v", "verbose", false, "verbose output"));
    return options;
  }

  private static CommandLineArguments parseCommandLineArguments(CommandLine cli) {
    String searchKeyword = cli.getOptionValue('s');
    String filePath = cli.getOptionValue('f');
    boolean verbose = cli.hasOption('v');
    return CommandLineArguments.builder()
      .searchKeyword(searchKeyword)
      .filePath(filePath)
      .verbose(verbose)
      .build();
  }

  private void validateCommandLineArguments() {
    String searchKeyword = commandLineArguments.getSearchKeyword();
    if (searchKeyword == null || searchKeyword.isEmpty()) {
      throw new IllegalArgumentException("Please provide search keyword with -s flag");
    }
    String filePath = commandLineArguments.getFilePath();
    if (filePath == null || filePath.isEmpty()) {
      throw new IllegalArgumentException("Please provide file path with -f flag");
    }
    if (filePath.toCharArray()[0] == TILDE) {
      filePath = filePath.replace(TILDE + "", System.getProperty("user.home"));
      commandLineArguments.setFilePath(filePath);
    }
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("File Path does not exists : " + filePath);
    }
  }

  private void execute() {
    if (VERBOSE) logger.info("Search Keyword : " + commandLineArguments.getSearchKeyword());
    if (VERBOSE) logger.info("File Path :  " + commandLineArguments.getFilePath() + "\n\n");
    Path filePath = Paths.get(commandLineArguments.getFilePath());
    try {
      Files.walkFileTree(filePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
          if (VERBOSE) logger.info("Visiting File : " + path.toAbsolutePath());
          searchInFile(path);
          return super.visitFile(path, attrs);
        }
      });
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
  }

  private void searchInFile(Path path) {
    File file = path.toFile();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      int lineNumber = 1;
      while ((line = reader.readLine()) != null) {
        if (line.contains(commandLineArguments.getSearchKeyword())) {
          logger.info(file.getAbsolutePath() + ":" + lineNumber + " : " + line);
        }
        lineNumber++;
      }
    } catch (IOException e) {
      logger.severe("Failed to read file: " + e.getMessage());
    }
  }

  public static class CommandLineArguments {

    private final String searchKeyword;
    private String filePath;
    private final Boolean verbose;

    protected CommandLineArguments(Builder builder) {
      this.searchKeyword = builder.searchKeyword;
      this.filePath = builder.filePath;
      this.verbose = builder.verbose;
    }

    public static Builder builder() {
      return new Builder();
    }

    public String getSearchKeyword() {
      return this.searchKeyword;
    }

    public String getFilePath() {
      return this.filePath;
    }

    public Boolean isVerbose() {
      return this.verbose;
    }

    public void setFilePath(String filePath) {
      this.filePath = filePath;
    }

    public static final class Builder {

      private String searchKeyword;
      private String filePath = ".";
      private Boolean verbose = false;

      public CommandLineArguments build() {
        if (searchKeyword == null || searchKeyword.isEmpty()) {
          throw new IllegalArgumentException("Search Keyword is required.");
        }
        return new CommandLineArguments(this);
      }

      public Builder searchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        return this;
      }

      public Builder filePath(String filePath) {
        this.filePath = filePath;
        return this;
      }

      public Builder verbose(Boolean verbose) {
        this.verbose = verbose;
        return this;
      }
    }
  }

}