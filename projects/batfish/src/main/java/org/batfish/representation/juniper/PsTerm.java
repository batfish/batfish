package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PsTerm implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Set<PsFrom> _froms;

  private final String _name;

  private final Set<PsThen> _thens;

  public PsTerm(String name) {
    _froms = new LinkedHashSet<>();
    _name = name;
    _thens = new LinkedHashSet<>();
  }

  public Set<PsFrom> getFroms() {
    return _froms;
  }

  public String getName() {
    return _name;
  }

  public Set<PsThen> getThens() {
    return _thens;
  }
}
