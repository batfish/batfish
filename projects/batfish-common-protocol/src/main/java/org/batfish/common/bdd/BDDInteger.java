package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public class BDDInteger {

  private final BDDFactory _factory;
  private final BDD[] _bitvec;
  private final long _maxVal;

  // Temporary ArrayLists used to optimize some internal computations.
  private final List<BDD> _trues;
  private final List<BDD> _falses;

  /** Certain API calls are only valid when this BDD has only variables in it. */
  private boolean _hasVariablesOnly;

  private BDD _vars;

  /*
   * Create an integer, but don't initialize its bit values
   */
  private BDDInteger(BDDFactory factory, int length) {
    checkArgument(length < 64, "Only lengths up to 63 are supported");
    _factory = factory;
    _bitvec = new BDD[length];
    _maxVal = 0xFFFF_FFFF_FFFF_FFFFL >>> (64 - length);
    _hasVariablesOnly = false;
    _trues = new ArrayList<>(length);
    _falses = new ArrayList<>(length);
  }

  public BDDInteger(BDDFactory factory, BDD[] bitvec) {
    this(factory, bitvec.length);
    _hasVariablesOnly = true; // TODO enforce this
    for (int i = 0; i < bitvec.length; i++) {
      _bitvec[i] = bitvec[i];
    }
  }

  public BDDInteger(BDDInteger other) {
    this(other._factory, other._bitvec.length);
    setValue(other);
  }

  /**
   * Returns true if this {@link BDDInteger} has only variables in its bitvec, as opposed to
   * zero/one and more complex BDDs.
   *
   * <p>This is typically true only for {@link BDDInteger} values constructed from {@link
   * BDDInteger#makeFromIndex(BDDFactory, int, int, boolean)} or copied from them.
   */
  public boolean hasVariablesOnly() {
    return _hasVariablesOnly;
  }

  /** Returns the number of bits in this {@link BDDInteger}. */
  public int size() {
    return _bitvec.length;
  }

  /**
   * Create an integer, and initialize its values as "don't care" This requires knowing the start
   * index variables the bitvector will use.
   */
  public static BDDInteger makeFromIndex(
      BDDFactory factory, int length, int start, boolean reverse) {
    assert factory.varNum() >= start + length;

    BDDInteger bdd = new BDDInteger(factory, length);
    for (int i = 0; i < length; i++) {
      int idx;
      if (reverse) {
        idx = start + length - i - 1;
      } else {
        idx = start + i;
      }
      bdd._bitvec[i] = bdd._factory.ithVar(idx);
    }
    bdd._hasVariablesOnly = true;
    return bdd;
  }

  /** Find a representative value of the represented integer that satisfies a given constraint. */
  public Optional<Long> getValueSatisfying(BDD bdd) {
    if (bdd.isZero()) {
      return Optional.empty();
    }
    if (_hasVariablesOnly) {
      return Optional.of(satAssignmentToLong(bdd.minAssignmentBits()));
    }
    return Optional.of(satAssignmentToLong(bdd.satOne()));
  }

  /**
   * Returns the smallest long consistent with the input assignment and this {@link BDDInteger}.
   *
   * <p>When this {@link BDDInteger#hasVariablesOnly()} is {@code false}, this function will perform
   * better if the assignment {@link BDD} is smaller, i.e., is produced by {@link BDD#satOne()}
   * instead of {@link BDD#fullSatOne()}.
   */
  public Long satAssignmentToLong(BDD satAssignment) {
    checkArgument(satAssignment.isAssignment(), "not a satisfying assignment");

    if (_hasVariablesOnly) {
      // Shortcut for performance.
      return satAssignmentToLong(satAssignment.minAssignmentBits());
    }

    if (_bitvec.length > Long.SIZE) {
      throw new IllegalArgumentException(
          "Can't get a representative of a BDDInteger with more than Long.SIZE bits");
    }

    long value = 0;
    for (int i = 0; i < _bitvec.length; i++) {
      BDD bitBDD = _bitvec[_bitvec.length - i - 1];
      // a.diff(b) is a.and(b.not()). When the input is only a partial assignment (like satOne),
      // this biases towards lexicographically smaller solutions: set a 1 only if you can't set 0.
      if (!satAssignment.diffSat(bitBDD)) {
        value |= 1L << i;
      }
    }
    return value;
  }

  public Long satAssignmentToLong(BitSet bits) {
    checkState(
        _hasVariablesOnly,
        "satAssignmentToLong can only be called on a BDDInteger with hasVariablesOnly() true");

    if (_bitvec.length > Long.SIZE) {
      throw new IllegalArgumentException(
          "Can't get a representative of a BDDInteger with more than Long.SIZE bits");
    }

    long value = 0;
    for (int i = 0; i < _bitvec.length; i++) {
      BDD bitBDD = _bitvec[_bitvec.length - i - 1];
      if (bits.get(bitBDD.level())) {
        value |= 1L << i;
      }
    }
    return value;
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

  /** Create an integer and initialize it to a concrete value */
  public static BDDInteger makeFromValue(BDDFactory factory, int length, long value) {
    BDDInteger bdd = new BDDInteger(factory, length);
    bdd.setValue(value);
    bdd._hasVariablesOnly = false;
    return bdd;
  }

  /** Build a constraint that matches the set of IPs contained by the input {@link Prefix}. */
  public BDD toBDD(Prefix prefix) {
    return firstBitsEqual(prefix.getStartIp(), prefix.getPrefixLength());
  }

  /** Build a constraint that matches the input {@link Ip}. */
  public BDD toBDD(Ip ip) {
    return firstBitsEqual(ip, Prefix.MAX_PREFIX_LENGTH);
  }

  /** Build a constraint that matches the {@link Ip IPs} matched by the input {@link IpWildcard}. */
  public BDD toBDD(IpWildcard ipWildcard) {
    checkArgument(_bitvec.length >= Prefix.MAX_PREFIX_LENGTH);
    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcardMask();
    _trues.clear();
    _falses.clear();
    for (int i = Prefix.MAX_PREFIX_LENGTH - 1; i >= 0; i--) {
      boolean significant = !Ip.getBitAtPosition(wildcard, i);
      if (significant) {
        boolean bitValue = Ip.getBitAtPosition(ip, i);
        if (bitValue) {
          _trues.add(_bitvec[i]);
        } else {
          _falses.add(_bitvec[i]);
        }
      }
    }
    return _factory.andAll(_trues).diffWith(_factory.orAll(_falses));
  }

  /** Check if the first length bits match the BDDInteger representing the advertisement prefix. */
  private BDD firstBitsEqual(Ip ip, int length) {
    checkArgument(length <= _bitvec.length, "Not enough bits");
    long b = ip.asLong();
    _trues.clear();
    _falses.clear();
    for (int i = length - 1; i >= 0; i--) {
      boolean bitValue = Ip.getBitAtPosition(b, i);
      if (bitValue) {
        _trues.add(_bitvec[i]);
      } else {
        _falses.add(_bitvec[i]);
      }
    }
    return _factory.andAll(_trues).diffWith(_factory.orAll(_falses));
  }

  /*
   * Map an if-then-else over each bit in the bitvector
   */
  public BDDInteger ite(BDD b, BDDInteger other) {
    BDDInteger val = new BDDInteger(this);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = b.ite(_bitvec[i], other._bitvec[i]);
    }
    val._hasVariablesOnly = false;
    return val;
  }

  /*
   * Create a BDD representing the exact value
   */
  public BDD value(long val) {
    checkArgument(val >= 0, "value is negative");
    checkArgument(val <= _maxVal, "value %s is out of range [0, %s]", val, _maxVal);
    long currentVal = val;
    _trues.clear();
    _falses.clear();
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      if ((currentVal & 1) != 0) {
        _trues.add(_bitvec[i]);
      } else {
        _falses.add(_bitvec[i]);
      }
      currentVal >>= 1;
    }
    return _factory.andAll(_trues).diffWith(_factory.orAll(_falses));
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

  /*
   * Set this BDD to have an exact value
   */
  public void setValue(long val) {
    checkArgument(val >= 0, "Cannot set a negative value");
    checkArgument(val <= _maxVal, "value %s is out of range [0, %s]", val, _maxVal);
    long currentVal = val;
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      if ((currentVal & 1) != 0) {
        _bitvec[i] = _factory.one();
      } else {
        _bitvec[i] = _factory.zero();
      }
      currentVal >>= 1;
    }
    _hasVariablesOnly = false;
  }

  /*
   * Set this BDD to be equal to another BDD
   */
  public void setValue(BDDInteger other) {
    for (int i = 0; i < _bitvec.length; ++i) {
      _bitvec[i] = other._bitvec[i].id();
    }
    _hasVariablesOnly = other._hasVariablesOnly;
  }

  /*
   * Add two BDDs bitwise to create a new BDD
   */
  public BDDInteger add(BDDInteger other) {
    BDD[] as = _bitvec;
    BDD[] bs = other._bitvec;

    checkArgument(as.length > 0, "Cannot add BDDIntegers of length 0");
    checkArgument(as.length == bs.length, "Cannot add BDDIntegers of different length");

    BDD carry = _factory.zero();
    BDDInteger sum = new BDDInteger(_factory, as.length);
    BDD[] cs = sum._bitvec;
    for (int i = cs.length - 1; i > 0; --i) {
      cs[i] = as[i].xor(bs[i]).xor(carry);
      carry = as[i].and(bs[i]).or(carry.and(as[i].or(bs[i])));
    }
    cs[0] = as[0].xor(bs[0]).xor(carry);
    sum._hasVariablesOnly = false;
    return sum;
  }

  /*
   * Subtract one BDD from another bitwise to create a new BDD
   */
  public BDDInteger sub(BDDInteger var1) {
    if (_bitvec.length != var1._bitvec.length) {
      throw new BDDException();
    } else {
      BDD var3 = _factory.zero();
      BDDInteger var4 = new BDDInteger(_factory, _bitvec.length);
      for (int var5 = var4._bitvec.length - 1; var5 >= 0; --var5) {
        var4._bitvec[var5] = _bitvec[var5].xor(var1._bitvec[var5]);
        var4._bitvec[var5] = var4._bitvec[var5].xor(var3.id());
        BDD var6 = var1._bitvec[var5].or(var3);
        BDD var7 = _bitvec[var5].less(var6);
        var6.free();
        var6 = _bitvec[var5].and(var1._bitvec[var5]);
        var6 = var6.and(var3);
        var6 = var6.or(var7);
        var3 = var6;
      }
      var3.free();
      var4._hasVariablesOnly = false;
      return var4;
    }
  }

  /** Returns a {@link BDD} containing all the variables of this {@link BDDInteger}. */
  public @Nonnull BDD getVars() {
    checkState(
        _hasVariablesOnly,
        "getVars can only be called on a BDDInteger with hasVariablesOnly() true");
    if (_vars == null) {
      _vars = _factory.andAll(_bitvec);
    }
    return _vars;
  }

  /**
   * Returns a {@link BDD} containing the {@code n} high-order variables of this {@link BDDInteger}.
   */
  public @Nonnull BDD getMostSignificantVars(int n) {
    checkState(
        _hasVariablesOnly,
        "getMostSignificantVars can only be called on a BDDInteger with hasVariablesOnly() true");
    checkArgument(
        n <= _bitvec.length,
        "getMostSignificantVars(%s) called on a BDDInteger with only %s variables",
        n,
        _bitvec.length);
    if (n == _bitvec.length) {
      return getVars();
    }
    return _factory.andAll(Arrays.asList(_bitvec).subList(0, n));
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDInteger)) {
      return false;
    }
    BDDInteger other = (BDDInteger) o;
    // No need to check _factory, and _hasVariablesOnly is 1-1 with _bitvec.
    return Arrays.equals(_bitvec, other._bitvec);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_bitvec);
  }
}
