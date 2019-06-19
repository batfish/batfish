package org.batfish.grammar.flatjuniper;

public class PartialGroupMatchException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PartialGroupMatchException(String msg) {
    super(msg);
  }

  public PartialGroupMatchException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
