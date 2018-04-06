package org.batfish.datamodel.expr;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class NotExpr extends AclLineExpr {
  private final AclLineExpr _operand;

  public NotExpr(AclLineExpr operand) {
    _operand = operand;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return !_operand.match(flow, srcInterface, availableAcls);
  }

  @Override
  public boolean exprEquals(Object o) {
    return _operand == ((NotExpr) o).getOperand();
  }

  @Override
  public int hashCode() {
    // Start hash with something to differentiate from another expr with the same set of exprs
    int hash = "Not".hashCode();
    int prime = 31;
    hash *= prime;
    hash += _operand.hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "Not_" + _operand.toString();
  }

  public AclLineExpr getOperand() {
    return _operand;
  }
}
