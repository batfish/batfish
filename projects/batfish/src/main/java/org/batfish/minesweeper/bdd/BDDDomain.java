package org.batfish.minesweeper.bdd;

import java.util.List;
import java.util.Objects;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDInteger;

/*
 * Class that wraps a BDDInteger around a finite collection of values
 * and provides an API for dealing directly with the values.
 */
public class BDDDomain<T> {

  private BDDFactory _factory;

  private List<T> _values;

  private BDDInteger _integer;

  public BDDDomain(BDDFactory factory, List<T> values, int index) {
    int bits = numBits(values);
    _factory = factory;
    _values = values;
    _integer = BDDInteger.makeFromIndex(_factory, bits, index, false);
  }

  public BDDDomain(BDDDomain<T> other) {
    _factory = other._factory;
    _values = other._values;
    _integer = new BDDInteger(other._integer);
  }

  private int numBits(List<T> values) {
    int size = values.size();
    double log = Math.log((double) size);
    double base = Math.log((double) 2);
    if (size == 0) {
      return 0;
    } else {
      return (int) Math.ceil(log / base);
    }
  }

  public BDD value(T value) {
    int idx = _values.indexOf(value);
    return _integer.value(idx);
  }

  public void setValue(T value) {
    int idx = _values.indexOf(value);
    _integer.setValue(idx);
  }

  public BDDInteger getInteger() {
    return _integer;
  }

  public void setInteger(BDDInteger i) {
    _integer = i;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDDomain<?>)) {
      return false;
    }
    BDDDomain<?> other = (BDDDomain<?>) o;
    return Objects.equals(_integer, other._integer);
  }

  @Override
  public int hashCode() {
    return _integer.hashCode();
  }
}
