package org.batfish.symbolic.bdd;

import java.util.HashSet;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.symbolic.AstVisitor;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDNetFactory.BDDRoute;

public class BDDUtils {

  public static Set<Integer> findAllLocalPrefs(Graph g) {
    Set<Integer> prefs = new HashSet<>();
    AstVisitor v = new AstVisitor();
    v.visit(
        g.getConfigurations().values(),
        stmt -> {
          if (stmt instanceof SetLocalPreference) {
            SetLocalPreference slp = (SetLocalPreference) stmt;
            IntExpr ie = slp.getLocalPreference();
            if (ie instanceof LiteralInt) {
              LiteralInt l = (LiteralInt) ie;
              prefs.add(l.getValue());
            }
          }
        },
        expr -> {});
    return prefs;
  }

  public static int numBits(int size) {
    double log = Math.log((double) size);
    double base = Math.log((double) 2);
    if (size == 0) {
      return 0;
    } else {
      return (int) Math.ceil(log / base);
    }
  }

  public static BDD firstBitsEqual(BDDFactory factory, BDD[] bits, Ip ip, int length) {
    long b = ip.asLong();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc.andWith(bits[i].not());
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
        acc.orWith(equalLen);
      }
    }
    return acc.andWith(lowerBitsMatch);
  }

  public static BDD prefixToBdd(BDDFactory factory, BDDRoute record, Prefix p) {
    BDD bitsMatch = firstBitsEqual(factory, record.getPrefix().getBitvec(), p, 32);
    BDD correctLen = record.getPrefixLength().value(p.getPrefixLength());
    return bitsMatch.andWith(correctLen);
  }
}
