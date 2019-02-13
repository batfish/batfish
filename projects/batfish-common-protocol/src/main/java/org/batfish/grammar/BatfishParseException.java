package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;

@ParametersAreNonnullByDefault
public class BatfishParseException extends BatfishException {

  private static final long serialVersionUID = 1L;

  @Nonnull private final ParserRuleContext _context;

  public BatfishParseException(String msg, @Nullable Throwable cause, ParserRuleContext context) {
    super(msg, cause);
    _context = context;
  }

  public @Nonnull ParserRuleContext getContext() {
    return _context;
  }
}
