package com.github.drsqrt.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Command-Line-Parser
 */
public class Parser {

  private static final Logger logger = Logger.getLogger(Parser.class.getName());
  private static final String TILDE = "~";

  public static Args parseCommandLine(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options options = createOptions();
      CommandLine commandLine = parser.parse(options, args);
      Args cli = parseCommandLineArgs(commandLine);
      Parser.validateCommandLineArguments(cli);
      return cli;
    } catch (ParseException parseException) {
      logger.severe(parseException.getMessage());
      throw new RuntimeException(parseException);
    }
  }

  private static Options createOptions() {
    Options options = new Options();
    options.addOption(new Option("v", "verbose", false, "verbose output"));
    options.addOption(new Option("r", "regex", false, "search as regex"));
    return options;
  }

  private static Args parseCommandLineArgs(CommandLine cli) {
    String[] keywordAndFiles = cli.getArgs();
    if (keywordAndFiles.length == 0) {
      throw new IllegalArgumentException("provide search keyword as the first argument");
    }
    String searchKeyword = keywordAndFiles[0];
    List<String> where = new ArrayList<>(Arrays.asList(keywordAndFiles).subList(1, keywordAndFiles.length));
    boolean verbose = cli.hasOption("v");
    boolean isRegex = cli.hasOption("r");
    return Args.builder()
      .searchKeyword(searchKeyword)
      .where(where)
      .verbose(verbose)
      .isRegex(isRegex)
      .build();
  }

  private static void validateCommandLineArguments(Args cli) {
    String searchKeyword = cli.getSearchKeyword();
    if (searchKeyword == null || searchKeyword.isEmpty()) {
      throw new IllegalArgumentException("Please provide search keyword with -s flag");
    }
    List<String> where = cli.getWhere();
    if (where.isEmpty()) {
      where.add(Args.DEFAULT_FILE_PATH);
    }
    if (TILDE.equals(where.getFirst())) {
      where.set(0, System.getProperty("user.home"));
    }
    cli.setWhere(where);
    for (String p : cli.getWhere()) {
      Path path = Paths.get(p);
      if (!Files.exists(path)) {
        throw new IllegalArgumentException("File Path does not exists : " + path);
      }
    }
  }

  public static void printHelpFormatter() {
    new HelpFormatter().printHelp("Grep", createOptions());
  }
}

