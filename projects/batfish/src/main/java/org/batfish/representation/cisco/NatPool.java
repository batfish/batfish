package org.batfish.representation.cisco;

import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.Ip;

public class NatPool extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _first;

  private Ip _last;

  public NatPool(String name, int definitionLine) {
    super(name, definitionLine);
  }

  public Ip getFirst() {
    return _first;
  }

  public Ip getLast() {
    return _last;
  }

  public void setFirst(Ip first) {
    _first = first;
  }

  public void setLast(Ip last) {
    _last = last;
  }
}
