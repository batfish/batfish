package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.appliesToGateway;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toHeaderSpace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.Machine;
import org.batfish.vendor.check_point_management.MachineVisitor;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRuleOrSectionVisitor;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSection;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.TypedManagementObject;

public class CheckpointNatConversions {

  @VisibleForTesting static final int NAT_PORT_FIRST = 10000;
  @VisibleForTesting static final int NAT_PORT_LAST = 60000;

  private static final MachineToTransformationSteps MANUAL_HIDE_MACHINE_TO_TRANSFORMATION_STEPS =
      new MachineToTransformationSteps();

  public static @Nonnull List<TransformationStep> getManualHideSourceTransformationSteps(
      Machine translatedSource) {
    return translatedSource.accept(MANUAL_HIDE_MACHINE_TO_TRANSFORMATION_STEPS);
  }

  /**
   * Visitor that gives the transformation steps for the translated-source or translated-destination
   * of a NAT rule.
   */
  private static class MachineToTransformationSteps
      implements MachineVisitor<List<TransformationStep>> {

    @Override
    public List<TransformationStep> visitGatewayOrServer(GatewayOrServer gatewayOrServer) {
      // TODO: implement
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitHost(Host host) {
      return ImmutableList.of(assignSourceIp(host.getIpv4Address()));
    }
  }

  /**
   * Get a list of the transformation steps corresponding to the given valid translated fields of a
   * manual HIDE NAT rule.
   */
  static @Nonnull Optional<List<TransformationStep>> manualHideTransformationSteps(
      TypedManagementObject src,
      TypedManagementObject dst,
      TypedManagementObject service,
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
      TypedManagementObject src,
      TypedManagementObject dst,
      TypedManagementObject service,
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
      NatRulebase natRulebase,
      org.batfish.vendor.check_point_management.NatRule natRule,
      Warnings warnings) {
    Optional<HeaderSpace> maybeOriginalHeaderSpace =
        toHeaderSpace(
            natRulebase.getObjectsDictionary().get(natRule.getOriginalSource()),
            natRulebase.getObjectsDictionary().get(natRule.getOriginalDestination()),
            natRulebase.getObjectsDictionary().get(natRule.getOriginalService()),
            warnings);
    Optional<List<TransformationStep>> maybeSteps =
        manualHideTransformationSteps(
            natRulebase.getObjectsDictionary().get(natRule.getTranslatedSource()),
            natRulebase.getObjectsDictionary().get(natRule.getTranslatedDestination()),
            natRulebase.getObjectsDictionary().get(natRule.getTranslatedService()),
            warnings);
    if (!maybeOriginalHeaderSpace.isPresent() || !maybeSteps.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(
        when(new MatchHeaderSpace(maybeOriginalHeaderSpace.get())).apply(maybeSteps.get()).build());
  }
}
