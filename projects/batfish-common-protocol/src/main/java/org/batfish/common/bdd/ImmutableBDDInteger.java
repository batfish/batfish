package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public class ImmutableBDDInteger extends BDDInteger implements Serializable {
  private BDD _vars;

  public ImmutableBDDInteger(BDDFactory factory, BDD[] bitvec) {
    super(factory, bitvec);
  }

  /**
   * Create an integer, and initialize its values as "don't care" This requires knowing the start
   * index variables the bitvector will use.
   */
  public static ImmutableBDDInteger makeFromIndex(BDDFactory factory, int length, int start) {
    return new ImmutableBDDInteger(factory, bitvector(factory, length, start, false));
  }

  @Override
  public Optional<Long> getValueSatisfying(BDD bdd) {
    return bdd.isZero()
        ? Optional.empty()
        : Optional.of(satAssignmentToLong(bdd.minAssignmentBits()));
  }

  /** Returns a {@link BDD} containing all the variables of this {@link BDDInteger}. */
  public @Nonnull BDD getVars() {
    if (_vars == null) {
      _vars = value(_maxVal);
    }
    return _vars;
  }

  @Override
  public long satAssignmentToLong(BDD satAssignment) {
    checkArgument(satAssignment.isAssignment(), "not a satisfying assignment");
    return satAssignmentToLong(satAssignment.minAssignmentBits());
  }

  @Override
  protected BDD firstBitsEqual(long val, int length) {
    checkArgument(length <= _bitvec.length, "Not enough bits");
    BDD ret = _factory.one();
    val >>= _bitvec.length - length;
    for (int i = length - 1; i >= 0; i--) {
      if ((val & 1) == 1) {
        ret.andEq(_bitvec[i]);
      } else {
        ret.diffEq(_bitvec[i]);
      }
      val >>= 1;
    }
    return ret;
  }

  @Override
  public BDD toBDD(IpWildcard ipWildcard) {
    checkArgument(_bitvec.length >= Prefix.MAX_PREFIX_LENGTH);
    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcardMask();
    BDD ret = _factory.one();
    for (int i = Prefix.MAX_PREFIX_LENGTH - 1; i >= 0; i--) {
      boolean significant = (wildcard & 1) == 0;
      if (significant) {
        boolean bitValue = (ip & 1) == 1;
        if (bitValue) {
          ret.andEq(_bitvec[i]);
        } else {
          ret.diffEq(_bitvec[i]);
        }
      }
      ip >>= 1;
      wildcard >>= 1;
    }
    return ret;
  }

  public int satAssignmentToInt(BitSet bits) {
    checkArgument(
        _bitvec.length <= 31, "Only BDDInteger of 31 or fewer bits can be converted to int");
    return satAssignmentToLong(bits).intValue();
  }

  public Long satAssignmentToLong(BitSet bits) {
    long value = 0;
    for (int i = 0; i < _bitvec.length; i++) {
      BDD bitBDD = _bitvec[_bitvec.length - i - 1];
      if (bits.get(bitBDD.level())) {
        value |= 1L << i;
      }
    }
    return value;
  }
}
