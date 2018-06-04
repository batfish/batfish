package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class NatPool extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _first;

  private Ip _last;

  public NatPool(String name) {
    super(name);
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
