package com.github.drsqrt;

import com.github.drsqrt.cli.Parser;
import com.github.drsqrt.cli.Args;
import com.github.drsqrt.config.Configuration;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Grep is a search tool <br>
 * Provided the search keyword and the filePath,<br>
 * it finds all the occurrence of search keyword below provided filepath
 */
public class Grep {

  private static final Logger logger = Logger.getLogger(Grep.class.getName());
  private boolean verbose = false;
  private Args cli;

  static {
    Configuration.configLogger();
  }

  private Grep() {
  }

  public static void main(String[] args) {
    new Grep().execute(args);
  }

  private void execute(String[] args) {
    try {
      cli = Parser.parseCommandLine(args);
      verbose = cli.isVerbose();
      initSearch();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage());
      Parser.printHelpFormatter();
      System.exit(1);
    }
  }

  private void initSearch() {
    if (verbose) logger.info("Search Keyword : " + cli.getSearchKeyword());
    if (verbose) logger.info("File Path :  " + cli.getWhere() + "\n\n");
    for (String p : cli.getWhere()) {
      Path start = Paths.get(p);
      try {
        Files.walkFileTree(start, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            if (path.toFile().isFile()) {
              if (verbose) logger.info("Visiting File : " + path.toAbsolutePath());
              searchInFile(path);
            }
            return super.visitFile(path, attrs);
          }
        });
      } catch (IOException e) {
        logger.severe(e.getMessage());
      }
    }
  }

  private void searchInFile(Path path) {
    File file = path.toFile();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      int lineNumber = 1;
      while ((line = reader.readLine()) != null) {
        if (cli.isRegex()) {
          Pattern pattern = Pattern.compile(cli.getSearchKeyword());
          Matcher matcher = pattern.matcher(line);
          if (matcher.matches()) {
            logger.info(file.getAbsolutePath() + ":" + lineNumber + " : " + line);
          }
        } else {
          if (line.contains(cli.getSearchKeyword())) {
            logger.info(file.getAbsolutePath() + ":" + lineNumber + " : " + line);
          }
        }
        lineNumber++;
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file : " + file.getAbsolutePath());
    }
  }

}