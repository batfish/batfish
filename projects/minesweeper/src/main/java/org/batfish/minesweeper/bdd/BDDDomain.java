package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.math.IntMath;
import java.math.RoundingMode;
import java.util.List;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.bdd.MutableBDDInteger;

/**
 * Class that wraps a BDDInteger around a finite collection of values and provides an API for
 * dealing directly with the values. This class is similar to {@link
 * org.batfish.common.bdd.BDDFiniteDomain} but wraps a mutable BDDInteger and so supports updates.
 */
public final class BDDDomain<T> {

  private final @Nonnull BDDFactory _factory;

  private final @Nonnull List<T> _values;

  private @Nonnull MutableBDDInteger _integer;

  public BDDDomain(BDDFactory factory, List<T> values, int index) {
    int bits = numBits(values.size());
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

  /** Returns the constraint that represents any value within the domain. */
  public BDD getIsValidConstraint() {
    return _values.isEmpty() ? _factory.one() : _integer.leq(_values.size() - 1);
  }

  /**
   * Returns the number of bits used to represent a domain of the given size.
   *
   * @param size the number of elements in the domain
   * @return the number of bits required
   */
  public static int numBits(int size) {
    if (size == 0) {
      return 0;
    }
    return IntMath.log2(size, RoundingMode.CEILING);
  }

  public BDD value(T value) {
    int idx = _values.indexOf(value);
    checkArgument(idx != -1, "%s is not in the domain %s", value, _values);
    return _integer.value(idx);
  }

  public T satAssignmentToValue(BDD satAssignment) {
    int idx = _integer.satAssignmentToInt(satAssignment);
    checkArgument(
        idx < _values.size(),
        "The given assignment is not valid in this domain. Was it restricted to valid values?",
        idx);
    return _values.get(idx);
  }

  public void setValue(T value) {
    int idx = _values.indexOf(value);
    checkArgument(
        idx != -1,
        "Cannot set value that is not defined in the domain: %s is not in %s",
        value,
        _values);
    _integer.setValue(idx);
  }

  /**
   * Augments a given pairing to pair corresponding BDDs from the given BDDDomain with this one. The
   * BDDs in the given BDDDomain should all be variables.
   *
   * @param other the BDDDomain of variables
   * @param pairing the existing pairing
   */
  public void augmentPairing(BDDDomain<T> other, BDDPairing pairing) {
    _integer.augmentPairing(other._integer, pairing);
  }

  /** Produces a BDD that represents the support (i.e., the set of BDD variables) of this domain. */
  public BDD support() {
    return _integer.support();
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
    // Values is ignored here, because we should only be checking equality for the same input
    // domain and bit assignment.
    return _integer.equals(other._integer);
  }

  @Override
  public int hashCode() {
    return _integer.hashCode();
  }
}
