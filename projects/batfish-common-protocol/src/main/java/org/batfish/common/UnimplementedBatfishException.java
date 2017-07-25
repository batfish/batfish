package org.batfish.common;

public class UnimplementedBatfishException extends BatfishException {

  /** */
  private static final long serialVersionUID = 1L;

  public UnimplementedBatfishException(String msg) {
    super(msg);
  }

  public UnimplementedBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
