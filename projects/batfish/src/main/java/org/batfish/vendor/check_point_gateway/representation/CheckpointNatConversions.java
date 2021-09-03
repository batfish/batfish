package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.appliesToGateway;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toHeaderSpace;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.Machine;
import org.batfish.vendor.check_point_management.MachineVisitor;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRuleOrSectionVisitor;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSection;
import org.batfish.vendor.check_point_management.NatTranslatedService;
import org.batfish.vendor.check_point_management.NatTranslatedServiceVisitor;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.ServiceGroup;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.TypedManagementObject;

public class CheckpointNatConversions {
  private static final TranslatedServiceToTransformationSteps
      MANUAL_HIDE_TRANSLATED_SERVICE_TO_TRANSFORMATION_STEPS =
          new TranslatedServiceToTransformationSteps();
  private static final MachineToTransformationSteps MANUAL_HIDE_MACHINE_TO_TRANSFORMATION_STEPS =
      new MachineToTransformationSteps();

  public static @Nonnull List<TransformationStep> getManualHideServiceTransformationSteps(
      NatTranslatedService service) {
    return service.accept(MANUAL_HIDE_TRANSLATED_SERVICE_TO_TRANSFORMATION_STEPS);
  }

  public static @Nonnull List<TransformationStep> getManualHideSourceTransformationSteps(
      Machine translatedSource) {
    return translatedSource.accept(MANUAL_HIDE_MACHINE_TO_TRANSFORMATION_STEPS);
  }

  // TODO Implement
  private static class TranslatedServiceToTransformationSteps
      implements NatTranslatedServiceVisitor<List<TransformationStep>> {
    private TranslatedServiceToTransformationSteps() {}

    @Override
    public List<TransformationStep> visitOriginal(Original original) {
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
      // TODO: warn
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitServiceGroup(ServiceGroup serviceGroup) {
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitServiceTcp(ServiceTcp serviceTcp) {
      return ImmutableList.of();
    }
  }

  private static class MachineToTransformationSteps
      implements MachineVisitor<List<TransformationStep>> {

    @Override
    public List<TransformationStep> visitGatewayOrServer(GatewayOrServer gatewayOrServer) {
      // TODO: implement
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitHost(Host host) {
      return ImmutableList.of(TransformationStep.assignSourceIp(host.getIpv4Address()));
    }
  }

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
    steps.addAll(getManualHideServiceTransformationSteps((NatTranslatedService) service));
    return Optional.of(steps.build());
  }

  private static boolean checkValidManualHide(
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
    if (!(service instanceof NatTranslatedService)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-service %s has unsupported type %s and will be"
                  + " ignored",
              service.getName(), service.getClass()));
      valid = false;
    }
    return valid;
  }

  private static @Nonnull Stream<NatRule> getNatRules(
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

  static @Nonnull Stream<? extends NatRule> getManualNatRules(
      NatRulebase natRulebase, GatewayOrServer gateway) {
    return getNatRules(natRulebase, gateway).filter(rule -> !rule.isAutoGenerated());
  }

  static @Nonnull Optional<Transformation> manualHideRuleTransformation(
      NatRulebase natRulebase,
      org.batfish.vendor.check_point_management.NatRule natRule,
      Map<String, IpSpace> ipSpaces,
      Warnings warnings) {
    Optional<HeaderSpace> maybeOriginalHeaderSpace =
        toHeaderSpace(
            ipSpaces,
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
