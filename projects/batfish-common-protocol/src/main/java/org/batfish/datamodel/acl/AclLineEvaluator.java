package org.batfish.datamodel.acl;

import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

/**
 * Evaluates the action of an {@link AclLine} on a given {@link Flow}. Visiting a line returns the
 * {@link LineAction} it will take on the flow, or {@code null} if the line does not match the flow.
 */
public class AclLineEvaluator extends Evaluator implements GenericAclLineVisitor<LineAction> {

  public AclLineEvaluator(
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    super(flow, srcInterface, availableAcls, namedIpSpaces);
  }

  // Override visit in order to explicitly mark it nullable
  @Override
  @Nullable
  public LineAction visit(AclLine line) {
    return line.accept(this);
  }

  @Override
  public LineAction visitExprAclLine(ExprAclLine exprAclLine) {
    return visit(exprAclLine.getMatchCondition()) ? exprAclLine.getAction() : null;
  }
}
