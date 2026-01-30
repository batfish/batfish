package org.batfish.grammar.huawei.parsing;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.TokenStream;
import org.batfish.grammar.BatfishParser;

/** Huawei parser base class providing validation functionality on top of {@link BatfishParser}. */
@ParametersAreNonnullByDefault
public abstract class HuaweiBaseParser extends BatfishParser {

  public HuaweiBaseParser(TokenStream input) {
    super(input);
  }
}
