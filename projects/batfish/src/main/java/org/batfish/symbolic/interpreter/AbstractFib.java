package org.batfish.symbolic.interpreter;

import net.sf.javabdd.BDD;

public class AbstractFib<T> {

  private AbstractRib<T> _rib;

  private BDD _headerspace;

  public AbstractFib(AbstractRib<T> rib, BDD headerspace) {
    this._rib = rib;
    this._headerspace = headerspace;
  }

  public AbstractRib<T> getRib() {
    return _rib;
  }

  public BDD getHeaderspace() {
    return _headerspace;
  }
}
