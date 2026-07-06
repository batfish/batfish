package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public abstract class BDDInteger implements Serializable {
  protected final BDDFactory _factory;
  protected final BDD[] _bitvec;
  protected final long _maxVal;

  protected BDDInteger(BDDFactory factory, BDD[] bitvec) {
    checkArgument(bitvec.length < 64, "Only lengths up to 63 are supported");
    _factory = factory;
    _bitvec = bitvec;
    _maxVal = bitvec.length == 0 ? 0L : 0xFFFF_FFFF_FFFF_FFFFL >>> (64 - bitvec.length);
  }

  /** Returns the number of bits in this {@link BDDInteger}. */
  public int size() {
    return _bitvec.length;
  }

  /** Find a representative value of the represented integer that satisfies a given constraint. */
  public abstract Optional<Long> getValueSatisfying(BDD bdd);

  public abstract long satAssignmentToLong(BDD satAssignment);

  public int satAssignmentToInt(BDD bdd) {
    checkArgument(
        _bitvec.length <= 31, "Only BDDInteger of 31 or fewer bits can be converted to int");
    return (int) satAssignmentToLong(bdd);
  }

  /**
   * Return a list of values satisfying the input {@link BDD}, up to some maximum number.
   *
   * @param bdd A constraint on this.
   * @param max The maximum number of values desired.
   * @return The satisfying values.
   */
  public List<Long> getValuesSatisfying(BDD bdd, int max) {
    ImmutableList.Builder<Long> values = new ImmutableList.Builder<>();

    checkArgument(max > 0, "max must be > 0");

    int num = 0;
    BDD pred = bdd;
    while (num < max) {
      if (pred.isZero()) {
        break;
      }
      long val = satAssignmentToLong(pred.satOne());
      values.add(val);
      pred = pred.diff(value(val));
      num++;
    }
    return values.build();
  }

  /** Build a constraint that matches the set of IPs contained by the input {@link Prefix}. */
  public final BDD toBDD(Prefix prefix) {
    checkArgument(
        _bitvec.length == Prefix.MAX_PREFIX_LENGTH,
        "toBDD(Prefix) requires %s bits",
        Prefix.MAX_PREFIX_LENGTH);
    return firstBitsEqual(prefix.getStartIp().asLong(), prefix.getPrefixLength());
  }

  /** Build a constraint that matches the input {@link Ip}. */
  public final BDD toBDD(Ip ip) {
    checkArgument(
        _bitvec.length == Prefix.MAX_PREFIX_LENGTH,
        "toBDD(Ip) requires %s bits",
        Prefix.MAX_PREFIX_LENGTH);
    return firstBitsEqual(ip.asLong(), Prefix.MAX_PREFIX_LENGTH);
  }

  protected abstract BDD firstBitsEqual(long val, int length);

  /** Build a constraint that matches the {@link Ip IPs} matched by the input {@link IpWildcard}. */
  public abstract BDD toBDD(IpWildcard ipWildcard);

  /*
   * Create a BDD representing the exact value
   */
  public final BDD value(long val) {
    checkArgument(val >= 0, "value is negative");
    checkArgument(val <= _maxVal, "value %s is out of range [0, %s]", val, _maxVal);
    return firstBitsEqual(val, _bitvec.length);
  }

  // Helper function to compute leq on the last N bits of the input value.
  private BDD leqN(long val, int n) {
    assert n <= _bitvec.length;
    long currentVal = val;
    BDD acc = _factory.one(); // whether the suffix of BDD is leq suffix of val.
    for (int i = 0; i < n; ++i) {
      BDD bit = _bitvec[_bitvec.length - i - 1];
      if ((currentVal & 1) != 0) {
        // since this bit of val is 1: 0 implies lt OR 1 and suffix leq. ('1 and' is redundant).
        acc.invimpEq(bit); // "not i or acc" rewritten "i implies acc" and flipped.
      } else {
        // since this bit of val is 0: must be 0 and have leq suffix.
        acc.diffEq(bit); // "not i and acc" rewritten "i less acc" and flipped.
      }
      currentVal >>= 1;
    }
    return acc;
  }

  /*
   * Less than or equal to on integers
   */
  public BDD leq(long val) {
    checkArgument(val >= 0, "value is negative");
    checkArgument(val <= _maxVal, "value %s is out of range [0, %s]", val, _maxVal);
    return leqN(val, _bitvec.length);
  }

  // Helper function to compute geq on the last N bits of the input value.
  private BDD geqN(long val, int n) {
    assert n <= _bitvec.length;
    long currentVal = val;
    BDD acc = _factory.one(); // whether the suffix of BDD is geq suffix of val.
    for (int i = 0; i < n; ++i) {
      BDD bit = _bitvec[_bitvec.length - i - 1];
      if ((currentVal & 1) != 0) {
        // since this bit of val is 1: must be 1 and have geq suffix.
        acc.andEq(bit);
      } else {
        // since this bit of val is 0: 1 implies gt OR 0 and suffix geq. ('0 and' is redundant.)
        acc.orEq(bit);
      }
      currentVal >>= 1;
    }
    return acc;
  }

  /*
   * Greater than or equal to on integers
   */
  public BDD geq(long val) {
    checkArgument(val >= 0, "value is negative");
    checkArgument(val <= _maxVal, "value %s is out of range [0, %s]", val, _maxVal);
    return geqN(val, _bitvec.length);
  }

  /*
   * Integers in the given range, inclusive, where {@code a} is less than or equal to {@code b}.
   */
  // This is basically this.geq(a).and(this.leq(b)). Differences:
  //   1. Short-circuit a == b
  //   2. Save work in the case where a and b have a common prefix, including when a and/or b is the
  //      start/end of the prefix.
  public BDD range(long a, long b) {
    checkArgument(a <= b, "range is not ordered correctly");
    checkArgument(a >= 0, "value is negative");
    checkArgument(b <= _maxVal, "value %s is out of range [0, %s]", b, _maxVal);

    if (a == b) {
      return value(a);
    }

    long bitOfFirstDifference = Long.highestOneBit(a ^ b);
    int sizeOfDifferentSuffix = Long.numberOfTrailingZeros(bitOfFirstDifference) + 1;
    assert sizeOfDifferentSuffix < 64;

    long suffixMask = 0xFFFF_FFFF_FFFF_FFFFL >>> (64 - sizeOfDifferentSuffix);
    BDD lower = ((a & suffixMask) == 0) ? _factory.one() : geqN(a, sizeOfDifferentSuffix);
    BDD upper = ((b & suffixMask) == suffixMask) ? _factory.one() : leqN(b, sizeOfDifferentSuffix);
    BDD between = lower.andWith(upper);
    long currentVal = a >> sizeOfDifferentSuffix;
    for (int i = sizeOfDifferentSuffix; i < _bitvec.length; ++i) {
      BDD bit = _bitvec[_bitvec.length - i - 1];
      if ((currentVal & 1) != 0) {
        between = between.andEq(bit);
      } else {
        between = between.diffEq(bit); // "not i and x" rewritten "i less x"
      }
      currentVal >>= 1;
    }
    return between;
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  // ---- symbolic (integer-vs-integer) comparisons ------------------------------------------------
  //
  // The above leq/geq/range compare this integer against a *constant*. The methods below compare
  // this integer against another *symbolic* BDDInteger, producing a BDD over both operands'
  // variables: the set of assignments under which the relation holds. Both integers must have the
  // same bit width. These are unsigned comparisons; _bitvec is most-significant-bit first.

  /**
   * The set of assignments under which this integer is strictly less than {@code other} (unsigned).
   * Both integers must have the same width.
   */
  public BDD lt(BDDInteger other) {
    return ltImpl(other, /* orEqual= */ false);
  }

  /**
   * The set of assignments under which this integer is less than or equal to {@code other}
   * (unsigned). Both integers must have the same width.
   */
  public BDD leq(BDDInteger other) {
    return ltImpl(other, /* orEqual= */ true);
  }

  /**
   * The set of assignments under which this integer is strictly greater than {@code other}
   * (unsigned). Both integers must have the same width.
   */
  public BDD gt(BDDInteger other) {
    return other.ltImpl(this, /* orEqual= */ false);
  }

  /**
   * The set of assignments under which this integer is greater than or equal to {@code other}
   * (unsigned). Both integers must have the same width.
   */
  public BDD geq(BDDInteger other) {
    return other.ltImpl(this, /* orEqual= */ true);
  }

  /**
   * The set of assignments under which this integer equals {@code other} bit-for-bit. Both integers
   * must have the same width.
   */
  public BDD eq(BDDInteger other) {
    return firstBitsEqual(other, _bitvec.length);
  }

  /**
   * The set of assignments under which this integer and {@code other} agree on their first {@code
   * numBits} (most-significant) bits, ignoring the lower bits. For an IP-valued integer this is
   * "the two addresses are in the same {@code /numBits} subnet". {@code numBits == } the width
   * reduces to {@link #eq}; {@code numBits == 0} is unconstrained ({@code one}). Both integers must
   * have the same width.
   */
  public BDD firstBitsEqual(BDDInteger other, int numBits) {
    checkArgument(_bitvec.length == other._bitvec.length, "operands must have equal width");
    checkArgument(numBits >= 0 && numBits <= _bitvec.length, "numBits out of range: %s", numBits);
    // _bitvec is most-significant-bit first, so the first numBits entries are the high bits.
    BDD acc = _factory.one();
    for (int i = 0; i < numBits; i++) {
      // bit-i equal: biimp is the xnor, computed directly and preserving both operands.
      acc = acc.andWith(_bitvec[i].biimp(other._bitvec[i]));
    }
    return acc;
  }

  /**
   * Unsigned "this {@code <} other" (or {@code <=} when {@code orEqual}), as a
   * most-significant-bit- first lexicographic comparison: scanning from the high bit, the result is
   * determined at the first differing bit (this has 0, other has 1 => less). Implemented with a
   * single backward sweep: {@code acc} is the relation on the suffix already processed; at each
   * more-significant bit {@code acc' = (a < b) OR (a == b AND acc)}.
   */
  private BDD ltImpl(BDDInteger other, boolean orEqual) {
    checkArgument(_bitvec.length == other._bitvec.length, "operands must have equal width");
    // Base case on the empty suffix: equal (so <= is true, < is false).
    BDD acc = orEqual ? _factory.one() : _factory.zero();
    // Scan least-significant to most-significant (high index to low index).
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      BDD a = _bitvec[i];
      BDD b = other._bitvec[i];
      // thisBitLess = !a & b ; bitsEqual = a <-> b.
      BDD thisBitLess = b.diff(a); // b AND NOT a
      BDD bitsEqual = a.biimp(b);
      // acc = thisBitLess OR (bitsEqual AND acc)
      acc = thisBitLess.orWith(bitsEqual.andWith(acc));
    }
    return acc;
  }
}
