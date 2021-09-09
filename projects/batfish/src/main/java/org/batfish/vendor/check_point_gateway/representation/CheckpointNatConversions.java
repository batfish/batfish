package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.appliesToGateway;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toMatchExpr;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.Machine;
import org.batfish.vendor.check_point_management.MachineVisitor;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRuleOrSectionVisitor;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSection;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.Uid;

public class CheckpointNatConversions {

  @VisibleForTesting static final int NAT_PORT_FIRST = 10000;
  @VisibleForTesting static final int NAT_PORT_LAST = 60000;

  @VisibleForTesting
  static final MachineToTransformationSteps MANUAL_HIDE_MACHINE_TO_TRANSFORMATION_STEPS =
      new MachineToTransformationSteps();

  public static @Nonnull List<TransformationStep> getManualHideSourceTransformationSteps(
      Machine translatedSource) {
    return translatedSource.accept(MANUAL_HIDE_MACHINE_TO_TRANSFORMATION_STEPS);
  }

  /**
   * Visitor that gives the transformation steps for the translated-source or translated-destination
   * of a NAT rule.
   */
  @VisibleForTesting
  static class MachineToTransformationSteps implements MachineVisitor<List<TransformationStep>> {

    @Override
    public List<TransformationStep> visitGatewayOrServer(GatewayOrServer gatewayOrServer) {
      // TODO: implement
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitHost(Host host) {
      Ip hostV4Addtess = host.getIpv4Address();
      return hostV4Addtess == null
          ? ImmutableList.of()
          : ImmutableList.of(assignSourceIp(hostV4Addtess));
    }
  }

  /**
   * Get a list of the transformation steps corresponding to the given valid translated fields of a
   * manual HIDE NAT rule.
   */
  static @Nonnull Optional<List<TransformationStep>> manualHideTransformationSteps(
      NamedManagementObject src,
      NamedManagementObject dst,
      NamedManagementObject service,
      Warnings warnings) {
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    if (!checkValidManualHide(src, dst, service, warnings)) {
      return Optional.empty();
    }
    steps.addAll(getManualHideSourceTransformationSteps((Machine) src));
    steps.add(assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST));
    return Optional.of(steps.build());
  }

  /**
   * Returns {@code true} iff the translated fields of a manual HIDE NAT rule are valid, and warns
   * for each invalid field.
   */
  @VisibleForTesting
  static boolean checkValidManualHide(
      NamedManagementObject src,
      NamedManagementObject dst,
      NamedManagementObject service,
      Warnings warnings) {
    boolean valid = true;
    if (!(src instanceof Machine)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-source %s has unsupported type %s and will be"
                  + " ignored",
              src.getName(), src.getClass()));
      valid = false;
    }
    if (!(dst instanceof Original)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-destination %s has unsupported type %s and will be"
                  + " ignored",
              dst.getName(), dst.getClass()));
      valid = false;
    }
    if (!(service instanceof Original)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-service %s has unsupported type %s and will be"
                  + " ignored",
              service.getName(), service.getClass()));
      valid = false;
    }
    return valid;
  }

  /** Get a stream of the applicable NAT rules for the provided gateway. */
  @VisibleForTesting
  static @Nonnull Stream<NatRule> getApplicableNatRules(
      NatRulebase natRulebase, GatewayOrServer gateway) {
    return natRulebase.getRulebase().stream()
        // Convert to stream of all rules
        .flatMap(
            ruleOrSection ->
                new NatRuleOrSectionVisitor<Stream<NatRule>>() {
                  @Override
                  public Stream<NatRule> visitNatRule(NatRule natRule) {
                    return Stream.of(natRule);
                  }

                  @Override
                  public Stream<NatRule> visitNatSection(NatSection natSection) {
                    return natSection.getRulebase().stream();
                  }
                }.visit(ruleOrSection))
        .filter(NatRule::isEnabled)
        .filter(natRule -> appliesToGateway(natRule.getInstallOn(), gateway));
  }

  /** Get a stream of the applicable manual NAT rules for the provided gateway. */
  static @Nonnull Stream<? extends NatRule> getManualNatRules(
      NatRulebase natRulebase, GatewayOrServer gateway) {
    return getApplicableNatRules(natRulebase, gateway).filter(rule -> !rule.isAutoGenerated());
  }

  /**
   * Get the {@link Transformation} corresponding to the translated fields of the given manual HIDE
   * NAT rule. Returns {@link Optional#empty()} if the rule has invalid original or translated
   * fields.
   */
  static @Nonnull Optional<Transformation> manualHideRuleTransformation(
      org.batfish.vendor.check_point_management.NatRule natRule,
      ServiceToMatchExpr serviceToMatchExpr,
      Map<Uid, ? extends NamedManagementObject> objects,
      Warnings warnings) {
    Optional<AclLineMatchExpr> maybeOrigMatchExpr =
        toMatchExpr(
            objects.get(natRule.getOriginalSource()),
            objects.get(natRule.getOriginalDestination()),
            objects.get(natRule.getOriginalService()),
            serviceToMatchExpr,
            warnings);
    Optional<List<TransformationStep>> maybeSteps =
        manualHideTransformationSteps(
            objects.get(natRule.getTranslatedSource()),
            objects.get(natRule.getTranslatedDestination()),
            objects.get(natRule.getTranslatedService()),
            warnings);
    if (!maybeOrigMatchExpr.isPresent() || !maybeSteps.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(when(maybeOrigMatchExpr.get()).apply(maybeSteps.get()).build());
  }

  static @Nonnull Optional<Transformation> mergeTransformations(
      List<Transformation> manualHideTransformations) {
    // TODO: add automatic, non-HIDE
    if (manualHideTransformations.isEmpty()) {
      return Optional.empty();
    }
    List<Transformation> reversedManualHideTransformations =
        Lists.reverse(manualHideTransformations);
    Iterator<Transformation> i = reversedManualHideTransformations.iterator();
    Transformation finalTransformation = i.next();
    while (i.hasNext()) {
      Transformation previousTransformation = i.next();
      finalTransformation =
          Transformation.when(previousTransformation.getGuard())
              .apply(previousTransformation.getTransformationSteps())
              .setOrElse(finalTransformation)
              .build();
    }
    return Optional.of(finalTransformation);
  }
}
