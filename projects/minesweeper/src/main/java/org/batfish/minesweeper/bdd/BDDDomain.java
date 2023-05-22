package org.batfish.minesweeper.bdd;

import java.util.List;
import java.util.Objects;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.MutableBDDInteger;

/**
 * Class that wraps a BDDInteger around a finite collection of values and provides an API for
 * dealing directly with the values. This class is similar to {@link
 * org.batfish.common.bdd.BDDFiniteDomain} but wraps a mutable BDDInteger and so supports updates.
 */
public class BDDDomain<T> {

  private BDDFactory _factory;

  private List<T> _values;

  private MutableBDDInteger _integer;

  public BDDDomain(BDDFactory factory, List<T> values, int index) {
    int bits = numBits(values);
    _factory = factory;
    _values = values;
    _integer = MutableBDDInteger.makeFromIndex(_factory, bits, index, false);
  }

  public BDDDomain(BDDDomain<T> other) {
    _factory = other._factory;
    _values = other._values;
    _integer = new MutableBDDInteger(other._integer);
  }

  /**
   * @param pred a predicate based on which the given BDDDomain is restricted.
   * @param other A BDDDomain
   */
  public BDDDomain(BDD pred, BDDDomain<T> other) {
    _factory = other._factory;
    _values = other._values;
    _integer = other.getInteger().and(pred);
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

  public T satAssignmentToValue(BDD satAssignment) {
    int idx = _integer.satAssignmentToInt(satAssignment);
    return _values.get(idx);
  }

  public void setValue(T value) {
    int idx = _values.indexOf(value);
    _integer.setValue(idx);
  }

  public MutableBDDInteger getInteger() {
    return _integer;
  }

  public void setInteger(MutableBDDInteger i) {
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
