package org.batfish.common;

public class PedanticBatfishException extends BatfishException {

  /** */
  private static final long serialVersionUID = 1L;

  public PedanticBatfishException(String msg) {
    super(msg);
  }

  public PedanticBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
