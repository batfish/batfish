package org.batfish.datamodel;

import org.batfish.common.Pair;

public class FilterResult extends Pair<Integer, LineAction> {

  /** */
  private static final long serialVersionUID = 1L;

  public FilterResult(Integer t1, LineAction t2) {
    super(t1, t2);
  }

  public LineAction getAction() {
    return _second;
  }

  public Integer getMatchLine() {
    return _first;
  }
}
