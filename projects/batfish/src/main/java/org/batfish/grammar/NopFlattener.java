package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;

/** {@link Flattener} implementation that just returns its input unmodified. */
@ParametersAreNonnullByDefault
public final class NopFlattener implements Flattener {

  public NopFlattener(String input) {
    _input = input;
  }

  @Override
  public @Nonnull String getFlattenedConfigurationText() {
    return _input;
  }

  @Override
  public @Nullable FlattenerLineMap getOriginalLineMap() {
    return null;
  }

  private final @Nonnull String _input;
}
