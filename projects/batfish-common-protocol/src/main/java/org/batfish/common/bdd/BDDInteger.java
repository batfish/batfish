package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public abstract class BDDInteger {
  protected final BDDFactory _factory;
  protected final BDD[] _bitvec;
  protected final long _maxVal;

  // Temporary ArrayLists used to optimize some internal computations.
  private final List<BDD> _trues;
  private final List<BDD> _falses;

  protected BDDInteger(BDDFactory factory, BDD[] bitvec) {
    checkArgument(bitvec.length < 64, "Only lengths up to 63 are supported");
    _factory = factory;
    _bitvec = bitvec;
    _maxVal = 0xFFFF_FFFF_FFFF_FFFFL >>> (64 - bitvec.length);
    _trues = new ArrayList<>(bitvec.length);
    _falses = new ArrayList<>(bitvec.length);
  }

  /** Returns the number of bits in this {@link BDDInteger}. */
  public int size() {
    return _bitvec.length;
  }

  /** Find a representative value of the represented integer that satisfies a given constraint. */
  public abstract Optional<Long> getValueSatisfying(BDD bdd);

  public abstract Long satAssignmentToLong(BDD satAssignment);

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

  public BDDFactory getFactory() {
    return _factory;
  }
}
