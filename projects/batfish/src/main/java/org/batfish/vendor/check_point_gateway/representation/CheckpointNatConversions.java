package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
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
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.HasNatSettings;
import org.batfish.vendor.check_point_management.HasNatSettingsVisitor;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.ManagementDomain;
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
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.Uid;
import org.batfish.vendor.check_point_management.UnhandledNatHideBehind;

public class CheckpointNatConversions {

  @VisibleForTesting static final int NAT_PORT_FIRST = 10000;
  @VisibleForTesting static final int NAT_PORT_LAST = 60000;
  @VisibleForTesting public static final Ip HIDE_BEHIND_GATEWAY_IP = Ip.ZERO;

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
   * manual STATIC NAT rule. Returns {@link Optional#empty()} if the fields are not valid/supported
   * for manual static NAT.
   */
  static @Nonnull Optional<List<TransformationStep>> manualStaticTransformationSteps(
      NatRule natRule, Map<Uid, ? extends NamedManagementObject> objects, Warnings warnings) {
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    if (!checkValidManualStatic(natRule, objects, warnings)) {
      return Optional.empty();
    }
    steps.addAll(
        getSourceTransformationSteps(
            (NatTranslatedSource) objects.get(natRule.getTranslatedSource())));
    steps.addAll(
        getDestinationTransformationSteps(
            (NatTranslatedDestination) objects.get(natRule.getTranslatedDestination())));
    return Optional.of(steps.build());
  }

  /**
   * Get a list of the transformation steps corresponding to the given translated fields of a manual
   * HIDE NAT rule.
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
   * for each invalid field. Assumes a STATIC NAT rule is passed in.
   */
  @VisibleForTesting
  static boolean checkValidManualStatic(
      NatRule natRule, Map<Uid, ? extends NamedManagementObject> objects, Warnings warnings) {
    // TODO loosen these constraints, e.g. allowing transforming addresses from Network->Network
    NamedManagementObject src = objects.get(natRule.getTranslatedSource());
    NamedManagementObject dst = objects.get(natRule.getTranslatedDestination());
    NamedManagementObject service = objects.get(natRule.getTranslatedService());
    NamedManagementObject origSrc = objects.get(natRule.getOriginalSource());
    NamedManagementObject origDst = objects.get(natRule.getOriginalDestination());

    if (!(src instanceof Original || src instanceof Host)) {
      warnings.redFlag(
          String.format(
              "Manual Static NAT rule translated-source %s has unsupported type %s and will be"
                  + " ignored",
              src.getName(), src.getClass().getSimpleName()));
      return false;
    } else if (!(dst instanceof Original || dst instanceof Host)) {
      warnings.redFlag(
          String.format(
              "Manual Static NAT rule translated-destination %s has unsupported type %s and will be"
                  + " ignored",
              dst.getName(), dst.getClass().getSimpleName()));
      return false;
    } else if (!(service instanceof Original)) {
      warnings.redFlag(
          String.format(
              "Manual Static NAT rule cannot translate services (like %s of type %s) and will be"
                  + " ignored",
              service.getName(), service.getClass().getSimpleName()));
      return false;
    }

    // Make sure if translation is occurring, the original and translated types line up correctly
    if (!(src instanceof Original)) {
      if (!src.getClass().equals(origSrc.getClass())) {
        warnings.redFlag(
            String.format(
                "Manual Static NAT rule translated-source %s of type %s is incompatible with"
                    + " original-source %s of type %s and will be ignored",
                src.getName(),
                src.getClass().getSimpleName(),
                origSrc.getName(),
                origSrc.getClass().getSimpleName()));
        return false;
      }
    }
    if (!(dst instanceof Original)) {
      if (!dst.getClass().equals(origDst.getClass())) {
        warnings.redFlag(
            String.format(
                "Manual Static NAT rule translated-destination %s of type %s is incompatible with"
                    + " original-destination %s of type %s and will be ignored",
                dst.getName(),
                dst.getClass().getSimpleName(),
                origDst.getName(),
                origDst.getClass().getSimpleName()));
        return false;
      }
    }
    return true;
  }

  /** Get a stream of the applicable NAT rules for the provided gateway. */
  @VisibleForTesting
  static @Nonnull Stream<NatRule> getApplicableNatRules(
      NatRulebase natRulebase, Map.Entry<ManagementDomain, GatewayOrServer> domainAndGateway) {
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
        .filter(
            natRule ->
                appliesToGateway(
                    natRule.getInstallOn(), natRulebase.getObjectsDictionary(), domainAndGateway));
  }

  /** Get a stream of the applicable manual NAT rules for the provided gateway. */
  static @Nonnull Stream<? extends NatRule> getManualNatRules(
      NatRulebase natRulebase, Map.Entry<ManagementDomain, GatewayOrServer> domainAndGateway) {
    return getApplicableNatRules(natRulebase, domainAndGateway)
        .filter(rule -> !rule.isAutoGenerated());
  }

  /**
   * Get the {@link AclLineMatchExpr} corresponding to the traffic that matches the given manual NAT
   * rule. Returns {@link Optional#empty()} if the rule has invalid original fields.
   */
  static @Nonnull Optional<AclLineMatchExpr> matchManualRule(
      org.batfish.vendor.check_point_management.NatRule natRule,
      ServiceToMatchExpr serviceToMatchExpr,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Map<Uid, ? extends NamedManagementObject> objects,
      Warnings warnings) {
    return toMatchExpr(
        objects.get(natRule.getOriginalSource()),
        objects.get(natRule.getOriginalDestination()),
        objects.get(natRule.getOriginalService()),
        serviceToMatchExpr,
        addressSpaceToMatchExpr,
        warnings);
  }

  /**
   * Get the {@link Transformation} corresponding to the translated fields of the given manual NAT
   * rule. Returns {@link Optional#empty()} if the rule has invalid translated fields.
   *
   * <p>This transformation is expected to be used in a context where match conditions have already
   * been checked, so does not have a guard condition.
   */
  static @Nonnull Optional<Transformation> manualRuleTransformation(
      org.batfish.vendor.check_point_management.NatRule natRule,
      Map<Uid, ? extends NamedManagementObject> objects,
      Warnings warnings) {
    Optional<List<TransformationStep>> maybeSteps;
    if (natRule.getMethod() == NatMethod.HIDE) {
      maybeSteps =
          manualHideTransformationSteps(
              objects.get(natRule.getTranslatedSource()),
              objects.get(natRule.getTranslatedDestination()),
              objects.get(natRule.getTranslatedService()),
              warnings);
    } else {
      assert natRule.getMethod() == NatMethod.STATIC;
      maybeSteps = manualStaticTransformationSteps(natRule, objects, warnings);
    }
    return maybeSteps.map(steps -> always().apply(steps).build());
  }

  /**
   * Returns true if the given {@link HasNatSettings} has settings that represent an automatic NAT
   * rule that can be successfully converted. Otherwise files warnings as necessary.
   */
  static boolean isValidAutomaticRule(HasNatSettings hasNatSettings, Warnings warnings) {
    NatSettings natSettings = hasNatSettings.getNatSettings();
    if (!natSettings.getAutoRule()) {
      return false;
    }
    if (!"All".equals(natSettings.getInstallOn())) {
      // TODO Support installing NAT rules on specific gateways.
      // TODO What does it mean if install-on is missing?
      warnings.redFlag(
          String.format(
              "Automatic NAT rules on specific gateways are not yet supported: NAT settings on %s"
                  + " %s will be ignored",
              hasNatSettings.getClass(), hasNatSettings.getName()));
      return false;
    }
    if (natSettings.getMethod() == null) {
      // TODO What does null method mean?
      warnings.redFlag(
          String.format(
              "NAT settings on %s %s will be ignored: No NAT method set",
              hasNatSettings.getClass(), hasNatSettings.getName()));
      return false;
    }
    switch (natSettings.getMethod()) {
      case HIDE:
        return isValidAutomaticHideRule(hasNatSettings, warnings);
      case STATIC:
        return isValidAutomaticStaticRule(hasNatSettings, warnings);
      default:
        warnings.redFlag(
            String.format(
                "NAT method %s not recognized: NAT settings on %s %s will be ignored",
                natSettings.getMethod(), hasNatSettings.getClass(), hasNatSettings.getName()));
        return false;
    }
  }

  private static boolean isValidAutomaticHideRule(
      HasNatSettings hasNatSettings, Warnings warnings) {
    NatSettings natSettings = hasNatSettings.getNatSettings();
    assert natSettings.getAutoRule() && natSettings.getMethod() == NatMethod.HIDE;
    if (natSettings.getHideBehind() == null) {
      warnings.redFlag(
          String.format(
              "NAT settings on %s %s are invalid and will be ignored: type is HIDE, but hide-behind"
                  + " is missing",
              hasNatSettings.getClass(), hasNatSettings.getName()));
      return false;
    } else if (natSettings.getHideBehind() instanceof UnhandledNatHideBehind) {
      warnings.redFlag(
          String.format(
              "NAT hide-behind \"%s\" is not recognized: NAT settings on %s %s will be"
                  + " ignored",
              ((UnhandledNatHideBehind) natSettings.getHideBehind()).getName(),
              hasNatSettings.getClass(),
              hasNatSettings.getName()));
      return false;
    }
    return true;
  }

  private static boolean isValidAutomaticStaticRule(
      HasNatSettings hasNatSettings, Warnings warnings) {
    if (!(hasNatSettings instanceof Host)) {
      // TODO Support automatic static NAT on constructs other than hosts
      warnings.redFlag(
          String.format(
              "Automatic static NAT rules on non-host objects are not yet supported: NAT settings"
                  + " on %s %s will be ignored",
              hasNatSettings.getClass(), hasNatSettings.getName()));
      return false;
    } else if (((Host) hasNatSettings).getIpv4Address() == null) {
      // TODO Support IPv6
      return false;
    }
    NatSettings natSettings = hasNatSettings.getNatSettings();
    assert natSettings.getAutoRule() && natSettings.getMethod() == NatMethod.STATIC;
    if (natSettings.getIpv4Address() == null) {
      // TODO Support IPv6 NAT
      return false;
    }
    return true;
  }

  /**
   * Get the {@link TransformationStep} to apply to the traffic that matches the given {@link
   * HasNatSettings}. Assumes {@code hasNatSettings} has passed {@link
   * #isValidAutomaticRule(HasNatSettings, Warnings)} and that the matching traffic is not internal
   * (see {@link #matchInternalTraffic}).
   */
  static @Nonnull TransformationStep automaticHideRuleTransformationStep(
      HasNatSettings hasNatSettings) {
    NatSettings natSettings = hasNatSettings.getNatSettings();
    assert natSettings.getHideBehind() != null; // guaranteed by isValidAutomaticHideRule
    Ip transformedIp =
        new NatHideBehindVisitor<Ip>() {
          @Override
          public Ip visitNatHideBehindGateway(NatHideBehindGateway natHideBehindGateway) {
            // Need to use egress interface's IP to hide source.
            // This stand-in IP will be translated to the egress interface's IP on egress.
            return HIDE_BEHIND_GATEWAY_IP;
          }

          @Override
          public Ip visitNatHideBehindIp(NatHideBehindIp natHideBehindIp) {
            return natHideBehindIp.getIp();
          }

          @Override
          public Ip visitUnhandledNatHideBehind(UnhandledNatHideBehind unhandledNatHideBehind) {
            // isValidAutomaticHideRule guarantees hideBehind is not unhandled
            throw new IllegalArgumentException();
          }
        }.visit(natSettings.getHideBehind());
    return assignSourceIp(transformedIp);
  }

  /**
   * Automatic hide rules configured on certain objects that represent IP spaces (namely, {@link
   * Network} and {@link AddressRange}) do not apply to traffic whose source and destination are
   * both within that object's IP space. This function returns an {@link AclLineMatchExpr} that
   * matches such traffic if applicable for the given {@link HasNatSettings}, and otherwise an empty
   * optional.
   */
  static Optional<AclLineMatchExpr> matchInternalTraffic(
      HasNatSettings hasNatSettings, AddressSpaceToMatchExpr toMatchExprVisitor) {
    return new HasNatSettingsVisitor<Optional<AclLineMatchExpr>>() {
      @Override
      public Optional<AclLineMatchExpr> visitAddressRange(AddressRange addressRange) {
        return Optional.of(
            and(
                toMatchExprVisitor.convertSource(addressRange),
                toMatchExprVisitor.convertDest(addressRange)));
      }

      @Override
      public Optional<AclLineMatchExpr> visitHost(Host host) {
        return Optional.empty();
      }

      @Override
      public Optional<AclLineMatchExpr> visitNetwork(Network network) {
        return Optional.of(
            and(
                toMatchExprVisitor.convertSource(network),
                toMatchExprVisitor.convertDest(network)));
      }
    }.visit(hasNatSettings);
  }

  /**
   * Get the {@link AclLineMatchExpr} corresponding to the traffic that matches the given {@link
   * HasNatSettings}. Assumes {@code hasNatSettings} has passed {@link
   * #isValidAutomaticRule(HasNatSettings, Warnings)}.
   *
   * @param srcNat Whether this match condition is for applying a source NAT rule as opposed to a
   *     destination NAT rule. If true, it matches on the source IP being the rule's original IP;
   *     otherwise, it matches on the destination IP being the rule's translated IP.
   */
  static @Nonnull AclLineMatchExpr matchAutomaticStaticRule(
      HasNatSettings hasNatSettings, boolean srcNat) {
    if (srcNat) {
      Host host = (Host) hasNatSettings;
      assert host.getIpv4Address() != null;
      return matchSrc(host.getIpv4Address());
    }
    NatSettings natSettings = hasNatSettings.getNatSettings();
    assert natSettings.getIpv4Address() != null;
    return matchDst(natSettings.getIpv4Address());
  }

  /**
   * Get the {@link TransformationStep} corresponding to the traffic that matches the given {@link
   * HasNatSettings}. Assumes {@code hasNatSettings} has passed {@link
   * #isValidAutomaticRule(HasNatSettings, Warnings)}.
   *
   * @param srcNat Whether this transformation is for applying a source NAT rule as opposed to a
   *     destination NAT rule. If true, the return value will assign the source IP to the NAT
   *     settings translated IP; otherwise, it will assign the destination IP to the settings'
   *     original IP.
   */
  static @Nonnull TransformationStep automaticStaticRuleTransformationStep(
      HasNatSettings hasNatSettings, boolean srcNat) {
    if (srcNat) {
      NatSettings natSettings = hasNatSettings.getNatSettings();
      assert natSettings.getIpv4Address() != null;
      return assignSourceIp(natSettings.getIpv4Address());
    }
    Host host = (Host) hasNatSettings;
    assert host.getIpv4Address() != null;
    return assignDestinationIp(host.getIpv4Address());
  }

  /**
   * Given a VI {@link Interface} and a list of functions to generate transformations for it,
   * generates and returns those transformations.
   *
   * @param transformationFuncs A list of functions that take in the VI interface's IP and return a
   *     {@link Transformation} that should be applied to traffic exiting that interface. The
   *     resulting transformations will be merged such that the first matching transformation will
   *     be applied to outgoing traffic.
   */
  static List<Transformation> getOutgoingTransformations(
      Interface viIface, List<Function<Ip, Transformation>> transformationFuncs) {
    Ip ifaceIp =
        Optional.ofNullable(viIface.getConcreteAddress())
            .map(ConcreteInterfaceAddress::getIp)
            .orElse(null);
    if (ifaceIp != null) {
      return transformationFuncs.stream()
          .map(transformationFunc -> transformationFunc.apply(ifaceIp))
          .collect(ImmutableList.toImmutableList());
    }
    // Interface does not have an IP. These transformations do not apply.
    return ImmutableList.of();
  }

  /**
   * Given a list of {@link Transformation}, merges them into a single transformation which will
   * apply the first matching transformation in the list. Returns empty optional if list is empty.
   */
  static @Nonnull Optional<Transformation> mergeTransformations(
      List<Transformation> transformations) {
    if (transformations.isEmpty()) {
      return Optional.empty();
    }
    List<Transformation> reversedTransformations = Lists.reverse(transformations);
    Iterator<Transformation> i = reversedTransformations.iterator();
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
