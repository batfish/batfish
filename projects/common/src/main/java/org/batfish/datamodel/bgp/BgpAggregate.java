package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/**
 * Configuration for an aggregate network to be generated in the BGP RIB in the presence of one or
 * more contributing networks.
 */
@ParametersAreNonnullByDefault
public final class BgpAggregate implements Serializable {

  public static @Nonnull BgpAggregate of(
      Prefix network,
      @Nullable String suppressionPolicy,
      @Nullable String generationPolicy,
      @Nullable String attributePolicy) {
    return new BgpAggregate(attributePolicy, generationPolicy, network, suppressionPolicy);
  }

  /**
   * If present, this policy should be used to transform the generated BGP aggregate after it has
   * been activated by at least one valid contributing route. It should be applied after all
   * applications of the policy returned by {@link #getGenerationPolicy}.
   *
   * <p>To set vendor-specific default properties of the generated aggregate, apply them here before
   * calling the user's attribute policy if present.
   *
   * <p>(TODO: this should probably be in a pre-generation-policy?)
   */
  @JsonProperty(PROP_ATTRIBUTE_POLICY)
  public @Nullable String getAttributePolicy() {
    return _attributePolicy;
  }

  /**
   * This policy is meant to be applied to each potential contributor to the aggregate network
   * following the suppression policy. In each application, the potential contributor should be thue
   * input, and the BGP aggregate route should be the output. Transformations in this policy should
   * be cumulatively applied to the output aggregate. If any input potential contributor is
   * permitted by the policy, then the aggregate should be activated. Transformations applied to the
   * aggregate for potential contributors that are rejected by the policy should be discarded. The
   * suppressed status of the output route following execution of this policy shall indicate whether
   * the input contributing route should be suppressed. Like transformations, the suppressed status
   * of the output route should be ignored if the input route is not permitted.
   *
   * <p>If absent, all potential contributors should be treated as actual contributors.
   *
   * <p>NOTE: Current limitations:
   *
   * <ul>
   *   <li>Transformations are currently ignored in iBDP.
   *   <li>Suppressed status is not currently implemented.
   * </ul>
   */
  @JsonProperty(PROP_GENERATION_POLICY)
  public @Nullable String getGenerationPolicy() {
    return _generationPolicy;
  }

  /**
   * The network of the aggregate. Potential contributors are those routes whose network is more
   * specific than this one.
   */
  @JsonProperty(PROP_NETWORK)
  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  /**
   * This policy should be used to determine which potential contributors are nominally suppressed.
   * The suppressed status determined by this policy may be overridden by generation policy, but
   * only when {@link #getGenerationPolicy} accepts the potential contributor.
   *
   * <p>If absent, no routes are suppressed.
   *
   * <p>NOTE: Currently, iBDP does not implement suppression policy.
   */
  @JsonProperty(PROP_SUPPRESSION_POLICY)
  public @Nullable String getSuppressionPolicy() {
    return _suppressionPolicy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BgpAggregate)) {
      return false;
    }
    BgpAggregate that = (BgpAggregate) o;
    return Objects.equals(_attributePolicy, that._attributePolicy)
        && Objects.equals(_generationPolicy, that._generationPolicy)
        && _network.equals(that._network)
        && Objects.equals(_suppressionPolicy, that._suppressionPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_attributePolicy, _generationPolicy, _network, _suppressionPolicy);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add(PROP_ATTRIBUTE_POLICY, _attributePolicy)
        .add(PROP_GENERATION_POLICY, _generationPolicy)
        .add(PROP_NETWORK, _network)
        .add(PROP_SUPPRESSION_POLICY, _suppressionPolicy)
        .toString();
  }

  private static final String PROP_ATTRIBUTE_POLICY = "attributePolicy";
  private static final String PROP_GENERATION_POLICY = "generationPolicy";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_SUPPRESSION_POLICY = "suppressionPolicy";

  private final @Nullable String _attributePolicy;
  private final @Nullable String _generationPolicy;
  private final @Nonnull Prefix _network;
  private final @Nullable String _suppressionPolicy;

  @JsonCreator
  private static @Nonnull BgpAggregate create(
      @JsonProperty(PROP_ATTRIBUTE_POLICY) @Nullable String attributePolicy,
      @JsonProperty(PROP_GENERATION_POLICY) @Nullable String generationPolicy,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_SUPPRESSION_POLICY) @Nullable String suppressionPolicy) {
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    return of(network, suppressionPolicy, generationPolicy, attributePolicy);
  }

  private BgpAggregate(
      @Nullable String attributePolicy,
      @Nullable String generationPolicy,
      Prefix network,
      @Nullable String suppressionPolicy) {
    _attributePolicy = attributePolicy;
    _generationPolicy = generationPolicy;
    _network = network;
    _suppressionPolicy = suppressionPolicy;
  }
}
