package org.batfish.datamodel.acl;

import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

/**
 * Evaluates the action of an {@link IpAccessListLine} on a given {@link Flow}. Visiting a line
 * returns the {@link LineAction} it will take on the flow, or {@code null} if the line does not
 * match the flow.
 */
public class IpAccessListLineEvaluator implements GenericIpAccessListLineVisitor<LineAction> {

  private final Evaluator _matchExprEvaluator;

  public IpAccessListLineEvaluator(
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    _matchExprEvaluator = new Evaluator(flow, srcInterface, availableAcls, namedIpSpaces);
  }

  // Override visit in order to explicitly mark it nullable
  @Override
  @Nullable
  public LineAction visit(IpAccessListLine line) {
    return line.accept(this);
  }

  @Override
  public LineAction visitIpAccessListLine(IpAccessListLine ipAccessListLine) {
    return _matchExprEvaluator.visit(ipAccessListLine.getMatchCondition())
        ? ipAccessListLine.getAction()
        : null;
  }
}
