package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
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

  /**
   * How the AS_PATH of a locally-generated aggregate is derived from its contributing routes.
   *
   * <p>The default {@link #NONE} matches vendors that generate an empty AS_PATH for aggregates
   * (e.g. Cisco IOS/NX-OS/XR without {@code as-set}). Arista EOS includes the contributors' common
   * leading AS_SEQUENCE, modeled by {@link #COMMON_SEQUENCE}.
   */
  public enum AsPathMode {
    /** Empty AS_PATH. */
    NONE,
    /**
     * Longest common leading AS_SEQUENCE of the contributors, with the divergent tail dropped
     * (matching Arista EOS and RFC 4271 aggregation without {@code as-set}).
     */
    COMMON_SEQUENCE,
  }

  public static @Nonnull BgpAggregate of(
      Prefix network,
      @Nullable String suppressionPolicy,
      @Nullable String generationPolicy,
      @Nullable String attributePolicy) {
    return of(network, suppressionPolicy, generationPolicy, attributePolicy, true, AsPathMode.NONE);
  }

  public static @Nonnull BgpAggregate of(
      Prefix network,
      @Nullable String suppressionPolicy,
      @Nullable String generationPolicy,
      @Nullable String attributePolicy,
      boolean installInMainRib) {
    return of(
        network,
        suppressionPolicy,
        generationPolicy,
        attributePolicy,
        installInMainRib,
        AsPathMode.NONE);
  }

  public static @Nonnull BgpAggregate of(
      Prefix network,
      @Nullable String suppressionPolicy,
      @Nullable String generationPolicy,
      @Nullable String attributePolicy,
      boolean installInMainRib,
      AsPathMode asPathMode) {
    return new BgpAggregate(
        attributePolicy,
        generationPolicy,
        network,
        suppressionPolicy,
        installInMainRib,
        asPathMode);
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

  /**
   * Whether the generated aggregate should be installed in the main RIB once activated. If {@code
   * false}, the aggregate is generated in the BGP RIB (and thus advertised to peers) but not
   * installed in the main RIB. This models {@code advertise-only} on Arista EOS.
   *
   * <p>Defaults to {@code true}.
   */
  @JsonProperty(PROP_INSTALL_IN_MAIN_RIB)
  public boolean getInstallInMainRib() {
    return _installInMainRib;
  }

  /**
   * How the AS_PATH of the generated aggregate is derived from its contributors. See {@link
   * AsPathMode}. Defaults to {@link AsPathMode#NONE}.
   */
  @JsonProperty(PROP_AS_PATH_MODE)
  public @Nonnull AsPathMode getAsPathMode() {
    return _asPathMode;
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
        && Objects.equals(_suppressionPolicy, that._suppressionPolicy)
        && _installInMainRib == that._installInMainRib
        && _asPathMode == that._asPathMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _attributePolicy,
        _generationPolicy,
        _network,
        _suppressionPolicy,
        _installInMainRib,
        _asPathMode.ordinal());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add(PROP_ATTRIBUTE_POLICY, _attributePolicy)
        .add(PROP_GENERATION_POLICY, _generationPolicy)
        .add(PROP_NETWORK, _network)
        .add(PROP_SUPPRESSION_POLICY, _suppressionPolicy)
        .add(PROP_INSTALL_IN_MAIN_RIB, _installInMainRib)
        .add(PROP_AS_PATH_MODE, _asPathMode)
        .toString();
  }

  private static final String PROP_ATTRIBUTE_POLICY = "attributePolicy";
  private static final String PROP_GENERATION_POLICY = "generationPolicy";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_SUPPRESSION_POLICY = "suppressionPolicy";
  private static final String PROP_INSTALL_IN_MAIN_RIB = "installInMainRib";
  private static final String PROP_AS_PATH_MODE = "asPathMode";

  private final @Nullable String _attributePolicy;
  private final @Nullable String _generationPolicy;
  private final @Nonnull Prefix _network;
  private final @Nullable String _suppressionPolicy;
  private final boolean _installInMainRib;
  private final @Nonnull AsPathMode _asPathMode;

  @JsonCreator
  private static @Nonnull BgpAggregate create(
      @JsonProperty(PROP_ATTRIBUTE_POLICY) @Nullable String attributePolicy,
      @JsonProperty(PROP_GENERATION_POLICY) @Nullable String generationPolicy,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_SUPPRESSION_POLICY) @Nullable String suppressionPolicy,
      @JsonProperty(PROP_INSTALL_IN_MAIN_RIB) @Nullable Boolean installInMainRib,
      @JsonProperty(PROP_AS_PATH_MODE) @Nullable AsPathMode asPathMode) {
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    return of(
        network,
        suppressionPolicy,
        generationPolicy,
        attributePolicy,
        // Default to true for backward compatibility with older serialized aggregates.
        installInMainRib == null || installInMainRib,
        // Default to NONE for backward compatibility with older serialized aggregates.
        firstNonNull(asPathMode, AsPathMode.NONE));
  }

  private BgpAggregate(
      @Nullable String attributePolicy,
      @Nullable String generationPolicy,
      Prefix network,
      @Nullable String suppressionPolicy,
      boolean installInMainRib,
      AsPathMode asPathMode) {
    _attributePolicy = attributePolicy;
    _generationPolicy = generationPolicy;
    _network = network;
    _suppressionPolicy = suppressionPolicy;
    _installInMainRib = installInMainRib;
    _asPathMode = asPathMode;
  }
}
