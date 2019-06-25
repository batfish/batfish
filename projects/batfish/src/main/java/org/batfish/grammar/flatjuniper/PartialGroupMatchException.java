package org.batfish.grammar.flatjuniper;

public class PartialGroupMatchException extends RuntimeException {

  public PartialGroupMatchException(String msg) {
    super(msg);
  }

  public PartialGroupMatchException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
