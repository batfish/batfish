package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PsTerm implements Serializable {

  private final @Nonnull PsFroms _froms;
  private final @Nonnull String _name;
  private final @Nonnull PsThens _thens;

  public PsTerm(String name) {
    _froms = new PsFroms();
    _name = name;
    _thens = new PsThens();
  }

  public @Nonnull PsFroms getFroms() {
    return _froms;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull PsThens getThens() {
    return _thens;
  }

  public boolean hasAtLeastOneFrom() {
    return _froms.hasAtLeastOneFrom();
  }
}
