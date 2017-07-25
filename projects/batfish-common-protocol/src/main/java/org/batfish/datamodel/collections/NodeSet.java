package org.batfish.datamodel.collections;

import java.util.Collection;
import java.util.TreeSet;

public class NodeSet extends TreeSet<String> {

  /** */
  private static final long serialVersionUID = 1L;

  public NodeSet() {}

  public NodeSet(Collection<String> c) {
    super(c);
  }
}
