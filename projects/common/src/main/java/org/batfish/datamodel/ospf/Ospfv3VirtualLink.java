package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** One OSPFv3 virtual link through a non-backbone transit area. */
@ParametersAreNonnullByDefault
public final class Ospfv3VirtualLink
    implements Serializable {

  private static final String PROP_TRANSIT_AREA =
      "transitArea";

  private static final String PROP_PEER_ROUTER_ID =
      "peerRouterId";

  private static final String PROP_AUTHENTICATION =
      "authentication";

  private static final String PROP_ENCRYPTION =
      "encryption";

  /**
   * Construct a virtual link without IPsec security.
   *
   * <p>This overload preserves the API used by existing callers.
   */
  public Ospfv3VirtualLink(
      long transitArea,
      Ip peerRouterId) {

    this(
        transitArea,
        peerRouterId,
        null,
        null);
  }

  /**
   * Construct a virtual link with optional AH authentication and no ESP.
   *
   * <p>This overload preserves the API introduced by OSPFv3 virtual-link
   * authentication support.
   */
  public Ospfv3VirtualLink(
      long transitArea,
      Ip peerRouterId,
      @Nullable Ospfv3Authentication authentication) {

    this(
        transitArea,
        peerRouterId,
        authentication,
        null);
  }

  @JsonCreator
  public Ospfv3VirtualLink(
      @JsonProperty(PROP_TRANSIT_AREA)
          @Nullable Long transitArea,
      @JsonProperty(PROP_PEER_ROUTER_ID)
          @Nullable Ip peerRouterId,
      @JsonProperty(PROP_AUTHENTICATION)
          @Nullable Ospfv3Authentication authentication,
      @JsonProperty(PROP_ENCRYPTION)
          @Nullable Ospfv3Encryption encryption) {

    checkArgument(
        transitArea != null,
        "Missing OSPFv3 virtual-link transit area");

    checkArgument(
        transitArea >= 0L
            && transitArea <= 0xFFFFFFFFL,
        "Invalid OSPFv3 virtual-link transit area %s",
        transitArea);

    checkArgument(
        transitArea != 0L,
        "OSPFv3 virtual-link transit area cannot be area 0");

    checkArgument(
        peerRouterId != null,
        "Missing OSPFv3 virtual-link peer router ID");

    _transitArea =
        transitArea;

    _peerRouterId =
        peerRouterId;

    _authentication =
        authentication;

    _encryption =
        encryption;
  }

  @JsonProperty(PROP_TRANSIT_AREA)
  public long getTransitArea() {
    return _transitArea;
  }

  @JsonProperty(PROP_PEER_ROUTER_ID)
  public @Nonnull Ip getPeerRouterId() {
    return _peerRouterId;
  }

  @JsonProperty(PROP_AUTHENTICATION)
  public @Nullable Ospfv3Authentication
      getAuthentication() {

    return _authentication;
  }

  @JsonProperty(PROP_ENCRYPTION)
  public @Nullable Ospfv3Encryption
      getEncryption() {

    return _encryption;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Ospfv3VirtualLink)) {
      return false;
    }

    Ospfv3VirtualLink rhs =
        (Ospfv3VirtualLink) o;

    return _transitArea == rhs._transitArea
        && _peerRouterId.equals(
            rhs._peerRouterId)
        && Objects.equals(
            _authentication,
            rhs._authentication)
        && Objects.equals(
            _encryption,
            rhs._encryption);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _transitArea,
        _peerRouterId,
        _authentication,
        _encryption);
  }

  private final long _transitArea;
  private final @Nonnull Ip _peerRouterId;
  private final @Nullable Ospfv3Authentication
      _authentication;
  private final @Nullable Ospfv3Encryption
      _encryption;
}
