package org.batfish.symbolic.abstraction;

import java.util.Arrays;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;

public class BDDInteger {

  private BDD[] _bitvec;


  /*
   * Create an integer, and initialize its values as "don't care"
   * This requires knowing the start index variables the bitvector
   * will use.
   */
  public static BDDInteger makeFromIndex(int length, int start) {
    BDDInteger bdd = new BDDInteger(length);
    for (int i = 0; i < length; i++) {
      bdd._bitvec[i] = BDDRecord.factory.ithVar(start + i);
    }
    return bdd;
  }

  /*
 * Create an integer and initialize it to a concrete value
 */
  public static BDDInteger makeFromValue(int length, long value) {
    BDDInteger bdd = new BDDInteger(length);
    bdd.setValue(value);
    return bdd;
  }

  /*
   * Create an integer, but don't initialize its bit values
   */
  private BDDInteger(int length) {
    _bitvec = new BDD[length];
  }

  public BDDInteger(BDDInteger other) {
    _bitvec = Arrays.copyOf(other._bitvec, other._bitvec.length);
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
  public BDD value(int val) {
    BDDFactory var2 = BDDRecord.factory;
    BDD bdd = var2.one();
    for (int i = this._bitvec.length - 1; i >= 0; i--) {
      BDD b = this._bitvec[i];
      if ((val & 1) != 0) {
        bdd = bdd.and(b);
      } else {
        bdd = bdd.and(b.not());
      }
      val >>= 1;
    }
    return bdd;
  }

  /*
   * Set this BDD to have an exact value
   */
  public void setValue(long val) {
    BDDFactory factory = BDDRecord.factory;
    for (int i = this._bitvec.length - 1; i >= 0; i--) {
      if ((val & 1) != 0) {
        this._bitvec[i] = factory.one();
      } else {
        this._bitvec[i] = factory.zero();
      }
      val >>= 1;
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
      BDDFactory var2 = BDDRecord.factory;
      BDD var3 = var2.zero();
      BDDInteger var4 = new BDDInteger(this._bitvec.length);
      for (int var5 = 0; var5 < var4._bitvec.length; ++var5) {
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
      BDDFactory var2 = BDDRecord.factory;
      BDD var3 = var2.zero();
      BDDInteger var4 = new BDDInteger(this._bitvec.length);
      for (int var5 = 0; var5 < var4._bitvec.length; ++var5) {
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

  @Override public boolean equals(Object o) {
    if (!(o instanceof BDDInteger)) {
      return false;
    }
    BDDInteger other = (BDDInteger) o;
    return Arrays.equals(_bitvec, other._bitvec);
  }

  @Override public int hashCode() {
    return Arrays.hashCode(_bitvec);
  }
}
