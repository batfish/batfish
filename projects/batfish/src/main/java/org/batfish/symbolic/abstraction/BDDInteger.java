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

  public BDDInteger ite(BDD b, BDDInteger other) {
    BDDInteger val = new BDDInteger(this);
    for (int i = 0; i < _bitvec.length; i++) {
      val._bitvec[i] = b.ite(_bitvec[i], other._bitvec[i]);
    }
    return val;
  }

  public BDD value(int val) {
    BDDFactory var2 = BDDRecord.factory;
    BDD bdd = var2.one();
    for (BDD b : this._bitvec) {
      if ((val & 1) != 0) {
        bdd.andWith(b);
      } else {
        bdd.andWith(b.not());
      }
      val >>= 1;
    }
    return bdd;
  }


  public void setValue(long val) {
    BDDFactory var2 = BDDRecord.factory;
    for (int var3 = 0; var3 < this._bitvec.length; ++var3) {
      if ((val & 1) != 0) {
        this._bitvec[var3] = var2.one();
      } else {
        this._bitvec[var3] = var2.zero();
      }
      val >>= 1;
    }
  }

  public void setValue(BDDInteger other) {
    for (int i = 0; i < this._bitvec.length; ++i) {
      this._bitvec[i] = other._bitvec[i].id();
    }
  }

  public BDDInteger add(BDDInteger var1) {
    if (this._bitvec.length != var1._bitvec.length) {
      throw new BDDException();
    } else {
      BDDFactory var2 = BDDRecord.factory;
      BDD var3 = var2.zero();
      BDDInteger var4 = new BDDInteger(this._bitvec.length);
      for (int var5 = 0; var5 < var4._bitvec.length; ++var5) {
        var4._bitvec[var5] = this._bitvec[var5].xor(var1._bitvec[var5]);
        var4._bitvec[var5].xorWith(var3.id());
        BDD var6 = this._bitvec[var5].or(var1._bitvec[var5]);
        var6.andWith(var3);
        BDD var7 = this._bitvec[var5].and(var1._bitvec[var5]);
        var7.orWith(var6);
        var3 = var7;
      }
      var3.free();
      return var4;
    }
  }

  public BDDInteger sub(BDDInteger var1) {
    if (this._bitvec.length != var1._bitvec.length) {
      throw new BDDException();
    } else {
      BDDFactory var2 = BDDRecord.factory;
      BDD var3 = var2.zero();
      BDDInteger var4 = new BDDInteger(this._bitvec.length);
      for (int var5 = 0; var5 < var4._bitvec.length; ++var5) {
        var4._bitvec[var5] = this._bitvec[var5].xor(var1._bitvec[var5]);
        var4._bitvec[var5].xorWith(var3.id());
        BDD var6 = var1._bitvec[var5].or(var3);
        BDD var7 = this._bitvec[var5].apply(var6, BDDFactory.less);
        var6.free();
        var6 = this._bitvec[var5].and(var1._bitvec[var5]);
        var6.andWith(var3);
        var6.orWith(var7);
        var3 = var6;
      }
      var3.free();
      return var4;
    }
  }

  public BDD[] getBitvec() {
    return _bitvec;
  }
}
