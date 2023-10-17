package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
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

  public static MutableBDDInteger makeMaxValue(BDDFactory factory, int length) {
    MutableBDDInteger bdd = new MutableBDDInteger(factory, length);
    bdd.setValue(bdd._maxVal);
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
    BDD fullSatAssignment =
        satAssignment.satOne(
            satAssignment
                .getFactory()
                .andAll(Arrays.stream(_bitvec).map(BDD::support).collect(Collectors.toSet())),
            false);

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
    _trues.clear();
    _falses.clear();
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
    return _factory.andAll(_trues).diffWith(_factory.orAll(_falses));
  }

  @Override
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

  /*
   * Add two BDDs bitwise to create a new BDD
   */
  public MutableBDDInteger add(BDDInteger other) {
    BDD[] as = _bitvec;
    BDD[] bs = other._bitvec;

    checkArgument(as.length > 0, "Cannot add BDDIntegers of length 0");
    checkArgument(as.length == bs.length, "Cannot add BDDIntegers of different length");

    BDD carry = _factory.zero();
    MutableBDDInteger sum = new MutableBDDInteger(_factory, as.length);
    BDD[] cs = sum._bitvec;
    for (int i = cs.length - 1; i > 0; --i) {
      cs[i] = as[i].xor(bs[i]).xor(carry);
      carry = as[i].and(bs[i]).or(carry.and(as[i].or(bs[i])));
    }
    cs[0] = as[0].xor(bs[0]).xor(carry);
    return sum;
  }

  /*
   * Add two BDDs bitwise to create a new BDD. Clips to MAX_VALUE in case of overflow.
   */
  public MutableBDDInteger addClipping(MutableBDDInteger other) {
    BDD[] as = _bitvec;
    BDD[] bs = other._bitvec;

    checkArgument(as.length > 0, "Cannot add BDDIntegers of length 0");
    checkArgument(as.length == bs.length, "Cannot add BDDIntegers of different length");

    BDD carry = _factory.zero();
    MutableBDDInteger sum = new MutableBDDInteger(_factory, as.length);
    BDD[] cs = sum._bitvec;
    for (int i = cs.length - 1; i >= 0; --i) {
      cs[i] = as[i].xor(bs[i]).xor(carry);
      carry = as[i].and(bs[i]).or(carry.and(as[i].or(bs[i])));
    }
    return sum.ite(carry.not(), makeMaxValue(_factory, _bitvec.length));
  }

  /*
   * Map an if-then-else over each bit in the bitvector
   */
  public MutableBDDInteger ite(BDD b, MutableBDDInteger other) {
    MutableBDDInteger val = new MutableBDDInteger(this);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = b.ite(_bitvec[i], other._bitvec[i]);
    }
    return val;
  }

  /**
   * @param pred a predicate
   * @return the same bitvector but restricted by pred.
   */
  public MutableBDDInteger and(BDD pred) {
    MutableBDDInteger val = new MutableBDDInteger(this);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = pred.and(_bitvec[i]);
    }
    return val;
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
  public MutableBDDInteger sub(MutableBDDInteger var1) {
    checkArgument(
        _bitvec.length == var1._bitvec.length, "Input variable must have equal bitvector length");
    BDD var3 = _factory.zero();
    MutableBDDInteger var4 = new MutableBDDInteger(_factory, _bitvec.length);
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
    return var4;
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
