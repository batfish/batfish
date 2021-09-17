package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.*;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.appliesToGateway;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toMatchExpr;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
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
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.HasNatSettings;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatHideBehindGateway;
import org.batfish.vendor.check_point_management.NatHideBehindIp;
import org.batfish.vendor.check_point_management.NatHideBehindVisitor;
import org.batfish.vendor.check_point_management.NatMethod;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRuleOrSectionVisitor;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSection;
import org.batfish.vendor.check_point_management.NatSettings;
import org.batfish.vendor.check_point_management.NatTranslatedDestination;
import org.batfish.vendor.check_point_management.NatTranslatedDestinationVisitor;
import org.batfish.vendor.check_point_management.NatTranslatedSource;
import org.batfish.vendor.check_point_management.NatTranslatedSourceVisitor;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.Uid;
import org.batfish.vendor.check_point_management.UnhandledNatHideBehind;

public class CheckpointNatConversions {

  @VisibleForTesting static final int NAT_PORT_FIRST = 10000;
  @VisibleForTesting static final int NAT_PORT_LAST = 60000;

  @VisibleForTesting
  static final TranslatedSourceToTransformationSteps TRANSLATED_SOURCE_TO_TRANSFORMATION_STEPS =
      new TranslatedSourceToTransformationSteps();

  @VisibleForTesting
  static final TranslatedDestinationToTransformationSteps
      TRANSLATED_DESTINATION_TO_TRANSFORMATION_STEPS =
          new TranslatedDestinationToTransformationSteps();

  public static @Nonnull List<TransformationStep> getSourceTransformationSteps(
      NatTranslatedSource translatedSource) {
    return TRANSLATED_SOURCE_TO_TRANSFORMATION_STEPS.visit(translatedSource);
  }

  public static @Nonnull List<TransformationStep> getDestinationTransformationSteps(
      NatTranslatedDestination translatedDestination) {
    return TRANSLATED_DESTINATION_TO_TRANSFORMATION_STEPS.visit(translatedDestination);
  }

  /** Visitor that gives the transformation steps for the translated-source of a NAT rule. */
  @VisibleForTesting
  static class TranslatedSourceToTransformationSteps
      implements NatTranslatedSourceVisitor<List<TransformationStep>> {

    @Override
    public List<TransformationStep> visitAddressRange(AddressRange addressRange) {
      Ip ipv4AddressFirst = addressRange.getIpv4AddressFirst();
      if (ipv4AddressFirst == null) {
        return ImmutableList.of();
      }
      Ip ipv4AddressLast = addressRange.getIpv4AddressLast();
      assert ipv4AddressLast != null;
      return ImmutableList.of(assignSourceIp(ipv4AddressFirst, ipv4AddressLast));
    }

    @Override
    public List<TransformationStep> visitHost(Host host) {
      Ip hostV4Addtess = host.getIpv4Address();
      return hostV4Addtess == null
          ? ImmutableList.of()
          : ImmutableList.of(assignSourceIp(hostV4Addtess));
    }

    @Override
    public List<TransformationStep> visitOriginal(Original original) {
      return ImmutableList.of();
    }
  }

  // TODO maybe merge
  /** Visitor that gives the transformation steps for the translated-destination of a NAT rule. */
  @VisibleForTesting
  static class TranslatedDestinationToTransformationSteps
      implements NatTranslatedDestinationVisitor<List<TransformationStep>> {

    @Override
    public List<TransformationStep> visitAddressRange(AddressRange addressRange) {
      Ip ipv4AddressFirst = addressRange.getIpv4AddressFirst();
      if (ipv4AddressFirst == null) {
        return ImmutableList.of();
      }
      Ip ipv4AddressLast = addressRange.getIpv4AddressLast();
      assert ipv4AddressLast != null;
      return ImmutableList.of(assignDestinationIp(ipv4AddressFirst, ipv4AddressLast));
    }

    @Override
    public List<TransformationStep> visitHost(Host host) {
      Ip hostV4Addtess = host.getIpv4Address();
      return hostV4Addtess == null
          ? ImmutableList.of()
          : ImmutableList.of(assignDestinationIp(hostV4Addtess));
    }

    @Override
    public List<TransformationStep> visitOriginal(Original original) {
      return ImmutableList.of();
    }
  }

