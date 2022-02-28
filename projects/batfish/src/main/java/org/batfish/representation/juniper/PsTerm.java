package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PsTerm implements Serializable {

  private final @Nonnull PsFroms _froms;
  private final @Nonnull String _name;
  private final @Nonnull Set<PsThen> _thens;

  public PsTerm(String name) {
    _froms = new PsFroms();
    _name = name;
    _thens = new LinkedHashSet<>();
  }

  public @Nonnull PsFroms getFroms() {
    return _froms;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<PsThen> getThens() {
    return _thens;
  }

  public boolean hasAtLeastOneFrom() {
    return _froms.hasAtLeastOneFrom();
  }
}
