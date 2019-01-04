package org.batfish.representation.juniper;

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
import org.batfish.datamodel.transformation.IpField;
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
   * Convert to a vendor-independent {@link Transformation}.
   *
   * @param andThen The next {@link Transformation} to apply after any {@link NatRule} matches and
   *     is applied.
   */
  public Optional<Transformation> toTransformation(
      IpField ipField, Map<String, NatPool> pools, @Nullable Transformation andThen) {
    Transformation transformation = null;
    for (NatRule rule : Lists.reverse(_rules)) {
      Optional<Builder> optionalBuilder = rule.toTransformationBuilder(ipField, pools);
      if (optionalBuilder.isPresent()) {
        transformation =
            optionalBuilder.get().setAndThen(andThen).setOrElse(transformation).build();
      }
    }
    return Optional.ofNullable(transformation);
  }
}
