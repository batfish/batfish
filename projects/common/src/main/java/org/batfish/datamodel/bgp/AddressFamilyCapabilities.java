package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.NEVER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Encapsulates fine-grained configuration of a BGP peer capabilities for a single {@link
 * AddressFamily}. These capabilities control route propagation between two BGP neighbors.
 *
 * <p>For example, in order for BGP add-path fean, both neighbors must have {@link
 * #getAdditionalPathsSend()} and {@link #getAdditionalPathsReceive()} enabled.
 */
@ParametersAreNonnullByDefault
public final class AddressFamilyCapabilities implements Serializable {
  private static final String PROP_ADDITIONAL_PATHS_RECEIVE = "additionalPathsReceive";
  private static final String PROP_ADDITIONAL_PATHS_SELECT_ALL = "additionalPathsSelectAll";
  private static final String PROP_ADDITIONAL_PATHS_SEND = "additionalPathsSend";
  private static final String PROP_ADVERTISE_EXTERNAL = "advertiseExternal";
  private static final String PROP_ADVERTISE_INACTIVE = "advertiseInactive";
  private static final String PROP_ALLOW_LOCAL_AS_IN = "allowLocalAsIn";
  private static final String PROP_ALLOW_REMOTE_AS_OUT = "allowRemoteAsOut";
  private static final String PROP_SEND_COMMUNITY = "sendCommunity";
  private static final String PROP_SEND_EXTENDED_COMMUNITY = "sendExtendedCommunity";

  private final boolean _additionalPathsReceive;
  private final boolean _additionalPathsSelectAll;
  private final boolean _additionalPathsSend;
  private final boolean _advertiseExternal;
  private final boolean _advertiseInactive;
  private final boolean _allowLocalAsIn;
  private final @Nonnull AllowRemoteAsOutMode _allowRemoteAsOut;
  private final boolean _sendCommunity;
  private final boolean _sendExtendedCommunity;

  private AddressFamilyCapabilities(
      boolean additionalPathsReceive,
      boolean additionalPathsSelectAll,
      boolean additionalPathsSend,
      boolean advertiseExternal,
      boolean advertiseInactive,
      boolean allowLocalAsIn,
      AllowRemoteAsOutMode allowRemoteAsOut,
      boolean sendCommunity,
      boolean sendExtendedCommunity) {
    _additionalPathsReceive = additionalPathsReceive;
    _additionalPathsSelectAll = additionalPathsSelectAll;
    _additionalPathsSend = additionalPathsSend;
    _advertiseExternal = advertiseExternal;
    _advertiseInactive = advertiseInactive;
    _allowLocalAsIn = allowLocalAsIn;
    _allowRemoteAsOut = allowRemoteAsOut;
    _sendCommunity = sendCommunity;
    _sendExtendedCommunity = sendExtendedCommunity;
  }

  @JsonCreator
  private static AddressFamilyCapabilities jsonCreator(
      @JsonProperty(PROP_ADDITIONAL_PATHS_RECEIVE) @Nullable Boolean additionalPathsReceive,
      @JsonProperty(PROP_ADDITIONAL_PATHS_SELECT_ALL) @Nullable Boolean additionalPathsSelectAll,
      @JsonProperty(PROP_ADDITIONAL_PATHS_SEND) @Nullable Boolean additionalPathsSend,
      @JsonProperty(PROP_ADVERTISE_EXTERNAL) @Nullable Boolean advertiseExternal,
      @JsonProperty(PROP_ADVERTISE_INACTIVE) @Nullable Boolean advertiseInactive,
      @JsonProperty(PROP_ALLOW_LOCAL_AS_IN) @Nullable Boolean allowLocalAsIn,
      @JsonProperty(PROP_ALLOW_REMOTE_AS_OUT) @Nullable AllowRemoteAsOutMode allowRemoteAsOut,
      @JsonProperty(PROP_SEND_COMMUNITY) @Nullable Boolean sendCommunity,
      @JsonProperty(PROP_SEND_EXTENDED_COMMUNITY) @Nullable Boolean sendExtendedCommunity) {

    checkArgument(additionalPathsReceive != null, "Missing %s", PROP_ADDITIONAL_PATHS_RECEIVE);
    checkArgument(additionalPathsSelectAll != null, "Missing %s", PROP_ADDITIONAL_PATHS_SELECT_ALL);
    checkArgument(additionalPathsSend != null, "Missing %s", PROP_ADDITIONAL_PATHS_SEND);
    checkArgument(advertiseExternal != null, "Missing %s", PROP_ADVERTISE_EXTERNAL);
    checkArgument(advertiseInactive != null, "Missing %s", PROP_ADVERTISE_INACTIVE);
    checkArgument(allowLocalAsIn != null, "Missing %s", PROP_ALLOW_LOCAL_AS_IN);
    checkArgument(allowRemoteAsOut != null, "Missing %s", PROP_ALLOW_REMOTE_AS_OUT);
    checkArgument(sendCommunity != null, "Missing %s", PROP_SEND_COMMUNITY);
    checkArgument(sendExtendedCommunity != null, "Missing %s", PROP_SEND_COMMUNITY);
    return builder()
        .setAdditionalPathsReceive(additionalPathsReceive)
        .setAdditionalPathsSelectAll(additionalPathsSelectAll)
        .setAdditionalPathsSend(additionalPathsSend)
        .setAdvertiseExternal(advertiseExternal)
        .setAdvertiseInactive(advertiseInactive)
        .setAllowLocalAsIn(allowLocalAsIn)
        .setAllowRemoteAsOut(allowRemoteAsOut)
        .setSendCommunity(sendCommunity)
        .setSendExtendedCommunity(sendExtendedCommunity)
        .build();
  }

  /** Whether the peer can receive multipath advertisements */
  @JsonProperty(PROP_ADDITIONAL_PATHS_RECEIVE)
  public boolean getAdditionalPathsReceive() {
    return _additionalPathsReceive;
  }

  /**
   * Whether the peer should send <em>all</em> paths for a given NLRI.
   *
   * <p>Note: other modes (such as equal-cost or path-count) are not yet supported
   */
  @JsonProperty(PROP_ADDITIONAL_PATHS_SELECT_ALL)
  public boolean getAdditionalPathsSelectAll() {
    return _additionalPathsSelectAll;
  }

  /** Whether the peer should advertise multiple paths in addition to the best path */
  @JsonProperty(PROP_ADDITIONAL_PATHS_SEND)
  public boolean getAdditionalPathsSend() {
    return _additionalPathsSend;
  }

  /**
   * Whether to advertise the best eBGP route for each network independently of whether it is the
   * best BGP route for that network
   */
  @JsonProperty(PROP_ADVERTISE_EXTERNAL)
  public boolean getAdvertiseExternal() {
    return _advertiseExternal;
  }

  /**
   * Whether to advertise the best BGP route for each network independently of whether it is the
   * best overall route for that network
   */
  @JsonProperty(PROP_ADVERTISE_INACTIVE)
  public boolean getAdvertiseInactive() {
    return _advertiseInactive;
  }

  /** Whether to allow reception of advertisements containing the local AS number in the AS-path */
  @JsonProperty(PROP_ALLOW_LOCAL_AS_IN)
  public boolean getAllowLocalAsIn() {
    return _allowLocalAsIn;
  }

  /** Whether to allow sending of advertisements containing the remote AS number in the AS-path */
  @JsonProperty(PROP_ALLOW_REMOTE_AS_OUT)
  public @Nonnull AllowRemoteAsOutMode getAllowRemoteAsOut() {
    return _allowRemoteAsOut;
  }

  /**
   * Whether or not to propagate the <em>standard</em> community attribute(s) of advertisements to
   * this peer
   */
  @JsonProperty(PROP_SEND_COMMUNITY)
  public boolean getSendCommunity() {
    return _sendCommunity;
  }

  /**
   * Whether or not to propagate the <em>extended</em> community attribute(s) of advertisements to
   * this peer
   */
  @JsonProperty(PROP_SEND_EXTENDED_COMMUNITY)
  public boolean getSendExtendedCommunity() {
    return _sendExtendedCommunity;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddressFamilyCapabilities)) {
      return false;
    }
    AddressFamilyCapabilities that = (AddressFamilyCapabilities) o;
    return _additionalPathsReceive == that._additionalPathsReceive
        && _additionalPathsSelectAll == that._additionalPathsSelectAll
        && _additionalPathsSend == that._additionalPathsSend
        && _advertiseExternal == that._advertiseExternal
        && _advertiseInactive == that._advertiseInactive
        && _allowLocalAsIn == that._allowLocalAsIn
        && _allowRemoteAsOut == that._allowRemoteAsOut
        && _sendCommunity == that._sendCommunity
        && _sendExtendedCommunity == that._sendExtendedCommunity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _additionalPathsReceive,
        _additionalPathsSelectAll,
        _additionalPathsSend,
        _advertiseExternal,
        _advertiseInactive,
        _allowLocalAsIn,
        _allowRemoteAsOut,
        _sendCommunity,
        _sendExtendedCommunity);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_ADDITIONAL_PATHS_RECEIVE, _additionalPathsReceive)
        .add(PROP_ADDITIONAL_PATHS_SELECT_ALL, _additionalPathsSelectAll)
        .add(PROP_ADDITIONAL_PATHS_SEND, _additionalPathsSend)
        .add(PROP_ADVERTISE_EXTERNAL, _advertiseExternal)
        .add(PROP_ADVERTISE_INACTIVE, _advertiseInactive)
        .add(PROP_ALLOW_LOCAL_AS_IN, _allowLocalAsIn)
        .add(PROP_ALLOW_REMOTE_AS_OUT, _allowRemoteAsOut)
        .add(PROP_SEND_COMMUNITY, _sendCommunity)
        .add(PROP_SEND_EXTENDED_COMMUNITY, _sendExtendedCommunity)
        .toString();
  }

  /**
   * Return a builder for {@link AddressFamilyCapabilities} By default all boolean values are
   * initialized to {@code false}, and {@code _allowRemoteAsOut} is initialized to {@link
   * AllowRemoteAsOutMode#NEVER}.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private boolean _additionalPathsReceive;
    private boolean _additionalPathsSelectAll;
    private boolean _additionalPathsSend;
    private boolean _advertiseExternal;
    private boolean _advertiseInactive;
    private boolean _allowLocalAsIn;
    private @Nonnull AllowRemoteAsOutMode _allowRemoteAsOut;
    private boolean _sendCommunity;
    private boolean _sendExtendedCommunity;

    private Builder() {
      _allowRemoteAsOut = NEVER;
    }

    public Builder setAdditionalPathsReceive(boolean additionalPathsReceive) {
      _additionalPathsReceive = additionalPathsReceive;
      return this;
    }

    public Builder setAdditionalPathsSelectAll(boolean additionalPathsSelectAll) {
      _additionalPathsSelectAll = additionalPathsSelectAll;
      return this;
    }

    public Builder setAdditionalPathsSend(boolean additionalPathsSend) {
      _additionalPathsSend = additionalPathsSend;
      return this;
    }

    public Builder setAdvertiseExternal(boolean advertiseExternal) {
      _advertiseExternal = advertiseExternal;
      return this;
    }

    public Builder setAdvertiseInactive(boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
      return this;
    }

    public Builder setAllowLocalAsIn(boolean allowLocalAsIn) {
      _allowLocalAsIn = allowLocalAsIn;
      return this;
    }

    public Builder setAllowRemoteAsOut(AllowRemoteAsOutMode allowRemoteAsOut) {
      _allowRemoteAsOut = allowRemoteAsOut;
      return this;
    }

    public Builder setSendCommunity(boolean sendCommunity) {
      _sendCommunity = sendCommunity;
      return this;
    }

    public Builder setSendExtendedCommunity(boolean sendExtendedCommunity) {
      _sendExtendedCommunity = sendExtendedCommunity;
      return this;
    }

    public AddressFamilyCapabilities build() {
      return new AddressFamilyCapabilities(
          _additionalPathsReceive,
          _additionalPathsSelectAll,
          _additionalPathsSend,
          _advertiseExternal,
          _advertiseInactive,
          _allowLocalAsIn,
          _allowRemoteAsOut,
          _sendCommunity,
          _sendExtendedCommunity);
    }
  }
}
