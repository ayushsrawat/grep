package com.github.drsqrt;

import com.github.drsqrt.cli.Args;
import com.github.drsqrt.cli.Parser;
import com.github.drsqrt.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
      initGrep();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage());
      Parser.printHelpFormatter();
      System.exit(1);
    }
  }

  private void initGrep() {
    if (verbose) logger.info("Search Keyword : " + cli.getSearchKeyword());
    if (verbose) logger.info("File Path :  " + cli.getWhere() + "\n\n");
    for (String p : cli.getWhere()) {
      Path start = Paths.get(p);
      try {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            File file = path.toFile();
            if (file.isFile()) {
              if (verbose) logger.info("Visiting File : " + path.toAbsolutePath());
              searchInFile(file);
            }
            return super.visitFile(path, attrs);
          }

          /** continue visiting other files if visiting failed due to AccessDenied or for some other reasons */
          @Override
          public FileVisitResult visitFileFailed(Path path, IOException exc) {
            return FileVisitResult.SKIP_SUBTREE;
          }
        });
      } catch (IOException e) {
        logger.severe(e.getMessage());
      }
    }
  }

  //todo
  private void searchInFile(File file) {
    if (isBinaryFile(file)) return;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      int lineNumber = 1;
      while ((line = reader.readLine()) != null) {
        if (line.length() > Args.MAX_LINE_LENGTH) return;
        if (cli.isRegex()) {
          Pattern pattern = Pattern.compile(cli.getSearchKeyword());
          Matcher matcher = pattern.matcher(line);
          if (matcher.matches()) {
            logger.info(String.format(
              "\u001B[94m%s\u001B[0m:\u001B[31m%d\u001B[0m : \u001B[32m%s\u001B[0m",
              file.getAbsolutePath(), lineNumber, line
            ));
          }
        } else {
          if (line.contains(cli.getSearchKeyword())) {
            logger.info(String.format(
              "\u001B[36m%s\u001B[0m:\u001B[31m%d\u001B[0m : \u001B[32m%s\u001B[0m",
              file.getAbsolutePath(), lineNumber, line
            ));
          }
        }
        lineNumber++;
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file : " + file.getAbsolutePath());
    }
  }

  public static boolean isBinaryFile(File file) {
    double threshold = 0.3; // 30% non-printable characters threshold
    int checkBytes = 100; // Read first 100 bytes
    int nonPrintableCount = 0;
    int totalRead = 0;

    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[checkBytes];
      int bytesRead = fis.read(buffer);
      if (bytesRead == -1) return false;

      for (int i = 0; i < bytesRead; i++) {
        byte b = buffer[i];
        if (b < 32 && b != 9 && b != 10 && b != 13) { // Ignore tab, newline, and carriage return
          nonPrintableCount++;
        }
        totalRead++;
      }
    } catch (IOException e) {
      return false;
    }
    return totalRead > 0 && ((double) nonPrintableCount / totalRead) > threshold;
  }

}