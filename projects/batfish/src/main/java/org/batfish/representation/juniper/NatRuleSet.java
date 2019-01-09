package org.batfish.representation.juniper;

import static org.batfish.datamodel.transformation.Transformation.when;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.Builder;

/** Represents a Juniper nat rule set */
@ParametersAreNonnullByDefault
public final class NatRuleSet implements Serializable, Comparable<NatRuleSet> {

  private static final long serialVersionUID = 1L;

  private final NatPacketLocation _fromLocation;

  private final List<NatRule> _rules;

  private final NatPacketLocation _toLocation;

  private final String _name;

  public NatRuleSet(String name) {
    _name = name;
    _fromLocation = new NatPacketLocation();
    _rules = new ArrayList<>();
    _toLocation = new NatPacketLocation();
  }

  @Nonnull
  public NatPacketLocation getFromLocation() {
    return _fromLocation;
  }

  @Nonnull
  public List<NatRule> getRules() {
    return _rules;
  }

  @Nonnull
  public NatPacketLocation getToLocation() {
    return _toLocation;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  /* Sort NatRuleSet according to the following order:
   * 1. Source interface/destination interface
   * 2. Source zone/destination interface
   * 3. Source routing instance/destination interface
   * 4. Source interface/destination zone
   * 5. Source zone/destination zone
   * 6. Source routing instance/destination zone
   * 7. Source interface/destination routing instance
   * 8. Source zone/destination routing instance
   * 9. Source routing instance/destination routing instance
   */
  private static final Comparator<NatRuleSet> COMPARATOR =
      Comparator.comparing(NatRuleSet::getToLocation).thenComparing(NatRuleSet::getFromLocation);

  @Override
  public int compareTo(NatRuleSet o) {
    return COMPARATOR.compare(this, o);
  }

  /**
   * Convert to an outgoing {@link Transformation} in the vendor-independent model. The outgoing
   * transformation is installed on the egress interface, so we need to encode constraints on the
   * ingress interface using {@link AclLineMatchExpr ACL line match expressions}.
   *
   * @param matchFromLocationExprs The {@link AclLineMatchExpr} to match traffic from each {@link
   *     NatPacketLocation}.
   * @param andThen The next {@link Transformation} to apply after any {@link NatRule} matches.
   * @param orElse The next {@link Transformation} to apply if no {@link NatRule} matches.
   */
  public Optional<Transformation> toOutgoingTransformation(
      TransformationType type,
      IpField ipField,
      Map<String, NatPool> pools,
      Map<NatPacketLocation, AclLineMatchExpr> matchFromLocationExprs,
      @Nullable Transformation andThen,
      @Nullable Transformation orElse) {

    AclLineMatchExpr matchFromLocation = matchFromLocationExprs.get(_fromLocation);

    if (matchFromLocation == null) {
      // non-existent NatPacketLocation
      return Optional.empty();
    }

    return rulesTransformation(type, ipField, pools, andThen, orElse)
        .map(
            rulesTransformation ->
                when(matchFromLocation)
                    .apply(new Noop(type))
                    .setAndThen(rulesTransformation)
                    .setOrElse(orElse)
                    .build());
  }

  /**
   * Convert to an incoming {@link Transformation} in the vendor-independent model. Since the
   * transformation is installed on the ingress interface, we don't need to use an {@link
   * AclLineMatchExpr} to match it.
   */
  public Optional<Transformation> toIncomingTransformation(
      TransformationType type,
      IpField ipField,
      Map<String, NatPool> pools,
      @Nullable Transformation andThen,
      @Nullable Transformation orElse) {
    return rulesTransformation(type, ipField, pools, andThen, orElse);
  }

  private Optional<Transformation> rulesTransformation(
      TransformationType type,
      IpField ipField,
      Map<String, NatPool> pools,
      @Nullable Transformation andThen,
      @Nullable Transformation orElse) {
    Transformation transformation = orElse;
    for (NatRule rule : Lists.reverse(_rules)) {
      Optional<Builder> optionalBuilder = rule.toTransformationBuilder(type, ipField, pools);
      if (optionalBuilder.isPresent()) {
        transformation =
            optionalBuilder.get().setAndThen(andThen).setOrElse(transformation).build();
      }
    }
    return Optional.ofNullable(transformation);
  }
}
