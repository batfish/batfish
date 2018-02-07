package org.batfish.geometry;

import javax.annotation.Nonnull;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class Rule implements Comparable<Rule> {

  private NodeInterfacePair _link;

  private FibRow _fib;

  public Rule(NodeInterfacePair source, FibRow fib) {
    this._link = source;
    this._fib = fib;
  }

  public NodeInterfacePair getLink() {
    return _link;
  }

  public FibRow getFib() {
    return _fib;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Rule rule = (Rule) o;

    return (_link != null ? _link.equals(rule._link) : rule._link == null)
        && (_fib != null ? _fib.equals(rule._fib) : rule._fib == null);
  }

  @Override
  public int hashCode() {
    int result = _link != null ? _link.hashCode() : 0;
    result = 31 * result + (_fib != null ? _fib.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(@Nonnull Rule that) {
    return _fib.getPrefix().getPrefixLength() - that._fib.getPrefix().getPrefixLength();
  }
}
