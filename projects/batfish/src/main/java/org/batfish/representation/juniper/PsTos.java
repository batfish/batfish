package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents all {@link PsTo} statements in a single {@link PsTerm} */
public final class PsTos implements Serializable {

  private PsToLevel _toLevel;
  private final Set<PsToProtocol> _toProtocols;
  private PsToRib _toRib;

  PsTos() {
    _toProtocols = new LinkedHashSet<>();
  }

  public void addToProtocol(@Nonnull PsToProtocol toProtocol) {
    _toProtocols.add(toProtocol);
  }

  public @Nonnull Set<PsToProtocol> getToProtocols() {
    return _toProtocols;
  }

  public void setToLevel(@Nonnull PsToLevel toLevel) {
    _toLevel = toLevel;
  }

  public @Nullable PsToLevel getToLevel() {
    return _toLevel;
  }

  public void setToRib(@Nonnull PsToRib toRib) {
    _toRib = toRib;
  }

  public @Nullable PsToRib getToRib() {
    return _toRib;
  }

  boolean hasAtLeastOneTo() {
    return _toLevel != null || !_toProtocols.isEmpty() || _toRib != null;
  }
}
