package com.github.drsqrt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

/**
 * Grep is a search tool <br>
 * Provided the search keyword and the filePath,<br>
 * it finds all the occurrence of search keyword below provided filepath
 */
public class Grep {

  private static final Logger logger = LogManager.getLogger(Grep.class);
  private static final char TILDE = '~';
  private static CommandLineArguments commandLineArguments;
  private static boolean VERBOSE = false;

  public static void main(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options options = new Options();
      options.addOption(new Option("s", "search", true, "search in the files"));
      options.addOption(new Option("f", "file", true, "the file directory"));
      options.addOption(new Option("v", "verbose", false, "verbose output"));
      CommandLine cli = parser.parse(options, args);
      String searchKeyword = cli.getOptionValue('s');
      String filePath = cli.getOptionValue('f');
      commandLineArguments = new CommandLineArguments(searchKeyword, filePath, cli.hasOption('v'));
      VERBOSE = commandLineArguments.isVerbose();
      if (VERBOSE) logger.info("Initialising GREP");

      Grep grep = new Grep();
      grep.validateCommandLineArguments();
      grep.execute();
    } catch (ParseException parseException) {
      logger.error(parseException);
    }
  }

  private void execute() {
    if (VERBOSE) logger.info("Search Keyword : {}", commandLineArguments.getSearchKeyword());
    if (VERBOSE) logger.info("File Path : {} \n\n", commandLineArguments.getFilePath());
    Path filePath = Paths.get(commandLineArguments.getFilePath());
    try {
      Files.walkFileTree(filePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
          if (VERBOSE) logger.info("Visiting File : {}", path.toAbsolutePath());
          File file = path.toFile();
          try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            int lineNumber = 1;
            while (line != null) {
              if (line.contains(commandLineArguments.getSearchKeyword())) {
                logger.info("{}:{}: {}", file.getAbsolutePath(), lineNumber, line);
              }
              line = reader.readLine();
              lineNumber++;
            }
          }
          return super.visitFile(path, attrs);
        }
      });
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private void validateCommandLineArguments() {
    String searchKeyword = commandLineArguments.getSearchKeyword();
    if (searchKeyword == null || searchKeyword.isEmpty()) {
      throw new RuntimeException("Please provide search keyword with -s flag");
    }
    String filePath = commandLineArguments.getFilePath();
    if (filePath == null || filePath.isEmpty()) {
      throw new RuntimeException("Please provide file path with -f flag");
    }
    if (filePath.toCharArray()[0] == TILDE) {
      filePath = filePath.replace(TILDE + "", System.getProperty("user.home"));
      commandLineArguments.setFilePath(filePath);
    }
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new RuntimeException("File Path does not exists : " + filePath);
    }
  }

  public static class CommandLineArguments {

    private final String searchKeyword;
    private String filePath;
    private final Boolean verbose;

    protected CommandLineArguments(String searchKeyword, String filePath, boolean verbose) {
      this.searchKeyword = searchKeyword;
      this.filePath = filePath;
      this.verbose = verbose;
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
  }

}