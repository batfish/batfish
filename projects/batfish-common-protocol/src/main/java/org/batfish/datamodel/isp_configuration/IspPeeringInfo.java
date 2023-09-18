package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Specification for peering between two modeled ISPs. It allows for specifying the two ISPs by
 * ASNs. Batfish will create a direct L3 link between the ISPs and a BGP session that does not
 * filter any routes.
 */
@ParametersAreNonnullByDefault
public class IspPeeringInfo {
  private static final String PROP_PEER1 = "peer1";
  private static final String PROP_PEER2 = "peer2";

  public static class Peer {
    private static final String PROP_ASN = "asn";

    private final long _asn;

    public Peer(long asn) {
      _asn = asn;
    }

    @JsonProperty(PROP_ASN)
    public long getAsn() {
      return _asn;
    }

    @JsonCreator
    private static Peer jsonCreator(@JsonProperty(PROP_ASN) @Nullable Long asn) {
      checkArgument(asn != null, "Missing %", PROP_ASN);
      return new Peer(asn);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Peer)) {
        return false;
      }
      Peer that = (Peer) o;
      return _asn == that._asn;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_asn);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("asn", _asn).toString();
    }
  }

  private final @Nonnull Peer _peer1;
  private final @Nonnull Peer _peer2;

  public IspPeeringInfo(Peer peer1, Peer peer2) {
    checkArgument(
        peer1._asn != peer2._asn, "Same ASN (%s) for both ISPs is not supported", peer1._asn);
    _peer1 = peer1;
    _peer2 = peer2;
  }

  @JsonCreator
  private static IspPeeringInfo jsonCreator(
      @JsonProperty(PROP_PEER1) @Nullable Peer peer1,
      @JsonProperty(PROP_PEER2) @Nullable Peer peer2) {
    checkArgument(peer1 != null, "Missing %", PROP_PEER1);
    checkArgument(peer2 != null, "Missing %", PROP_PEER2);
    return new IspPeeringInfo(peer1, peer2);
  }

  @JsonProperty(PROP_PEER1)
  public Peer getPeer1() {
    return _peer1;
  }

  @JsonProperty(PROP_PEER2)
  public Peer getPeer2() {
    return _peer2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspPeeringInfo)) {
      return false;
    }
    IspPeeringInfo that = (IspPeeringInfo) o;
    return _peer1.equals(that._peer1) && _peer2.equals(that._peer2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_peer1, _peer2);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("peer1", _peer1).add("peer2", _peer2).toString();
  }
}
