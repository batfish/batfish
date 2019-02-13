package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;

public class BatfishParseException extends BatfishException {

  private static final long serialVersionUID = 1L;

  private final ParserRuleContext _context;

  public BatfishParseException(String msg, Throwable cause, ParserRuleContext context) {
    super(msg, cause);
    _context = context;
  }

  public ParserRuleContext getContext() {
    return _context;
  }
}
