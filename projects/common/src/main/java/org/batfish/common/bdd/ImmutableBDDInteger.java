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
  private BDD[] _negBitvec;

  public ImmutableBDDInteger(BDDFactory factory, BDD[] bitvec) {
    super(factory, bitvec);

    _negBitvec = new BDD[bitvec.length];
    for (int i = 0; i < _negBitvec.length; i++) {
      _negBitvec[i] = bitvec[i].not();
    }
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
  protected BDD firstBitsEqual(long value, int length) {
    checkArgument(length <= _bitvec.length, "Not enough bits");
    long val = value >> (_bitvec.length - length);
    BDD[] literals = new BDD[length];
    for (int i = length - 1; i >= 0; i--) {
      if ((val & 1) == 1) {
        literals[i] = _bitvec[i];
      } else {
        literals[i] = _negBitvec[i];
      }
      val >>= 1;
    }
    return _factory.andLiterals(literals);
  }

  @Override
  public BDD toBDD(IpWildcard ipWildcard) {
    checkArgument(_bitvec.length >= Prefix.MAX_PREFIX_LENGTH);
    if (ipWildcard.isPrefix()) {
      return firstBitsEqual(
          ipWildcard.getIp().asLong(),
          Integer.numberOfLeadingZeros((int) ipWildcard.getWildcardMask()));
    }

    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcardMask();

    int sigBits = Prefix.MAX_PREFIX_LENGTH - Long.bitCount(wildcard);
    assert sigBits < Prefix.MAX_PREFIX_LENGTH && sigBits > 0; // can't == 0, since it's not a prefix
    BDD[] literals = new BDD[sigBits];

    int lit = literals.length - 1; // populating literals back-to-front
    for (int i = Prefix.MAX_PREFIX_LENGTH - 1; i >= 0; i--) {
      boolean significant = (wildcard & 1) == 0;
      if (significant) {
        boolean bitValue = (ip & 1) == 1;
        if (bitValue) {
          literals[lit] = _bitvec[i];
        } else {
          literals[lit] = _negBitvec[i];
        }
        lit--;
      }
      ip >>= 1;
      wildcard >>= 1;
    }
    assert lit == -1; // we populated every element of the array
    return _factory.andLiterals(literals);
  }

  public int satAssignmentToInt(BitSet bits) {
    checkArgument(
        _bitvec.length <= 31, "Only BDDInteger of 31 or fewer bits can be converted to int");
    return (int) satAssignmentToLong(bits);
  }

  public long satAssignmentToLong(BitSet bits) {
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
