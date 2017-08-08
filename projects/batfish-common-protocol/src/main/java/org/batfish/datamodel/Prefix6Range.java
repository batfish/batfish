package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigInteger;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;

public class Prefix6Range extends Pair<Prefix6, SubRange> {

  /** */
  private static final long serialVersionUID = 1L;

  public static Prefix6Range fromPrefix6(Prefix6 prefix6) {
    int prefix6Length = prefix6.getPrefixLength();
    return new Prefix6Range(prefix6, new SubRange(prefix6Length, prefix6Length));
  }

  private static SubRange lengthRangeFromStr(String str) {
    Prefix6 prefix6;
    String[] mainParts = str.split(";");
    int numMainParts = mainParts.length;
    if (numMainParts < 1 || numMainParts > 2) {
      throw new BatfishException("Invalid Prefix6Range string: '" + str + "'");
    } else {
      prefix6 = new Prefix6(mainParts[0]);
      if (mainParts.length == 1) {
        int prefix6Length = prefix6.getPrefixLength();
        return new SubRange(prefix6Length, prefix6Length);
      } else {
        return new SubRange(mainParts[1]);
      }
    }
  }

  private static Prefix6 prefix6FromStr(String str) {
    Prefix6 prefix6;
    String[] mainParts = str.split(";");
    int numMainParts = mainParts.length;
    if (numMainParts < 1 || numMainParts > 2) {
      throw new BatfishException("Invalid Prefix6Range string: '" + str + "'");
    } else {
      prefix6 = new Prefix6(mainParts[0]);
      return prefix6;
    }
  }

  public Prefix6Range(Prefix6 prefix6, SubRange lengthRange) {
    super(prefix6, lengthRange);
  }

  @JsonCreator
  public Prefix6Range(String str) {
    super(prefix6FromStr(str), lengthRangeFromStr(str));
  }

  public SubRange getLengthRange() {
    return _second;
  }

  public Prefix6 getPrefix6() {
    return _first;
  }

  public boolean includesPrefix6(Prefix6 argPrefix6) {
    Prefix6 prefix6 = getPrefix6();
    SubRange lengthRange = getLengthRange();
    int prefixLength = prefix6.getPrefixLength();
    int minPrefixLength = lengthRange.getStart();
    int maxPrefixLength = lengthRange.getEnd();
    int argPrefixLength = argPrefix6.getPrefixLength();
    if (minPrefixLength > argPrefixLength || maxPrefixLength < argPrefixLength) {
      return false;
    }
    BigInteger maskedPrefixAsBigInteger =
        prefix6.getAddress().getNetworkAddress(prefixLength).asBigInteger();
    BigInteger argMaskedPrefixAsBigInteger =
        argPrefix6.getAddress().getNetworkAddress(prefixLength).asBigInteger();
    return maskedPrefixAsBigInteger.equals(argMaskedPrefixAsBigInteger);
  }

  public boolean includesPrefix6Range(Prefix6Range argPrefixRange) {
    Prefix6 prefix6 = getPrefix6();
    SubRange lengthRange = getLengthRange();
    int prefixLength = prefix6.getPrefixLength();
    BigInteger maskedPrefixAsBigInteger =
        prefix6.getAddress().getNetworkAddress(prefixLength).asBigInteger();
    Prefix6 argPrefix = argPrefixRange.getPrefix6();
    SubRange argLengthRange = argPrefixRange.getLengthRange();
    BigInteger argMaskedPrefixAsBigInteger =
        argPrefix.getAddress().getNetworkAddress(prefixLength).asBigInteger();
    return maskedPrefixAsBigInteger.equals(argMaskedPrefixAsBigInteger)
        && lengthRange.getStart() <= argLengthRange.getStart()
        && lengthRange.getEnd() >= argLengthRange.getEnd();
  }

  @Override
  @JsonValue
  public String toString() {
    int prefixLength = _first.getPrefixLength();
    int low = _second.getStart();
    int high = _second.getEnd();
    if (prefixLength == low && prefixLength == high) {
      return _first.toString();
    } else {
      return _first + ";" + low + "-" + high;
    }
  }
}
