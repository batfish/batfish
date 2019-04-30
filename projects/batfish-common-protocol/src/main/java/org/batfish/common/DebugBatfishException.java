package org.batfish.common;

public class DebugBatfishException extends BatfishException {

  private static final long serialVersionUID = 1L;

  public DebugBatfishException(String msg) {
    super(msg);
  }

  public DebugBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
