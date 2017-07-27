package org.batfish.common;

public class RedFlagBatfishException extends BatfishException {

  /** */
  private static final long serialVersionUID = 1L;

  public RedFlagBatfishException(String msg) {
    super(msg);
  }

  public RedFlagBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
