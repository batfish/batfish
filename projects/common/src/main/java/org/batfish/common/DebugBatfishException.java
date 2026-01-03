package org.batfish.common;

public class DebugBatfishException extends BatfishException {

  public DebugBatfishException(String msg) {
    super(msg);
  }

  public DebugBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
