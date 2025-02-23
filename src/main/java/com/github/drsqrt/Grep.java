package com.github.drsqrt;

import com.github.drsqrt.config.CommandLineArgs;
import com.github.drsqrt.util.Util;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
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
import java.util.logging.Logger;

/**
 * Grep is a search tool <br>
 * Provided the search keyword and the filePath,<br>
 * it finds all the occurrence of search keyword below provided filepath
 */
public class Grep {

  private static final Logger logger = Logger.getLogger(Grep.class.getName());
  private static final char TILDE = '~';
  private static CommandLineArgs cli;
  private static boolean VERBOSE = false;

  static {
    Util.configLogger();
  }

  public static void main(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options options = Util.createOptions();
      CommandLine commandLine = parser.parse(options, args);

      cli = Util.parseCommandLineArgs(commandLine);
      VERBOSE = cli.isVerbose();

      if (VERBOSE) logger.info("Initialising GREP");

      Grep grep = new Grep();
      grep.validateCommandLineArguments();
      grep.execute();
    } catch (ParseException parseException) {
      logger.severe(parseException.getMessage());
    }
  }

  private void validateCommandLineArguments() {
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

  private void execute() {
    if (VERBOSE) logger.info("Search Keyword : " + cli.getSearchKeyword());
    if (VERBOSE) logger.info("File Path :  " + cli.getFilePath() + "\n\n");
    Path filePath = Paths.get(cli.getFilePath());
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
        if (line.contains(cli.getSearchKeyword())) {
          logger.info(file.getAbsolutePath() + ":" + lineNumber + " : " + line);
        }
        lineNumber++;
      }
    } catch (IOException e) {
      logger.severe("Failed to read file: " + e.getMessage());
    }
  }

}