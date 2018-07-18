package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.Ip;

public class NatPool implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _first;

  private Ip _last;

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
