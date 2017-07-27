package org.batfish.main;

import org.batfish.common.BatfishException;

public class ParserBatfishException extends BatfishException {

  /** */
  private static final long serialVersionUID = 1L;

  public ParserBatfishException(String msg) {
    super(msg);
  }

  public ParserBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
