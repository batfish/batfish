package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;

public class BDDInteger {

  private BDDFactory _factory;

  private BDD[] _bitvec;

  /*
   * Create an integer, but don't initialize its bit values
   */
  private BDDInteger(BDDFactory factory, int length) {
    _factory = factory;
    _bitvec = new BDD[length];
  }

  public BDDInteger(BDDInteger other) {
    _factory = other._factory;
    _bitvec = new BDD[other._bitvec.length];
    for (int i = 0; i < _bitvec.length; i++) {
      _bitvec[i] = other._bitvec[i].id();
    }
  }

  /*
   * Create an integer, and initialize its values as "don't care"
   * This requires knowing the start index variables the bitvector
   * will use.
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
    return bdd;
  }

  /** Find a representative value of the represented integer that satisfies a given constraint. */
  public Optional<Long> getValueSatisfying(BDD bdd) {
    BDD satAssignment = bdd.fullSatOne();
    return satAssignment.isZero()
        ? Optional.empty()
        : Optional.of(satAssignmentToLong(satAssignment));
  }

  /** @param satAssignment a satisfying assignment (i.e. produced by fullSat, allSat, etc) */
  public Long satAssignmentToLong(BDD satAssignment) {
    if (_bitvec.length > Long.SIZE) {
      throw new IllegalArgumentException(
          "Can't get a representative of a BDDInteger with more than Long.SIZE bits");
    }

    long value = 0;
    for (int i = 0; i < _bitvec.length; i++) {
      BDD bitBDD = _bitvec[_bitvec.length - i - 1];
      if (!satAssignment.and(bitBDD).isZero()) {
        value |= ((long) 1) << i;
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
      BDD satAssignment = pred.fullSatOne();
      if (satAssignment.isZero()) {
        break;
      }

      Long val = satAssignmentToLong(satAssignment);
      values.add(val);
      pred = pred.and(value(val).not());
      num++;
    }
    return values.build();
  }

  /*
   * Create an integer and initialize it to a concrete value
   */
  public static BDDInteger makeFromValue(BDDFactory factory, int length, long value) {
    BDDInteger bdd = new BDDInteger(factory, length);
    bdd.setValue(value);
    return bdd;
  }

  /*
   * Map an if-then-else over each bit in the bitvector
   */
  public BDDInteger ite(BDD b, BDDInteger other) {
    BDDInteger val = new BDDInteger(this);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = b.ite(_bitvec[i], other._bitvec[i]);
    }
    return val;
  }

  /*
   * Create a BDD representing the exact value
   */
  public BDD value(long val) {
    long currentVal = val;
    BDD bdd = _factory.one();
    for (int i = this._bitvec.length - 1; i >= 0; i--) {
      BDD b = this._bitvec[i];
      if ((currentVal & 1) != 0) {
        bdd = bdd.and(b);
      } else {
        bdd = bdd.and(b.not());
      }
      currentVal >>= 1;
    }
    return bdd;
  }

  /*
   * Less than or equal to on integers
   */
  public BDD leq(long val) {
    long currentVal = val;
    BDD[] eq = new BDD[_bitvec.length];
    BDD[] less = new BDD[_bitvec.length];
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      if ((currentVal & 1) != 0) {
        eq[i] = _bitvec[i];
        less[i] = _bitvec[i].not();
      } else {
        eq[i] = _bitvec[i].not();
        less[i] = _factory.zero();
      }
      currentVal >>= 1;
    }
    BDD acc = _factory.one();
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      acc = less[i].or(eq[i].and(acc));
    }
    return acc;
  }

  /*
   * Less than or equal to on integers
   */
  public BDD geq(long val) {
    long currentVal = val;
    BDD[] eq = new BDD[_bitvec.length];
    BDD[] greater = new BDD[_bitvec.length];
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      if ((currentVal & 1) != 0) {
        eq[i] = _bitvec[i];
        greater[i] = _factory.zero();
      } else {
        eq[i] = _bitvec[i].not();
        greater[i] = _bitvec[i];
      }
      currentVal >>= 1;
    }
    BDD acc = _factory.one();
    for (int i = _bitvec.length - 1; i >= 0; i--) {
      acc = greater[i].or(eq[i].and(acc));
    }
    return acc;
  }

  /*
   * Set this BDD to have an exact value
   */
  public void setValue(long val) {
    long currentVal = val;
    for (int i = this._bitvec.length - 1; i >= 0; i--) {
      if ((currentVal & 1) != 0) {
        this._bitvec[i] = _factory.one();
      } else {
        this._bitvec[i] = _factory.zero();
      }
      currentVal >>= 1;
    }
  }

  /*
   * Set this BDD to be equal to another BDD
   */
  public void setValue(BDDInteger other) {
    for (int i = 0; i < this._bitvec.length; ++i) {
      this._bitvec[i] = other._bitvec[i].id();
    }
  }

  /*
   * Add two BDDs bitwise to create a new BDD
   */
  public BDDInteger add(BDDInteger var1) {
    if (this._bitvec.length != var1._bitvec.length) {
      throw new BDDException();
    } else {
      BDD var3 = _factory.zero();
      BDDInteger var4 = new BDDInteger(_factory, this._bitvec.length);
      for (int var5 = var4._bitvec.length - 1; var5 >= 0; --var5) {
        var4._bitvec[var5] = this._bitvec[var5].xor(var1._bitvec[var5]);
        var4._bitvec[var5] = var4._bitvec[var5].xor(var3.id());
        BDD var6 = this._bitvec[var5].or(var1._bitvec[var5]);
        var6 = var6.and(var3);
        BDD var7 = this._bitvec[var5].and(var1._bitvec[var5]);
        var7 = var7.or(var6);
        var3 = var7;
      }
      var3.free();
      return var4;
    }
  }

  /*
   * Subtract one BDD from another bitwise to create a new BDD
   */
  public BDDInteger sub(BDDInteger var1) {
    if (this._bitvec.length != var1._bitvec.length) {
      throw new BDDException();
    } else {
      BDD var3 = _factory.zero();
      BDDInteger var4 = new BDDInteger(_factory, this._bitvec.length);
      for (int var5 = var4._bitvec.length - 1; var5 >= 0; --var5) {
        var4._bitvec[var5] = this._bitvec[var5].xor(var1._bitvec[var5]);
        var4._bitvec[var5] = var4._bitvec[var5].xor(var3.id());
        BDD var6 = var1._bitvec[var5].or(var3);
        BDD var7 = this._bitvec[var5].apply(var6, BDDFactory.less);
        var6.free();
        var6 = this._bitvec[var5].and(var1._bitvec[var5]);
        var6 = var6.and(var3);
        var6 = var6.or(var7);
        var3 = var6;
      }
      var3.free();
      return var4;
    }
  }

  public BDD[] getBitvec() {
    return _bitvec;
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
    return Arrays.equals(_bitvec, other._bitvec);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_bitvec);
  }
}
