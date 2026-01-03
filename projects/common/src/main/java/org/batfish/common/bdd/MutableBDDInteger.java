package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class MutableBDDInteger extends BDDInteger {
  // Temporary ArrayLists used to optimize some internal computations.
  private transient List<BDD> _trues;
  private transient List<BDD> _falses;

  public MutableBDDInteger(BDDFactory factory, BDD[] bitvec) {
    super(factory, bitvec);
    initTransientFields();
  }

  public MutableBDDInteger(BDDFactory factory, int length) {
    this(factory, new BDD[length]);
  }

  public MutableBDDInteger(MutableBDDInteger other) {
    this(other._factory, other._bitvec.length);
    setValue(other);
  }

  @Serial
  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    initTransientFields();
  }

  private void initTransientFields() {
    _trues = new ArrayList<>(_bitvec.length);
    _falses = new ArrayList<>(_bitvec.length);
  }

  /**
   * Create an integer, and initialize its values as "don't care" This requires knowing the start
   * index variables the bitvector will use.
   */
  public static MutableBDDInteger makeFromIndex(
      BDDFactory factory, int length, int start, boolean reverse) {
    return new MutableBDDInteger(factory, bitvector(factory, length, start, reverse));
  }

  /** Create an integer and initialize it to a concrete value */
  public static MutableBDDInteger makeFromValue(BDDFactory factory, int length, long value) {
    MutableBDDInteger bdd = new MutableBDDInteger(factory, length);
    bdd.setValue(value);
    return bdd;
  }

  /** Set this BDD to have an exact value */
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
  }

  @Override
  public Optional<Long> getValueSatisfying(BDD bdd) {
    return bdd.isZero() ? Optional.empty() : Optional.of(satAssignmentToLong(bdd.satOne()));
  }

  @Override
  public long satAssignmentToLong(BDD satAssignment) {
    checkArgument(satAssignment.isAssignment(), "not a satisfying assignment");
    checkArgument(
        _bitvec.length <= 63, "Only BDDInteger of 63 or fewer bits can be converted to long");

    // we must "complete" the given partial assignment in order to properly get models in the face
    // of mutation, where this object represents a function of the original BDD variables. we could
    // use BDD::fullSatOne to do that, but if there are many BDD variables then that will create a
    // very large BDD, which can cause performance issues. instead we only explicitly treat as false
    // the variables that do not appear in the given SAT assignment but are part of the support of
    // this MutableBDDInteger.
    BDD fullSatAssignment = satAssignment.satOne(support(), false);

    long value = 0;
    for (int i = 0; i < _bitvec.length; i++) {
      BDD bitBDD = _bitvec[_bitvec.length - i - 1];
      if (fullSatAssignment.andSat(bitBDD)) {
        value |= 1L << i;
      }
    }
    return value;
  }

  /** Check if the first length bits match the BDDInteger representing the advertisement prefix. */
  @Override
  protected BDD firstBitsEqual(long value, int length) {
    checkArgument(length <= _bitvec.length, "Not enough bits");
    long startBDDCount = _factory.numOutstandingBDDs();
    checkState(_trues.isEmpty(), "Unexpected array state");
    checkState(_falses.isEmpty(), "Unexpected array state");
    long val = value >> (_bitvec.length - length);
    for (int i = length - 1; i >= 0; i--) {
      boolean bitValue = (val & 1) == 1;
      if (bitValue) {
        _trues.add(_bitvec[i]);
      } else {
        _falses.add(_bitvec[i]);
      }
      val >>= 1;
    }
    BDD result = _factory.andAll(_trues).diffWith(_factory.orAll(_falses));
    _trues.clear();
    _falses.clear();
    assertNoLeaks(startBDDCount, 1);
    return result;
  }

  @Override
  public BDD toBDD(IpWildcard ipWildcard) {
    checkArgument(_bitvec.length >= Prefix.MAX_PREFIX_LENGTH);
    long startBDDCount = _factory.numOutstandingBDDs();
    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcardMask();
    checkState(_trues.isEmpty(), "Unexpected array state");
    checkState(_falses.isEmpty(), "Unexpected array state");
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
    BDD result = _factory.andAll(_trues).diffWith(_factory.orAll(_falses));
    _trues.clear();
    _falses.clear();
    assertNoLeaks(startBDDCount, 1);
    return result;
  }

  /*
   * Add two BDDs bitwise to create a new BDD
   */
  public MutableBDDInteger add(BDDInteger other) {
    return addImpl(other, false);
  }

  /*
   * Add two BDDs bitwise to create a new BDD. Clips to MAX_VALUE in case of overflow.
   */
  public MutableBDDInteger addClipping(BDDInteger other) {
    return addImpl(other, true);
  }

  private MutableBDDInteger addImpl(BDDInteger other, boolean clipping) {
    BDD[] as = _bitvec;
    BDD[] bs = other._bitvec;

    checkArgument(as.length > 0, "Cannot add BDDIntegers of length 0");
    checkArgument(as.length == bs.length, "Cannot add BDDIntegers of different length");

    long startBDDCount = _factory.numOutstandingBDDs();
    MutableBDDInteger sum = new MutableBDDInteger(_factory, as.length);
    BDD[] cs = sum._bitvec;
    BDD carry = _factory.zero();
    for (int i = cs.length - 1; i >= 0; --i) {
      cs[i] = as[i].xor(bs[i]).xorEq(carry);
      carry = as[i].and(bs[i]).orWith(carry.andWith(as[i].or(bs[i])));
    }
    MutableBDDInteger result;
    if (clipping) {
      MutableBDDInteger maxValue = makeFromValue(_factory, _bitvec.length, _maxVal);
      result = sum.ite(carry.notEq(), maxValue);
      maxValue.free();
      sum.free();
      carry.free();
    } else {
      result = sum;
      carry.free();
    }
    assertNoLeaks(startBDDCount, _bitvec.length);
    return result;
  }

  private void assertNoLeaks(long startBDDs, long incremental) {
    assert _factory.numOutstandingBDDs() == startBDDs + incremental
        : String.format(
            "Expected to create %d new BDDs, actually created %d",
            incremental, _factory.numOutstandingBDDs() - startBDDs);
  }

  /**
   * Free all the component {@link BDD BDDs} in this {@link BDDInteger}. This instance is no longer
   * safe to use unless re-initialized via, e.g., {@link #setValue(long)}.
   */
  public void free() {
    for (int i = 0; i < _bitvec.length; i++) {
      _bitvec[i].free();
      _bitvec[i] = null;
    }
  }

  /*
   * Map an if-then-else over each bit in the bitvector
   */
  public MutableBDDInteger ite(BDD b, MutableBDDInteger other) {
    long startBDDCount = _factory.numOutstandingBDDs();
    MutableBDDInteger val = new MutableBDDInteger(_factory, _bitvec.length);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = b.ite(_bitvec[i], other._bitvec[i]);
    }
    assertNoLeaks(startBDDCount, _bitvec.length);
    return val;
  }

  /**
   * @param pred a predicate
   * @return the same bitvector but restricted by pred.
   */
  public MutableBDDInteger and(BDD pred) {
    long startBDDCount = _factory.numOutstandingBDDs();
    MutableBDDInteger val = new MutableBDDInteger(_factory, _bitvec.length);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = pred.and(_bitvec[i]);
    }
    assertNoLeaks(startBDDCount, _bitvec.length);
    return val;
  }

  /**
   * Produces a BDD whose models represent all possible differences between the two BDDIntegers --
   * valuations of the BDD variables that cause the two BDDIntegers to have different values. The
   * two BDDIntegers are assumed to have the same length.
   *
   * @param other the second BDDInteger
   * @return a predicate represented as a BDD
   */
  public BDD allDifferences(BDDInteger other) {
    long startBDDCount = _factory.numOutstandingBDDs();
    assert _bitvec.length == other._bitvec.length;
    BDD result =
        _factory.orAllAndFree(
            IntStream.range(0, _bitvec.length)
                .mapToObj(i -> _bitvec[i].xor(other._bitvec[i]))
                .collect(ImmutableList.toImmutableList()));
    assertNoLeaks(startBDDCount, 1);
    return result;
  }

  /**
   * Augments a given pairing to pair corresponding BDDs from the given MutableBDDInteger with this
   * one. The BDDs in the given MutableBDDInteger should all be variables.
   *
   * @param other the MutableBDDInteger of variables
   * @param pairing the existing pairing
   */
  public void augmentPairing(MutableBDDInteger other, BDDPairing pairing) {
    pairing.set(other._bitvec, _bitvec);
  }

  /**
   * Produces a BDD that represents the support (i.e., the set of BDD variables) of this BDD
   * integer.
   */
  public BDD support() {
    return _factory.andAllAndFree(
        Arrays.stream(_bitvec).map(BDD::support).collect(Collectors.toSet()));
  }

  /*
   * Set this BDD to be equal to another BDD
   */
  public void setValue(MutableBDDInteger other) {
    for (int i = 0; i < _bitvec.length; ++i) {
      _bitvec[i] = other._bitvec[i].id();
    }
  }

  /*
   * Subtract one BDD from another bitwise to create a new BDD
   */
  public MutableBDDInteger sub(BDDInteger other) {
    return subImpl(other, false);
  }

  /*
   * Subtract one BDD from another bitwise to create a new BDD. Clips to 0 in case of underflow.
   */
  public MutableBDDInteger subClipping(BDDInteger other) {
    return subImpl(other, true);
  }

  private MutableBDDInteger subImpl(BDDInteger other, boolean clipping) {
    BDD[] as = _bitvec;
    BDD[] bs = other._bitvec;

    checkArgument(as.length == bs.length, "Cannot subtract BDDIntegers of different length");
    checkArgument(as.length > 0, "Cannot subtract BDDIntegers of length 0");

    long startBDDCount = _factory.numOutstandingBDDs();
    MutableBDDInteger diff = new MutableBDDInteger(_factory, as.length);
    BDD[] cs = diff._bitvec;
    BDD borrow = _factory.zero();
    for (int i = cs.length - 1; i >= 0; --i) {
      cs[i] = as[i].xor(bs[i]).xorEq(borrow);
      BDD var7 = bs[i].or(borrow).diffEq(as[i]);
      borrow = as[i].and(bs[i]).andWith(borrow).orWith(var7);
    }
    MutableBDDInteger result;
    if (clipping) {
      MutableBDDInteger zero = makeFromValue(_factory, _bitvec.length, 0);
      result = diff.ite(borrow.notEq(), zero);
      zero.free();
      diff.free();
      borrow.free();
    } else {
      result = diff;
      borrow.free();
    }
    assertNoLeaks(startBDDCount, _bitvec.length);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MutableBDDInteger)) {
      return false;
    }
    MutableBDDInteger other = (MutableBDDInteger) o;
    // No need to check _factory
    return Arrays.equals(_bitvec, other._bitvec);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_bitvec);
  }
}
