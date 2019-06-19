package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents the result of applying a Filter (ACL, etc.) to a flow. */
@ParametersAreNonnullByDefault
public class FilterResult {
  @Nonnull private final LineAction _action;
  @Nullable private final Integer _matchLine;

  public FilterResult(@Nullable Integer matchLine, LineAction action) {
    _action = action;
    _matchLine = matchLine;
  }

  @Nonnull
  public LineAction getAction() {
    return _action;
  }

  @Nullable
  public Integer getMatchLine() {
    return _matchLine;
  }
}
