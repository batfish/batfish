package org.batfish.datamodel.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class OrExpr extends AclLineExpr {
  private final Set<AclLineExpr> _disjuncts;

  public OrExpr(Set<AclLineExpr> disjuncts) {
    _disjuncts = ImmutableSet.copyOf(disjuncts);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return _disjuncts.stream().anyMatch(d -> d.match(flow, srcInterface, availableAcls));
  }

  @Override
  public boolean exprEquals(Object o) {
    return _disjuncts == ((OrExpr) o).getDisjuncts();
  }

  @Override
  public int hashCode() {
    // Start hash with something to differentiate from another expr with the same set of exprs
    int hash = "Or".hashCode();
    int prime = 31;
    for (AclLineExpr b : _disjuncts) {
      hash *= prime;
      hash += b.hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    String separator = " Or ";
    _disjuncts.forEach(
        d -> {
          sb.append(d.toString());
          sb.append(separator);
        });
    sb.setLength(sb.length() - separator.length());
    sb.append(")");
    return sb.toString();
  }

  public Set<AclLineExpr> getDisjuncts() {
    return _disjuncts;
  }
}
