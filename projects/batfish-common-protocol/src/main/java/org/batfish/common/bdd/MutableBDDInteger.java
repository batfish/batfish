package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import java.util.Arrays;
import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;

public final class MutableBDDInteger extends BDDInteger {
  public MutableBDDInteger(BDDFactory factory, BDD[] bitvec) {
    super(factory, bitvec);
  }

  public MutableBDDInteger(BDDFactory factory, int length) {
    this(factory, new BDD[length]);
  }

  public MutableBDDInteger(MutableBDDInteger other) {
    this(other._factory, other._bitvec.length);
    setValue(other);
  }
  /*
   * Create an integer, and initialize its values as "don't care"
   * This requires knowing the start index variables the bitvector
   * will use.
   */
  public static MutableBDDInteger makeFromIndex(
      BDDFactory factory, int length, int start, boolean reverse) {
    return new MutableBDDInteger(factory, bitvector(factory, length, start, reverse));
  }

  /*
   * Create an integer and initialize it to a concrete value
   */
  public static MutableBDDInteger makeFromValue(BDDFactory factory, int length, long value) {
    MutableBDDInteger bdd = new MutableBDDInteger(factory, length);
    bdd.setValue(value);
    return bdd;
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
  }

  @Override
  public Optional<Long> getValueSatisfying(BDD bdd) {
    return bdd.isZero() ? Optional.empty() : Optional.of(satAssignmentToLong(bdd.satOne()));
  }

  @Override
  public Long satAssignmentToLong(BDD satAssignment) {
    checkArgument(satAssignment.isAssignment(), "not a satisfying assignment");
    checkArgument(
        _bitvec.length <= Long.SIZE,
        "Can't get a representative of a BDDInteger with more than Long.SIZE bits");

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

  /*
   * Add two BDDs bitwise to create a new BDD
   */
  public BDDInteger add(BDDInteger other) {
    BDD[] as = _bitvec;
    BDD[] bs = other._bitvec;

    checkArgument(as.length > 0, "Cannot add BDDIntegers of length 0");
    checkArgument(as.length == bs.length, "Cannot add BDDIntegers of different length");

    BDD carry = _factory.zero();
    BDDInteger sum = new MutableBDDInteger(_factory, as.length);
    BDD[] cs = sum._bitvec;
    for (int i = cs.length - 1; i > 0; --i) {
      cs[i] = as[i].xor(bs[i]).xor(carry);
      carry = as[i].and(bs[i]).or(carry.and(as[i].or(bs[i])));
    }
    cs[0] = as[0].xor(bs[0]).xor(carry);
    return sum;
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
    if (_bitvec.length != var1._bitvec.length) {
      throw new BDDException();
    } else {
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
