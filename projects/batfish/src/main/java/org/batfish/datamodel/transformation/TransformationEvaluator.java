package org.batfish.datamodel.transformation;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.Evaluator;

/** Evaluates a {@link Transformation} on an input {@link Flow}. */
public class TransformationEvaluator {
  private final Flow.Builder _flow;
  private final String _srcInterface;
  private final Map<String, IpAccessList> _namedAcls;
  private final Map<String, IpSpace> _namedIpSpaces;
  private final StepEvaluator _stepEvaluator;

  // the ACL evaluator has to be re-initialized every time we transform the flow
  private Evaluator _aclEvaluator;

  @VisibleForTesting
  class StepEvaluator implements TransformationStepVisitor<Void> {
    private void set(IpField field, Ip ip) {
      switch (field) {
        case DESTINATION:
          _flow.setDstIp(ip);
          break;
        case SOURCE:
          _flow.setSrcIp(ip);
          break;
        default:
          throw new IllegalArgumentException("unknown IpField " + field);
      }
    }

    private Ip get(IpField field) {
      switch (field) {
        case DESTINATION:
          return _flow.getDstIp();
        case SOURCE:
          return _flow.getSrcIp();
        default:
          throw new IllegalArgumentException("unknown IpField " + field);
      }
    }

    @Override
    public Void visitAssignIpAddressFromPool(AssignIpAddressFromPool assignIpAddressFromPool) {
      set(assignIpAddressFromPool.getIpField(), assignIpAddressFromPool.getPoolStart());
      return null;
    }

    @Override
    public Void visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet) {
      IpField field = shiftIpAddressIntoSubnet.getIpField();
      Ip ip = get(field);
      Prefix targetSubnet = shiftIpAddressIntoSubnet.getSubnet();
      Prefix currentSubnetPrefix = Prefix.create(ip, targetSubnet.getPrefixLength());
      long offset = ip.asLong() - currentSubnetPrefix.getStartIp().asLong();
      set(field, Ip.create(targetSubnet.getStartIp().asLong() + offset));
      return null;
    }
  }

  private TransformationEvaluator(
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> namedAcls,
      Map<String, IpSpace> namedIpSpaces) {
    _flow = flow.toBuilder();
    _srcInterface = srcInterface;
    _namedAcls = namedAcls;
    _namedIpSpaces = namedIpSpaces;
    _stepEvaluator = new StepEvaluator();
    initAclEvaluator();
  }

  public static Flow eval(
      Transformation transformation,
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> namedAcls,
      Map<String, IpSpace> namedIpSpaces) {
    TransformationEvaluator evaluator =
        new TransformationEvaluator(flow, srcInterface, namedAcls, namedIpSpaces);
    evaluator.eval(transformation);
    return evaluator._flow.build();
  }

  private void initAclEvaluator() {
    _aclEvaluator = new Evaluator(_flow.build(), _srcInterface, _namedAcls, _namedIpSpaces);
  }

  private void eval(Transformation transformation) {
    Transformation node = transformation;
    while (node != null) {
      if (_aclEvaluator.visit(node.getGuard())) {
        node.getTransformationSteps().forEach(_stepEvaluator::visit);
        initAclEvaluator();
        node = node.getAndThen();
      } else {
        node = node.getOrElse();
      }
    }
  }
}
