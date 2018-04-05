package org.batfish.datamodel.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class AndExpr extends BooleanExpr {
  private final Set<BooleanExpr> _conjuncts;

  public AndExpr(Set<BooleanExpr> conjuncts) {
    _conjuncts = ImmutableSet.copyOf(conjuncts);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return _conjuncts.stream().allMatch(c -> c.match(flow, srcInterface, availableAcls));
  }

  @Override
  public int hashCode() {
    // Start hash with something to differentiate from another expr with the same set of exprs
    int hash = "And".hashCode();
    int prime = 31;
    for (BooleanExpr b : _conjuncts) {
      hash *= prime;
      hash += b.hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    String separator = " And ";
    _conjuncts.forEach(
        c -> {
          sb.append(c.toString());
          sb.append(separator);
        });
    // sb.setLength(sb.length() - separator.length());
    sb.append(")");
    return sb.toString();
  }
}
