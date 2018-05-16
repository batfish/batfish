package org.batfish.symbolic.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.SubRange;
import org.batfish.symbolic.bdd.BDDRouteFactory.BDDRoute;

public class BDDUtils {

  public static BDD firstBitsEqual(BDDFactory factory, BDD[] bits, Ip ip, int length) {
    long b = ip.asLong();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.andWith(bits[i].not());
      }
    }
    return acc;
  }

  public static BDD firstBitsEqual(BDDFactory factory, BDD[] bits, Prefix p, int length) {
    return firstBitsEqual(factory, bits, p.getStartIp(), length);
  }

  public static BDD prefixRangeToBdd(BDDFactory factory, BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    SubRange r = range.getLengthRange();
    int len = p.getPrefixLength();
    int lower = r.getStart();
    int upper = r.getEnd();

    BDD lowerBitsMatch = firstBitsEqual(factory, record.getPrefix().getBitvec(), p, len);
    BDD acc = factory.zero();
    if (lower == 0 && upper == 32) {
      acc = factory.one();
    } else {
      for (int i = lower; i <= upper; i++) {
        BDD equalLen = record.getPrefixLength().value(i);
        acc = acc.or(equalLen);
      }
    }
    return acc.and(lowerBitsMatch);
  }

  public static BDD prefixToBdd(BDDFactory factory, BDDRoute record, Prefix p) {
    BDD bitsMatch = firstBitsEqual(factory, record.getPrefix().getBitvec(), p, 32);
    BDD correctLen = record.getPrefixLength().value(p.getPrefixLength());
    return bitsMatch.and(correctLen);
  }
}
