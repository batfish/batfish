package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents the result of applying a Filter (ACL, etc.) to a flow. */
@ParametersAreNonnullByDefault
public class FilterResult {
  private final @Nonnull LineAction _action;
  private final @Nullable Integer _matchLine;

  public FilterResult(@Nullable Integer matchLine, LineAction action) {
    _action = action;
    _matchLine = matchLine;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nullable Integer getMatchLine() {
    return _matchLine;
  }
}
