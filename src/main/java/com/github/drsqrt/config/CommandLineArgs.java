package com.github.drsqrt.config;

public class CommandLineArgs {

  public static String DEFAULT_FILE_PATH = ".";

  private final String searchKeyword;
  private String filePath;
  private final Boolean verbose;

  public CommandLineArgs(Builder builder) {
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

    public CommandLineArgs build() {
      if (searchKeyword == null || searchKeyword.isEmpty()) {
        throw new IllegalArgumentException("Search Keyword is required.");
      }
      return new CommandLineArgs(this);
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
