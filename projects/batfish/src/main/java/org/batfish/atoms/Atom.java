package org.batfish.atoms;

import java.util.HashSet;
import java.util.Set;
import net.sf.javabdd.BDD;

public class Atom {

  private Set<Integer> _labels;

  private BDD _bdd;

  public Atom(Set<Integer> labels, BDD bdd) {
    this._labels = labels;
    this._bdd = bdd;
  }

  public Atom(Integer label, BDD bdd) {
    this._labels = new HashSet<>();
    this._labels.add(label);
    this._bdd = bdd;
  }

  public Atom(BDD bdd) {
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
