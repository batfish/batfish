package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

/**
 * Tracks whether a route with a particular prefix and optionally matching one of a set of protocols
 * is in the main RIB of a particular VRF.
 */
public final class TrackRoute implements TrackMethod {

  @Override
  public <R> R accept(GenericTrackMethodVisitor<R> visitor) {
    return visitor.visitTrackRoute(this);
  }

  /** The prefix a tracked route must have. */
  @JsonProperty(PROP_PREFIX)
  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  /**
   * If empty, do not match protocols. Else the protocol of the route to be matched must be present
   * in the returned set.
   */
  @JsonIgnore
  public @Nonnull Set<RoutingProtocol> getProtocols() {
    return _protocols;
  }

  @JsonProperty(PROP_PROTOCOLS)
  private @Nonnull SortedSet<RoutingProtocol> getProtocolsSorted() {
    return ImmutableSortedSet.copyOf(_protocols);
  }

  /** The vrf whose main RIB should be checked for a tracked route. */
  @JsonProperty(PROP_VRF)
  public @Nonnull String getVrf() {
    return _vrf;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof TrackRoute)) {
      return false;
    }
    TrackRoute that = (TrackRoute) o;
    return _prefix.equals(that._prefix)
        && _protocols.equals(that._protocols)
        && _vrf.equals(that._vrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _protocols, _vrf);
  }

  public static @Nonnull TrackRoute of(Prefix prefix, Set<RoutingProtocol> protocols, String vrf) {
    return new TrackRoute(prefix, protocols, vrf);
  }

  @JsonCreator
  private static @Nonnull TrackRoute create(
      @JsonProperty(PROP_PREFIX) @Nullable Prefix prefix,
      @JsonProperty(PROP_PROTOCOLS) @Nullable Set<RoutingProtocol> protocols,
      @JsonProperty(PROP_VRF) @Nullable String vrf) {
    checkArgument(prefix != null, "Missing %s", PROP_PREFIX);
    checkArgument(vrf != null, "Missing %s", PROP_VRF);
    return new TrackRoute(
        prefix, ImmutableSet.copyOf(firstNonNull(protocols, ImmutableSet.of())), vrf);
  }

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_PROTOCOLS = "protocols";
  private static final String PROP_VRF = "vrf";

  private final @Nonnull Prefix _prefix;
  private final @Nonnull Set<RoutingProtocol> _protocols;
  private final @Nonnull String _vrf;

  private TrackRoute(Prefix prefix, Set<RoutingProtocol> protocols, String vrf) {
    _prefix = prefix;
    _protocols = protocols;
    _vrf = vrf;
  }
}
