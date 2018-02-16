package org.batfish.atoms;

import java.util.HashSet;
import java.util.Set;
import net.sf.javabdd.BDD;

/*
 * An atomic predicate is represented as a thin wrapper around
 * a BDD. It maintains a set of integer labels that act as a
 * way of storing provenance information about which predicates
 * contributed to the creation of this predicate.
 */
public class Atom {

  private Set<Integer> _labels;

  private BDD _bdd;

  Atom(Set<Integer> labels, BDD bdd) {
    this._labels = labels;
    this._bdd = bdd;
  }

  Atom(Integer label, BDD bdd) {
    this._labels = new HashSet<>();
    this._labels.add(label);
    this._bdd = bdd;
  }

  Atom(BDD bdd) {
    this._labels = new HashSet<>();
    this._bdd = bdd;
  }

  public Set<Integer> getLabels() {
    return _labels;
  }

  public BDD getBdd() {
    return _bdd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Atom that = (Atom) o;
    return _bdd.equals(that._bdd);
  }

  @Override
  public int hashCode() {
    return _bdd.hashCode();
  }

  @Override
  public String toString() {
    return "Atom{" + "_labels=" + _labels + ", _bdd=" + _bdd.hashCode() + '}';
  }
}
