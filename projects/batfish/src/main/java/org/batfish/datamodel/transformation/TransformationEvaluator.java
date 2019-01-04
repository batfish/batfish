package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.FlowDiff.flowDiff;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.Evaluator;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

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
    private final Map<TransformationType, ImmutableSortedSet.Builder<FlowDiff>> _flowDiffs =
        new EnumMap<>(TransformationType.class);

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

    private ImmutableSortedSet.Builder<FlowDiff> getFlowDiffs(TransformationType type) {
      return _flowDiffs.computeIfAbsent(type, k -> ImmutableSortedSet.naturalOrder());
    }

    private List<Step<?>> getTraceSteps() {
      return _flowDiffs
          .entrySet()
          .stream()
          .map(
              entry -> {
                ImmutableSortedSet<FlowDiff> flowDiffs = entry.getValue().build();
                TransformationStepDetail detail =
                    new TransformationStepDetail(entry.getKey(), flowDiffs);
                StepAction action =
                    flowDiffs.isEmpty() ? StepAction.PERMITTED : StepAction.TRANSFORMED;
                return new org.batfish.datamodel.flow.TransformationStep(detail, action);
              })
          .collect(ImmutableList.toImmutableList());
    }

    @Override
    public Void visitAssignIpAddressFromPool(AssignIpAddressFromPool step) {
      IpField ipField = step.getIpField();
      Ip oldValue = get(ipField);
      Ip newValue = step.getPoolStart();
      set(ipField, newValue);
      if (oldValue.equals(newValue)) {
        getFlowDiffs(step.getType());
      } else {
        getFlowDiffs(step.getType()).add(flowDiff(ipField, oldValue, newValue));
      }
      return null;
    }

    @Override
    public Void visitNoop(Noop noop) {
      /* getFlowDiffs makes sure the type is in the key set, which signals that we went through
       * the transformation
       */
      getFlowDiffs(noop.getType());
      return null;
    }

    @Override
    public Void visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet step) {
      IpField field = step.getIpField();
      Ip oldValue = get(field);
      Prefix targetSubnet = step.getSubnet();
      Prefix currentSubnetPrefix = Prefix.create(oldValue, targetSubnet.getPrefixLength());
      long offset = oldValue.asLong() - currentSubnetPrefix.getStartIp().asLong();
      Ip newValue = Ip.create(targetSubnet.getStartIp().asLong() + offset);
      set(field, newValue);
      getFlowDiffs(step.getType()).add(flowDiff(field, oldValue, newValue));
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

  /** The result of evaluating a {@link Transformation}. */
  public static final class TransformationResult {
    private final Flow _outputFlow;
    private final List<Step<?>> _traceSteps;

    TransformationResult(Flow outputFlow, List<Step<?>> traceSteps) {
      _outputFlow = outputFlow;
      _traceSteps = ImmutableList.copyOf(traceSteps);
    }

    public Flow getOutputFlow() {
      return _outputFlow;
    }

    public List<Step<?>> getTraceSteps() {
      return _traceSteps;
    }
  }

  public static TransformationResult eval(
      Transformation transformation,
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> namedAcls,
      Map<String, IpSpace> namedIpSpaces) {
    TransformationEvaluator evaluator =
        new TransformationEvaluator(flow, srcInterface, namedAcls, namedIpSpaces);
    evaluator.eval(transformation);
    return new TransformationResult(
        evaluator._flow.build(), evaluator._stepEvaluator.getTraceSteps());
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
