package org.batfish.main;

import java.util.List;
import org.batfish.common.BatfishException;

public class ParserBatfishException extends BatfishException {

  public ParserBatfishException(String msg) {
    super(msg);
  }

  public ParserBatfishException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public ParserBatfishException(String string, List<String> specificErrors) {
    super(string);
    specificErrors.stream().map(ParserBatfishException::new).forEach(this::addSuppressed);
  }
}
