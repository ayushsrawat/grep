package com.github.drsqrt.cli;

import java.util.List;

/**
 * Java Command Line Arguments for GREP
 */
public class Args {

  public static String DEFAULT_FILE_PATH = ".";

  private final String searchKeyword;
  private List<String> where;
  private final Boolean verbose;
  private final Boolean isRegex;

  public Args(Builder builder) {
    this.searchKeyword = builder.searchKeyword;
    this.where = builder.where;
    this.verbose = builder.verbose;
    this.isRegex = builder.isRegex;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getSearchKeyword() {
    return this.searchKeyword;
  }

  public List<String> getWhere() {
    return this.where;
  }

  public Boolean isVerbose() {
    return this.verbose;
  }

  public Boolean isRegex() {
    return this.isRegex;
  }

  public void setWhere(List<String> where) {
    this.where = where;
  }

  public static final class Builder {

    private String searchKeyword;
    private List<String> where;
    private Boolean verbose = false;
    private Boolean isRegex = false;

    public Args build() {
      return new Args(this);
    }

    public Builder searchKeyword(String searchKeyword) {
      this.searchKeyword = searchKeyword;
      return this;
    }

    public Builder where(List<String> where) {
      this.where = where;
      return this;
    }

    public Builder verbose(Boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    public Builder isRegex(Boolean isRegex) {
      this.isRegex = isRegex;
      return this;
    }

  }

}
