package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.transition.Transitions.IDENTITY;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
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

  public TransformationToTransition(BDDPacket bddPacket, IpAccessListToBdd ipAccessListToBdd) {
    _bddPacket = bddPacket;
    _cache = new IdentityHashMap<>();
    _ipAccessListToBdd = ipAccessListToBdd;
    _stepToTransition = new TransformationStepToTransition();
  }

  private static EraseAndSet assignIpFromPool(BDDInteger var, Ip poolStart, Ip poolEnd) {
    BDD erase = Arrays.stream(var.getBitvec()).reduce(var.getFactory().one(), BDD::and);
    BDD setValue =
        poolStart.equals(poolEnd)
            ? var.value(poolStart.asLong())
            : var.geq(poolStart.asLong()).and(var.leq(poolEnd.asLong()));
    return new EraseAndSet(erase, setValue);
  }

  private static EraseAndSet shiftIpIntoPrefix(BDDInteger var, Prefix prefix) {
    int len = prefix.getPrefixLength();
    BDD erase = Arrays.stream(var.getBitvec()).limit(len).reduce(var.getFactory().one(), BDD::and);
    BDD setValue = new IpSpaceToBDD(var).toBDD(prefix);
    return new EraseAndSet(erase, setValue);
  }

  private class TransformationStepToTransition implements TransformationStepVisitor<Transition> {
    private BDDInteger ipField(IpField ipField) {
      switch (ipField) {
        case DESTINATION:
          return _bddPacket.getDstIp();
        case SOURCE:
          return _bddPacket.getSrcIp();
        default:
          throw new IllegalArgumentException("Unknown IpField: " + ipField);
      }
    }

    @Override
    public Transition visitAssignIpAddressFromPool(AssignIpAddressFromPool step) {
      return assignIpFromPool(ipField(step.getIpField()), step.getPoolStart(), step.getPoolEnd());
    }

    @Override
    public Transition visitNoop(Noop noop) {
      return IDENTITY;
    }

    @Override
    public Transition visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet step) {
      return shiftIpIntoPrefix(ipField(step.getIpField()), step.getSubnet());
    }

    @Override
    public Transition visitAssignPortFromPool(AssignPortFromPool assignPortFromPool) {
      // TODO
      return null;
    }
  }

  public Transition toTransition(@Nullable Transformation transformation) {
    return transformation == null
        ? Identity.INSTANCE
        : _cache.computeIfAbsent(transformation, this::computeTransition);
  }

  private Transition computeTransition(Transformation transformation) {
    BDD guard = _ipAccessListToBdd.toBdd(transformation.getGuard());
    Transition steps = computeSteps(transformation.getTransformationSteps());

    Transition trueBranch =
        transformation.getAndThen() == null
            ? steps
            : new Composite(steps, toTransition(transformation.getAndThen()));
    Transition falseBranch =
        transformation.getOrElse() == null
            ? Identity.INSTANCE
            : toTransition(transformation.getOrElse());
    return new Branch(guard, trueBranch, falseBranch);
  }

  private Transition computeSteps(List<TransformationStep> transformationSteps) {
    return transformationSteps.stream()
        .map(_stepToTransition::visit)
        .reduce(Composite::new)
        .orElse(Identity.INSTANCE);
  }
}
