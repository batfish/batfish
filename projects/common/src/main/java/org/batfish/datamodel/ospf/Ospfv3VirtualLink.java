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

  public static final int
      DEFAULT_HELLO_INTERVAL_SECONDS =
          10;

  public static final int
      DEFAULT_DEAD_INTERVAL_SECONDS =
          40;

  public static final int
      DEFAULT_RETRANSMIT_INTERVAL_SECONDS =
          5;

  public static final int
      DEFAULT_TRANSIT_DELAY_SECONDS =
          1;

  private static final String PROP_TRANSIT_AREA =
      "transitArea";

  private static final String PROP_PEER_ROUTER_ID =
      "peerRouterId";

  private static final String PROP_AUTHENTICATION =
      "authentication";

  private static final String PROP_ENCRYPTION =
      "encryption";

  private static final String
      PROP_HELLO_INTERVAL_SECONDS =
          "helloIntervalSeconds";

  private static final String
      PROP_DEAD_INTERVAL_SECONDS =
          "deadIntervalSeconds";

  private static final String
      PROP_RETRANSMIT_INTERVAL_SECONDS =
          "retransmitIntervalSeconds";

  private static final String
      PROP_TRANSIT_DELAY_SECONDS =
          "transitDelaySeconds";

  /**
   * Construct a virtual link using default timers and no IPsec security.
   *
   * <p>This overload preserves the original virtual-link API.
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
   * Construct a virtual link with optional AH authentication and default
   * timers.
   *
   * <p>This overload preserves the API introduced by virtual-link
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

  /**
   * Construct a virtual link with optional AH/ESP security and default
   * timers.
   *
   * <p>This overload preserves the API introduced by virtual-link ESP
   * encryption support.
   */
  public Ospfv3VirtualLink(
      @Nullable Long transitArea,
      @Nullable Ip peerRouterId,
      @Nullable Ospfv3Authentication authentication,
      @Nullable Ospfv3Encryption encryption) {

    this(
        transitArea,
        peerRouterId,
        authentication,
        encryption,
        DEFAULT_HELLO_INTERVAL_SECONDS,
        DEFAULT_DEAD_INTERVAL_SECONDS,
        DEFAULT_RETRANSMIT_INTERVAL_SECONDS,
        DEFAULT_TRANSIT_DELAY_SECONDS);
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
          @Nullable Ospfv3Encryption encryption,
      @JsonProperty(PROP_HELLO_INTERVAL_SECONDS)
          @Nullable Integer helloIntervalSeconds,
      @JsonProperty(PROP_DEAD_INTERVAL_SECONDS)
          @Nullable Integer deadIntervalSeconds,
      @JsonProperty(PROP_RETRANSMIT_INTERVAL_SECONDS)
          @Nullable Integer retransmitIntervalSeconds,
      @JsonProperty(PROP_TRANSIT_DELAY_SECONDS)
          @Nullable Integer transitDelaySeconds) {

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

    int effectiveHelloInterval =
        helloIntervalSeconds == null
            ? DEFAULT_HELLO_INTERVAL_SECONDS
            : helloIntervalSeconds;

    int effectiveDeadInterval =
        deadIntervalSeconds == null
            ? DEFAULT_DEAD_INTERVAL_SECONDS
            : deadIntervalSeconds;

    int effectiveRetransmitInterval =
        retransmitIntervalSeconds == null
            ? DEFAULT_RETRANSMIT_INTERVAL_SECONDS
            : retransmitIntervalSeconds;

    int effectiveTransitDelay =
        transitDelaySeconds == null
            ? DEFAULT_TRANSIT_DELAY_SECONDS
            : transitDelaySeconds;

    checkArgument(
        effectiveHelloInterval >= 1
            && effectiveHelloInterval <= 65535,
        "Invalid OSPFv3 virtual-link hello interval %s",
        effectiveHelloInterval);

    checkArgument(
        effectiveDeadInterval >= 1
            && effectiveDeadInterval <= 65535,
        "Invalid OSPFv3 virtual-link dead interval %s",
        effectiveDeadInterval);

    checkArgument(
        effectiveRetransmitInterval >= 1
            && effectiveRetransmitInterval <= 3600,
        "Invalid OSPFv3 virtual-link retransmit interval %s",
        effectiveRetransmitInterval);

    checkArgument(
        effectiveTransitDelay >= 1
            && effectiveTransitDelay <= 3600,
        "Invalid OSPFv3 virtual-link transit delay %s",
        effectiveTransitDelay);

    _transitArea =
        transitArea;

    _peerRouterId =
        peerRouterId;

    _authentication =
        authentication;

    _encryption =
        encryption;

    _helloIntervalSeconds =
        effectiveHelloInterval;

    _deadIntervalSeconds =
        effectiveDeadInterval;

    _retransmitIntervalSeconds =
        effectiveRetransmitInterval;

    _transitDelaySeconds =
        effectiveTransitDelay;
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

  @JsonProperty(PROP_HELLO_INTERVAL_SECONDS)
  public int getHelloIntervalSeconds() {

    return _helloIntervalSeconds;
  }

  @JsonProperty(PROP_DEAD_INTERVAL_SECONDS)
  public int getDeadIntervalSeconds() {

    return _deadIntervalSeconds;
  }

  @JsonProperty(PROP_RETRANSMIT_INTERVAL_SECONDS)
  public int getRetransmitIntervalSeconds() {

    return _retransmitIntervalSeconds;
  }

  @JsonProperty(PROP_TRANSIT_DELAY_SECONDS)
  public int getTransitDelaySeconds() {

    return _transitDelaySeconds;
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
            rhs._encryption)
        && _helloIntervalSeconds
            == rhs._helloIntervalSeconds
        && _deadIntervalSeconds
            == rhs._deadIntervalSeconds
        && _retransmitIntervalSeconds
            == rhs._retransmitIntervalSeconds
        && _transitDelaySeconds
            == rhs._transitDelaySeconds;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _transitArea,
        _peerRouterId,
        _authentication,
        _encryption,
        _helloIntervalSeconds,
        _deadIntervalSeconds,
        _retransmitIntervalSeconds,
        _transitDelaySeconds);
  }

  private final long _transitArea;

  private final @Nonnull Ip _peerRouterId;

  private final @Nullable Ospfv3Authentication
      _authentication;

  private final @Nullable Ospfv3Encryption
      _encryption;

  private final int _helloIntervalSeconds;

  private final int _deadIntervalSeconds;

  private final int _retransmitIntervalSeconds;

  private final int _transitDelaySeconds;
}
