package org.batfish.datamodel.expr;

import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class NotExpr extends BooleanExpr {
  private final BooleanExpr _operand;

  public NotExpr(BooleanExpr operand) {
    _operand = operand;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return !_operand.match(flow, srcInterface, availableAcls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(false, _operand.hashCode());
  }

  @Override
  public String toString() {
    return "Not_" + _operand.toString();
  }
}
