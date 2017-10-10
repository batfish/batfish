package org.batfish.symbolic.abstraction;

import java.util.List;
import java.util.Objects;
import net.sf.javabdd.BDD;

/*
 * Class that wraps a BDDInteger around a finite collection of values
 * and provides an API for dealing directly with the values.
 */
public class BDDDomain<T> {

  private List<T> _values;

  private BDDInteger _integer;

  public BDDDomain(List<T> values, int index) {
    int bits = numBits(values);
    _values = values;
    _integer = BDDInteger.makeFromIndex(bits, index);
  }

  public BDDDomain(BDDDomain<T> other) {
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

  @Override public boolean equals(Object o) {
    if (!(o instanceof BDDDomain<?>)) {
      return false;
    }
    BDDDomain<?> other = (BDDDomain<?>) o;
    return Objects.equals(_integer, other._integer);
  }

  @Override public int hashCode() {
    return _integer.hashCode();
  }
}
