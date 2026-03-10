package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.BDDReachabilityUtils.computePortTransformationProtocolsBdd;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.branch;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.reverse;
import static org.batfish.bddreachability.transition.Transitions.transform;
import static org.batfish.datamodel.transformation.ReturnFlowTransformation.returnFlowTransformation;

import com.google.common.collect.BoundType;
import com.google.common.collect.RangeSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.ImmutableBDDInteger;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.PrimedBDDInteger;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.ReturnFlowTransformation;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.datamodel.transformation.TransformationStepVisitor;

/** Convert a {@link Transformation} to a BDD reachability graph {@link Transition}. */
@ParametersAreNonnullByDefault
public class TransformationToTransition {
  private final BDDPacket _bddPacket;
  private final IdentityHashMap<Transformation, Transition> _cache;
  private final IpAccessListToBdd _ipAccessListToBdd;
  private final TransformationStepToTransition _stepToTransition;
  private final BDD _ipProtocolsWithPortsBdd;

  public TransformationToTransition(BDDPacket bddPacket, IpAccessListToBdd ipAccessListToBdd) {
    _bddPacket = bddPacket;
    _cache = new IdentityHashMap<>();
    _ipAccessListToBdd = ipAccessListToBdd;
    _ipProtocolsWithPortsBdd = computePortTransformationProtocolsBdd(_bddPacket.getIpProtocol());
    _stepToTransition = new TransformationStepToTransition();
  }

  private static Transition assignIpFromPool(PrimedBDDInteger var, RangeSet<Ip> ranges) {
    ImmutableBDDInteger primeVar = var.getPrimeVar();
    BDD setValue =
        primeVar
            .getFactory()
            .orAll(
                ranges.asRanges().stream()
                    .map(
                        range -> {
                          assert range.lowerBoundType() == BoundType.CLOSED
                              && range.upperBoundType() == BoundType.CLOSED;
                          return primeVar.range(
                              range.lowerEndpoint().asLong(), range.upperEndpoint().asLong());
                        })
                    .collect(Collectors.toList()));
    return transform(setValue, var.getPairingFactory());
  }

  private Transition assignPortFromPool(PrimedBDDInteger var, int poolStart, int poolEnd) {
    BDD setValue = var.getPrimeVar().range(poolStart, poolEnd);
    Transition transform = transform(setValue, var.getPairingFactory());
    // AssignPortFromPool is a noop on protocols that don't have ports
    return branch(_ipProtocolsWithPortsBdd, transform, IDENTITY);
  }

  private class TransformationStepToTransition implements TransformationStepVisitor<Transition> {
    private PrimedBDDInteger ipField(IpField ipField) {
      return switch (ipField) {
        case DESTINATION -> _bddPacket.getDstIpPrimedBDDInteger();
        case SOURCE -> _bddPacket.getSrcIpPrimedBDDInteger();
      };
    }

    private PrimedBDDInteger portField(PortField portField) {
      return switch (portField) {
        case DESTINATION -> _bddPacket.getDstPortPrimedBDDInteger();
        case SOURCE -> _bddPacket.getSrcPortPrimedBDDInteger();
      };
    }

    @Override
    public Transition visitAssignIpAddressFromPool(AssignIpAddressFromPool step) {
      return assignIpFromPool(ipField(step.getIpField()), step.getIpRanges());
    }

    @Override
    public Transition visitNoop(Noop noop) {
      return IDENTITY;
    }

    @Override
    public Transition visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet step) {
      Prefix prefix = step.getSubnet();
      int len = prefix.getPrefixLength();
      PrimedBDDInteger var = ipField(step.getIpField());
      BDDInteger primeVar = var.getPrimeVar();
      return transform(primeVar.toBDD(prefix), var.getPairingFactory(len));
    }

    @Override
    public Transition visitAssignPortFromPool(AssignPortFromPool step) {
      return assignPortFromPool(
          portField(step.getPortField()), step.getPoolStart(), step.getPoolEnd());
    }

    @Override
    public Transition visitApplyAll(ApplyAll applyAll) {
      return compose(
          applyAll.getSteps().stream().map(step -> step.accept(this)).toArray(Transition[]::new));
    }

    @Override
    public Transition visitApplyAny(ApplyAny applyAny) {
      return Transitions.or(
          applyAny.getSteps().stream().map(step -> step.accept(this)).toArray(Transition[]::new));
    }
  }

  public Transition toTransition(@Nullable Transformation transformation) {
    return transformation == null
        ? IDENTITY
        : _cache.computeIfAbsent(transformation, this::computeTransition);
  }

  /**
   * The return flow transition applies the {@link ReturnFlowTransformation} (in which source and
   * destination fields have been swapped) in the reverse direction (because the return flow travels
   * in the opposite direction as the original).
   */
  public Transition toReturnFlowTransition(@Nullable Transformation transformation) {
    return transformation == null
        ? IDENTITY
        : reverse(toTransition(returnFlowTransformation(transformation)));
  }

  private @Nonnull Transition computeTransition(Transformation transformation) {
    BDD guard = _ipAccessListToBdd.toBdd(transformation.getGuard());
    Transition steps = computeSteps(transformation.getTransformationSteps());

    Transition trueBranch =
        transformation.getAndThen() == null
            ? steps
            : compose(steps, toTransition(transformation.getAndThen()));
    Transition falseBranch =
        transformation.getOrElse() == null
            ? Identity.INSTANCE
            : toTransition(transformation.getOrElse());
    return branch(guard, trueBranch, falseBranch);
  }

  private Transition computeSteps(List<TransformationStep> transformationSteps) {
    return compose(
        transformationSteps.stream().map(_stepToTransition::visit).toArray(Transition[]::new));
  }
}
