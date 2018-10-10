package org.batfish.datamodel.transformation;

import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.transformation.Transformation.Direction;
import org.batfish.datamodel.transformation.Transformation.RuleAction;

public class TransformationEvaluator implements GenericTransformationRuleVisitor<Flow> {

  private final Map<String, IpAccessList> _aclDefinitions;
  private final Direction _direction;
  private final Flow _flow;
  private final Map<String, IpSpace> _namedIpSpaces;
  private @Nullable final String _srcInterface;

  public TransformationEvaluator(
      Flow flow,
      Direction direction,
      @Nullable String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      Map<String, IpSpace> namedIpSpaces) {
    _aclDefinitions = aclDefinitions;
    _direction = direction;
    _flow = flow;
    _namedIpSpaces = namedIpSpaces;
    _srcInterface = srcInterface;
  }

  @Override
  public Flow visitStaticTransformationRule(StaticNatRule rule) {
    if (rule.getAction() == RuleAction.DESTINATION_INSIDE) {
      // Not supported and/or invalid
      throw new BatfishException("Static NAT is invalid");
    }
    // Check if flow matches ACL
    if (aclDenies(rule)) {
      return _flow;
    }

    Flow.Builder transformedFlowBuilder = new Flow.Builder(_flow);
    switch (rule.getAction()) {
      case SOURCE_INSIDE:
        switch (_direction) {
          case EGRESS:
            // If source matches localNetwork, then rewrite source to globalNetwork
            if (rule.getLocalNetwork().containsIp(_flow.getSrcIp())) {
              transformedFlowBuilder.setSrcIp(shiftIp(_flow.getSrcIp(), rule, _direction));
            }
            break;
          case INGRESS:
            // If destination matches globalNetwork, then rewrite destination to localNetwork
            if (rule.getGlobalNetwork().containsIp(_flow.getDstIp())) {
              transformedFlowBuilder.setDstIp(shiftIp(_flow.getDstIp(), rule, _direction));
            }
            break;
          default:
            throw new BatfishException("Unexpected direction: " + _direction);
        }
        break;
      case SOURCE_OUTSIDE:
        switch (_direction) {
          case EGRESS:
            // If destination matches localNetwork, then rewrite destination to globalNetwork
            if (rule.getLocalNetwork().containsIp(_flow.getDstIp())) {
              transformedFlowBuilder.setDstIp(shiftIp(_flow.getDstIp(), rule, _direction));
            }
            break;
          case INGRESS:
            // If source matches globalNetwork, then rewrite flow source to localNetwork
            if (rule.getGlobalNetwork().containsIp(_flow.getSrcIp())) {
              transformedFlowBuilder.setSrcIp(shiftIp(_flow.getSrcIp(), rule, _direction));
            }
            break;
          default:
            throw new BatfishException("Unexpected direction: " + _direction);
        }
        break;
      default:
        throw new BatfishException("Static NAT is invalid");
    }

    return transformedFlowBuilder.build();
  }

  @Override
  public Flow visitDynamicTransformationRule(DynamicNatRule rule) {
    if (rule.getAction() == RuleAction.SOURCE_OUTSIDE) {
      // Not supported
      return _flow;
    }
    // Check if flow matches ACL
    if (aclDenies(rule)) {
      return _flow;
    }

    Ip natPoolStartIp = rule.getPoolIpFirst();
    Flow.Builder transformedFlowBuilder = new Flow.Builder(_flow);

    if (shouldProcess(rule, RuleAction.SOURCE_INSIDE, Direction.EGRESS)
        || shouldProcess(rule, RuleAction.SOURCE_OUTSIDE, Direction.EGRESS)
        || shouldProcess(rule, RuleAction.DESTINATION_INSIDE, Direction.INGRESS)) {
      transformedFlowBuilder.setSrcIp(natPoolStartIp);
    } else if (shouldProcess(rule, RuleAction.SOURCE_INSIDE, Direction.INGRESS)
        || shouldProcess(rule, RuleAction.SOURCE_OUTSIDE, Direction.EGRESS)
        || shouldProcess(rule, RuleAction.DESTINATION_INSIDE, Direction.EGRESS)) {
      transformedFlowBuilder.setDstIp(natPoolStartIp);
    }
    return transformedFlowBuilder.build();
  }

  private boolean aclDenies(Transformation rule) {
    IpAccessList acl = rule.getAcl();
    return acl != null
        && acl.filter(_flow, _srcInterface, _aclDefinitions, _namedIpSpaces).getAction()
            != LineAction.PERMIT;
  }

  private boolean shouldProcess(Transformation rule, RuleAction matchAction, Direction direction) {
    return rule.getAction() == matchAction && _direction == direction;
  }

  private static long localToGlobalShift(StaticNatRule rule) {
    return rule.getGlobalNetwork().getStartIp().asLong()
        - rule.getLocalNetwork().getStartIp().asLong();
  }

  private static Ip shiftIp(Ip ip, StaticNatRule rule, Direction direction) {
    long shift = localToGlobalShift(rule);
    if (direction == Direction.INGRESS) {
      shift = -shift;
    }
    return new Ip(ip.asLong() + shift);
  }
}
