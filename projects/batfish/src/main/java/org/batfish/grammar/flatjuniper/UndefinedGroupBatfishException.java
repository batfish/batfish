package org.batfish.grammar.flatjuniper;

import org.batfish.common.BatfishException;

public class UndefinedGroupBatfishException extends BatfishException {

  public UndefinedGroupBatfishException(String msg) {
    super(msg);
  }

  public UndefinedGroupBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
