package org.batfish.geometry;

import javax.annotation.Nonnull;

public class Rule implements Comparable<Rule> {

  private GraphLink _link;
  private EquivalenceClass _equivClass;
  private int _priority;

  public Rule(GraphLink link, EquivalenceClass ec, int priority) {
    this._link = link;
    this._equivClass = ec;
    this._priority = priority;
  }

  public GraphLink getLink() {
    return _link;
  }

  public EquivalenceClass getEquivalenceClass() {
    return _equivClass;
  }

  public int getPriority() {
    return _priority;
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

    if (_priority != rule._priority) {
      return false;
    }
    if (!_link.equals(rule._link)) {
      return false;
    }
    return _equivClass.equals(rule._equivClass);
  }

  @Override
  public int hashCode() {
    int result = _link.hashCode();
    result = 31 * result + _equivClass.hashCode();
    result = 31 * result + _priority;
    return result;
  }

  @Override public String toString() {
    return "Rule{" + "_link=" + _link + ", _priority=" + _priority + '}';
  }

  @Override
  public int compareTo(@Nonnull Rule that) {
    return _priority - that._priority;
  }
}
