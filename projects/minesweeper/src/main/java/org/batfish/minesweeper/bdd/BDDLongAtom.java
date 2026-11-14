package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;
import java.math.RoundingMode;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

/**
 * Represents a symbolic non-negative long-valued route attribute (such as a Junos {@code tag2} or
 * {@code color}) that route policies only ever match or set against a small set of known literal
 * values.
 *
 * <p>Rather than tracking every bit of the value, the attribute is modeled as an atom domain whose
 * values are: 0 (the attribute is unset), each known literal, and a single OTHER value standing for
 * every value that is neither 0 nor a known literal. This needs only {@code log2(#literals + 2)}
 * BDD variables.
 */
@ParametersAreNonnullByDefault
public final class BDDLongAtom {

  /**
   * The domain value standing for any value other than 0 or a known literal. It is negative, so it
   * is never a real attribute value.
   */
  private static final long OTHER = -1L;

  /**
   * Allocates a new {@link BDDLongAtom} at the given variable index in the given factory.
   *
   * @param knownValues the literal values tracked precisely, which must be positive and strictly
   *     increasing
   */
  public static @Nonnull BDDLongAtom create(BDDFactory factory, int index, List<Long> knownValues) {
    checkKnownValues(knownValues);
    // Note: the domain values are ordered so that 0 < any known value < OTHER so that choosing the
    // minimum value results in the most understandable result.
    List<Long> domainValues =
        ImmutableList.<Long>builderWithExpectedSize(knownValues.size() + 2)
            .add(0L)
            .addAll(knownValues)
            .add(OTHER)
            .build();
    return new BDDLongAtom(
        new BDDDomain<>(factory, domainValues, index), otherConcreteValue(knownValues));
  }

  /** Creates a copy of the argument. Each can be mutated separately. */
  public static @Nonnull BDDLongAtom copyOf(BDDLongAtom other) {
    return new BDDLongAtom(new BDDDomain<>(other._domain), other._otherConcreteValue);
  }

  /**
   * Returns the number of bits needed to build a {@link BDDLongAtom} with the given set of known
   * values.
   */
  public static int numBitsFor(List<Long> knownValues) {
    // 0, known values, and OTHER value not in the known set.
    return IntMath.log2(knownValues.size() + 2, RoundingMode.CEILING);
  }

  private static void checkKnownValues(List<Long> knownValues) {
    long previous = 0L;
    for (long value : knownValues) {
      checkArgument(
          value > previous,
          "Known values must be positive and strictly increasing: %s",
          knownValues);
      previous = value;
    }
  }

  /** The smallest positive value that is not one of the (sorted) known values. */
  private static long otherConcreteValue(List<Long> sortedKnownValues) {
    long candidate = 1L;
    for (long value : sortedKnownValues) {
      if (value != candidate) {
        break;
      }
      candidate++;
    }
    return candidate;
  }

  /** Returns a new {@link BDDLongAtom} restricted to the given predicate. */
  public @Nonnull BDDLongAtom and(BDD pred) {
    return new BDDLongAtom(new BDDDomain<>(pred, _domain), _otherConcreteValue);
  }

  /**
   * Produces a new attribute by composing the underlying domain through the given pairing (see
   * {@link BDD#veccompose(BDDPairing)}), for functional composition of symbolic routes.
   */
  public @Nonnull BDDLongAtom veccompose(BDDPairing pairing) {
    return new BDDLongAtom(_domain.veccompose(pairing), _otherConcreteValue);
  }

  /**
   * Augments a given pairing to pair corresponding BDDs from the given {@link BDDLongAtom} with
   * this one. The BDDs in the given {@link BDDLongAtom} should all be variables.
   *
   * @param other the {@link BDDLongAtom} of variables
   * @param pairing the existing pairing
   */
  public void augmentPairing(BDDLongAtom other, BDDPairing pairing) {
    _domain.augmentPairing(other._domain, pairing);
  }

  /**
   * Produces a BDD that represents the support (i.e., the set of BDD variables) of this attribute.
   */
  public @Nonnull BDD support() {
    return _domain.support();
  }

  /**
   * Produces a BDD whose models represent all possible differences between this and the other
   * {@link BDDLongAtom}.
   */
  public @Nonnull BDD allDifferences(BDDLongAtom other) {
    return _domain.getInteger().allDifferences(other._domain.getInteger());
  }

  /** Returns the constraint that the attribute takes one of the values in its domain. */
  public @Nonnull BDD getIsValidConstraint() {
    return _domain.getIsValidConstraint();
  }

  /** Frees every BDD backing this attribute's underlying domain; see {@link BDDDomain#free}. */
  public void free() {
    _domain.free();
  }

  public int getNumBits() {
    return _domain.getInteger().size();
  }

  /** Sets this attribute to the given value, which must be 0 (unset) or one of the known values. */
  public void setValue(long value) {
    checkArgument(value >= 0, "Value must be non-negative: %s", value);
    _domain.setValue(value);
  }

  /**
   * Returns the BDD representing that this attribute has the given value, which must be 0 (unset)
   * or one of the known values.
   */
  public @Nonnull BDD value(long value) {
    checkArgument(value >= 0, "Value must be non-negative: %s", value);
    return _domain.value(value);
  }

  /**
   * Returns a concrete value of this attribute consistent with the given satisfying assignment. The
   * OTHER domain value is converted to a value that is neither 0 nor any known value.
   */
  public long satAssignmentToLong(BDD model) {
    long value = _domain.satAssignmentToValue(model);
    return value == OTHER ? _otherConcreteValue : value;
  }

  private BDDLongAtom(BDDDomain<Long> domain, long otherConcreteValue) {
    _domain = domain;
    _otherConcreteValue = otherConcreteValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BDDLongAtom)) {
      return false;
    }
    BDDLongAtom that = (BDDLongAtom) o;
    return _domain.equals(that._domain);
  }

  @Override
  public int hashCode() {
    return _domain.hashCode();
  }

  // Note: the domain values are ordered so that 0 < any known value < OTHER so that choosing the
  // minimum value results in the most understandable result.
  private final @Nonnull BDDDomain<Long> _domain;

  /** The concrete value reported for the OTHER domain value. */
  private final long _otherConcreteValue;
}
