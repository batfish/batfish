package org.batfish.specifier.parboiled;

import javax.annotation.ParametersAreNonnullByDefault;
import org.parboiled.parserunners.BasicParseRunner;

/** A helper class that provides auto complete suggestions */
@ParametersAreNonnullByDefault
public final class ParboiledNameUtils {

  public static boolean isValidReferenceObjectName(String name) {
    return new BasicParseRunner<AstNode>(CommonParser.INSTANCE.ReferenceObjectName())
        .run(name)
        .parseErrors
        .isEmpty();
  }
}