  /**
   * Get a list of the transformation steps corresponding to the given valid translated fields of a
   * manual STATIC NAT rule.
   */
  static @Nonnull Optional<List<TransformationStep>> manualStaticTransformationSteps(
      NamedManagementObject src,
      NamedManagementObject dst,
      NamedManagementObject service,
      Warnings warnings) {
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    if (!checkValidManualStatic(src, dst, service, warnings)) {
      return Optional.empty();
    }
    steps.addAll(getSourceTransformationSteps((NatTranslatedSource) src));
    steps.addAll(getDestinationTransformationSteps((NatTranslatedDestination) dst));
    return Optional.of(steps.build());
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
    steps.addAll(getSourceTransformationSteps((NatTranslatedSource) src));
    steps.add(assignSourcePort(NAT_PORT_FIRST, NAT_PORT_LAST));
    return Optional.of(steps.build());
  }

  private static final CheckIpv4TranslatedSource CHECK_IPV4_TRANSLATED_SOURCE =
      new CheckIpv4TranslatedSource();

  private static final class CheckIpv4TranslatedSource
      implements NatTranslatedSourceVisitor<Boolean> {

    @Override
    public Boolean visitAddressRange(AddressRange addressRange) {
      return addressRange.getIpv4AddressFirst() != null
          && addressRange.getIpv4AddressLast() != null;
    }

    @Override
    public Boolean visitHost(Host host) {
      return host.getIpv4Address() != null;
    }

    @Override
    public Boolean visitOriginal(Original original) {
      return true;
    }
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
    if (src instanceof Original || !(src instanceof NatTranslatedSource)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-source %s has invalid type %s and will be"
                  + " ignored",
              src.getName(), src.getClass()));
      return false;
    } else if (!CHECK_IPV4_TRANSLATED_SOURCE.visit((NatTranslatedSource) src)) {
      // unsupported for foreseeable future, so don't bother warning
      return false;
    } else if (!(dst instanceof Original)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-destination %s has invalid type %s and will be"
                  + " ignored",
              dst.getName(), dst.getClass()));
      return false;
    } else if (!(service instanceof Original)) {
      warnings.redFlag(
          String.format(
              "Manual Hide NAT rule translated-service %s has invalid type %s and will be"
                  + " ignored",
              service.getName(), service.getClass()));
      return false;
    }
    return true;
  }

  /**
   * Returns {@code true} iff the translated fields of a manual STATIC NAT rule are valid, and warns
   * for each invalid field.
   */
  @VisibleForTesting
  static boolean checkValidManualStatic(
      NamedManagementObject src,
      NamedManagementObject dst,
      NamedManagementObject service,
      Warnings warnings) {
    // TODO ALL THIS
    if (!(src instanceof Host)) {
      warnings.redFlag(
          String.format(
              "Manual Static NAT rule translated-source %s has invalid type %s and will be"
                  + " ignored",
              src.getName(), src.getClass()));
      return false;
    } /*else if (!CHECK_IPV4_TRANSLATED_SOURCE.visit((NatTranslatedSource) src)) {
        // unsupported for foreseeable future, so don't bother warning
        return false;
      } */ else if (!(dst instanceof Host)) {
      warnings.redFlag(
          String.format(
              "Manual Static NAT rule translated-destination %s has invalid type %s and will be"
                  + " ignored",
              dst.getName(), dst.getClass()));
      return false;
    } else if (!(service instanceof Original)) {
      warnings.redFlag(
          String.format(
              "Manual Static NAT rule translated-service %s has invalid type %s and will be"
                  + " ignored",
              service.getName(), service.getClass()));
      return false;
    }
    return true;
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
   * Get the {@link Transformation} corresponding to the translated fields of the given manual NAT
   * rule. Returns {@link Optional#empty()} if the rule has invalid original or translated fields.
   */
  static @Nonnull Optional<Transformation> manualRuleTransformation(
      org.batfish.vendor.check_point_management.NatRule natRule,
      ServiceToMatchExpr serviceToMatchExpr,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Map<Uid, ? extends NamedManagementObject> objects,
      Warnings warnings) {
    Optional<AclLineMatchExpr> maybeOrigMatchExpr =
        toMatchExpr(
            objects.get(natRule.getOriginalSource()),
            objects.get(natRule.getOriginalDestination()),
            objects.get(natRule.getOriginalService()),
            serviceToMatchExpr,
            addressSpaceToMatchExpr,
            warnings);
    Optional<List<TransformationStep>> maybeSteps =
        natRule.getMethod() == NatMethod.HIDE
            ? manualHideTransformationSteps(
                objects.get(natRule.getTranslatedSource()),
                objects.get(natRule.getTranslatedDestination()),
                objects.get(natRule.getTranslatedService()),
                warnings)
            : manualStaticTransformationSteps(
                objects.get(natRule.getTranslatedSource()),
                objects.get(natRule.getTranslatedDestination()),
                objects.get(natRule.getTranslatedService()),
                warnings);
    if (!maybeOrigMatchExpr.isPresent() || !maybeSteps.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(when(maybeOrigMatchExpr.get()).apply(maybeSteps.get()).build());
  }

  /** Get the {@link Transformation} corresponding to the given {@link NatSettings}. */
  static @Nonnull Optional<Transformation> automaticHideRuleTransformation(
      HasNatSettings hasNatSettings,
      GatewayOrServer gateway,
      AddressSpaceToMatchExpr toMatchExprVisitor,
      Warnings warnings) {
    NatSettings natSettings = hasNatSettings.getNatSettings();
    assert natSettings.getAutoRule() && natSettings.getMethod() == NatMethod.HIDE;
    if (natSettings.getHideBehind() == null) {
      warnings.redFlag(
          String.format(
              "NAT settings on %s %s are invalid and will be ignored: type is HIDE, but hide-behind"
                  + " is missing",
              hasNatSettings.getClass(), hasNatSettings.getName()));
      return Optional.empty();
    } else if (!"All".equals(natSettings.getInstallOn())) {
      // TODO Support installing NAT rules on specific gateways.
      // TODO What does it mean if install-on is missing?
      warnings.redFlag(
          String.format(
              "Automatic NAT rules on specific gateways are not yet supported: NAT settings on %s"
                  + " %s will be ignored",
              hasNatSettings.getClass(), hasNatSettings.getName()));
      return Optional.empty();
    }
    // Build match expression to match traffic from the source to hide
    AclLineMatchExpr matchOriginalSrc = toMatchExprVisitor.convertSource(hasNatSettings);
    // Find IP to translate the hidden source to
    Optional<Ip> transformedIp =
        new NatHideBehindVisitor<Optional<Ip>>() {
          @Override
          public Optional<Ip> visitNatHideBehindGateway(NatHideBehindGateway natHideBehindGateway) {
            // TODO When hiding behind a gateway, should the translated IP be the gateway IP
            //      or the ingress interface IP?
            if (gateway.getIpv4Address() == null) {
              warnings.redFlag(
                  String.format(
                      "Cannot hide behind gateway %s because it has no IP: NAT settings on %s %s"
                          + " will be ignored",
                      gateway.getName(), hasNatSettings.getClass(), hasNatSettings.getName()));
              return Optional.empty();
            }
            return Optional.of(gateway.getIpv4Address());
          }

          @Override
          public Optional<Ip> visitNatHideBehindIp(NatHideBehindIp natHideBehindIp) {
            return Optional.of(natHideBehindIp.getIp());
          }

          @Override
          public Optional<Ip> visitUnhandledNatHideBehind(
              UnhandledNatHideBehind unhandledNatHideBehind) {
            warnings.redFlag(
                String.format(
                    "NAT hide-behind \"%s\" is not recognized: NAT settings on %s %s will be"
                        + " ignored",
                    unhandledNatHideBehind.getName(),
                    hasNatSettings.getClass(),
                    hasNatSettings.getName()));
            return Optional.empty();
          }
        }.visit(natSettings.getHideBehind());
    return transformedIp.map(
        transformed -> when(matchOriginalSrc).apply(assignSourceIp(transformed)).build());
  }

  static @Nonnull Optional<Transformation> mergeTransformations(
      List<Transformation> manualHideTransformations,
      List<Transformation> automaticHideTransformations) {
    // TODO: add non-HIDE
    if (manualHideTransformations.isEmpty() && automaticHideTransformations.isEmpty()) {
      return Optional.empty();
    }
    List<Transformation> reversedAutomaticHideTransformations =
        Lists.reverse(automaticHideTransformations);
    List<Transformation> reversedManualHideTransformations =
        Lists.reverse(manualHideTransformations);
    Iterator<Transformation> i =
        Iterators.concat(
            reversedAutomaticHideTransformations.iterator(),
            reversedManualHideTransformations.iterator());
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
