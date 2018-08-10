package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Uniquely identifies a {@link BgpPeerConfig} in a network. */
public final class BgpPeerConfigId implements Comparable<BgpPeerConfigId> {
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_VRF_NAME = "vrf";
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_DYNAMIC = "dynamic";
  private final String _hostname;
  private final String _vrfName;
  private final Prefix _remotePeerPrefix;
  private final boolean _dynamic;

  /** Create a new ID. */
  public BgpPeerConfigId(
      @Nonnull String hostname,
      @Nonnull String vrfName,
      @Nonnull Prefix remotePeerPrefix,
      boolean dynamic) {
    _hostname = hostname;
    _vrfName = vrfName;
    _remotePeerPrefix = remotePeerPrefix;
    _dynamic = dynamic;
  }

  @JsonCreator
  private static BgpPeerConfigId createNewConfigId(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_VRF_NAME) @Nullable String vrfName,
      @JsonProperty(PROP_PREFIX) @Nullable Prefix remotePeerPrefix,
      @JsonProperty(PROP_DYNAMIC) boolean dynamic) {
    return new BgpPeerConfigId(
        requireNonNull(hostname),
        requireNonNull(vrfName),
        requireNonNull(remotePeerPrefix),
        dynamic);
  }

  @Nonnull
  @JsonProperty(PROP_HOSTNAME)
  public String getHostname() {
    return _hostname;
  }

  @Nonnull
  @JsonProperty(PROP_VRF_NAME)
  public String getVrfName() {
    return _vrfName;
  }

  @Nonnull
  @JsonProperty(PROP_PREFIX)
  public Prefix getRemotePeerPrefix() {
    return _remotePeerPrefix;
  }

  @JsonProperty(PROP_DYNAMIC)
  public boolean isDynamic() {
    return _dynamic;
  }

  @Override
  public int compareTo(@Nonnull BgpPeerConfigId o) {
    return Comparator.comparing(BgpPeerConfigId::getHostname)
        .thenComparing(BgpPeerConfigId::getVrfName)
        .thenComparing(BgpPeerConfigId::getRemotePeerPrefix)
        .thenComparing(BgpPeerConfigId::isDynamic)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpPeerConfigId)) {
      return false;
    }
    BgpPeerConfigId other = (BgpPeerConfigId) o;
    return Objects.equals(_hostname, other._hostname)
        && Objects.equals(_vrfName, other._vrfName)
        && Objects.equals(_remotePeerPrefix, other._remotePeerPrefix)
        && _dynamic == other._dynamic;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vrfName, _remotePeerPrefix, _dynamic);
  }
}
