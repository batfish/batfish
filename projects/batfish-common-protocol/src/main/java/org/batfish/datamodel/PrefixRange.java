package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;

public class PrefixRange extends Pair<Prefix, SubRange> {

  /** */
  private static final long serialVersionUID = 1L;

  public static PrefixRange fromPrefix(Prefix prefix) {
    int prefixLength = prefix.getPrefixLength();
    return new PrefixRange(prefix, new SubRange(prefixLength, prefixLength));
  }

  private static SubRange lengthRangeFromStr(String str) {
    Prefix prefix;
    String[] mainParts = str.split(":");
    int numMainParts = mainParts.length;
    if (numMainParts < 1 || numMainParts > 2) {
      throw new BatfishException("Invalid PrefixRange string: '" + str + "'");
    } else {
      prefix = new Prefix(mainParts[0]);
      if (mainParts.length == 1) {
        int prefixLength = prefix.getPrefixLength();
        return new SubRange(prefixLength, prefixLength);
      } else {
        return new SubRange(mainParts[1]);
      }
    }
  }

  private static Prefix prefixFromStr(String str) {
    Prefix prefix;
    String[] mainParts = str.split(":");
    int numMainParts = mainParts.length;
    if (numMainParts < 1 || numMainParts > 2) {
      throw new BatfishException("Invalid PrefixRange string: '" + str + "'");
    } else {
      prefix = new Prefix(mainParts[0]);
      return prefix;
    }
  }

  public PrefixRange(Prefix prefix, SubRange lengthRange) {
    super(prefix, lengthRange);
  }

  @JsonCreator
  public PrefixRange(String str) {
    super(prefixFromStr(str), lengthRangeFromStr(str));
  }

  public SubRange getLengthRange() {
    return _second;
  }

  public Prefix getPrefix() {
    return _first;
  }

  public boolean includesPrefix(Prefix argPrefix) {
    Prefix prefix = getPrefix();
    SubRange lengthRange = getLengthRange();
    int prefixLength = prefix.getPrefixLength();
    int minPrefixLength = lengthRange.getStart();
    int maxPrefixLength = lengthRange.getEnd();
    int argPrefixLength = argPrefix.getPrefixLength();
    if (minPrefixLength > argPrefixLength || maxPrefixLength < argPrefixLength) {
      return false;
    }
    long maskedPrefixAsLong = prefix.getAddress().getNetworkAddress(prefixLength).asLong();
    long argMaskedPrefixAsLong = argPrefix.getAddress().getNetworkAddress(prefixLength).asLong();
    return maskedPrefixAsLong == argMaskedPrefixAsLong;
  }

  public boolean includesPrefixRange(PrefixRange argPrefixRange) {
    Prefix prefix = getPrefix();
    SubRange lengthRange = getLengthRange();
    int prefixLength = prefix.getPrefixLength();
    long maskedPrefixAsLong = prefix.getAddress().getNetworkAddress(prefixLength).asLong();
    Prefix argPrefix = argPrefixRange.getPrefix();
    SubRange argLengthRange = argPrefixRange.getLengthRange();
    long argMaskedPrefixAsLong = argPrefix.getAddress().getNetworkAddress(prefixLength).asLong();
    return maskedPrefixAsLong == argMaskedPrefixAsLong
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
      return _first + ":" + low + "-" + high;
    }
  }
}
