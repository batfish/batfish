package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** A user facing representation for properties of a BGP session. */
@ParametersAreNonnullByDefault
public final class BgpSessionProperties {

  public static final String PROP_LOCAL_AS = "localAs";
  public static final String PROP_REMOTE_AS = "remoteAs";
  public static final String PROP_LOCAL_IP = "localIp";
  public static final String PROP_REMOTE_IP = "remoteIp";

  private final long _localAs;
  private final long _remoteAs;
  private final @Nonnull Ip _localIp;
  private final @Nonnull Ip _remoteIp;

  public BgpSessionProperties(long localAs, long remoteAs, Ip localIp, Ip remoteIp) {
    _localAs = localAs;
    _remoteAs = remoteAs;
    _localIp = localIp;
    _remoteIp = remoteIp;
  }

  @JsonCreator
  private static BgpSessionProperties jsonCreator(
      @JsonProperty(PROP_LOCAL_AS) @Nullable Long localAs,
      @JsonProperty(PROP_REMOTE_AS) @Nullable Long remoteAs,
      @JsonProperty(PROP_LOCAL_IP) @Nullable Ip localIp,
      @JsonProperty(PROP_REMOTE_IP) @Nullable Ip remoteIp) {
    checkArgument(localAs != null, "%s must be specified", PROP_LOCAL_AS);
    checkArgument(remoteAs != null, "%s must be specified", PROP_REMOTE_AS);
    checkArgument(localIp != null, "%s must be specified", PROP_LOCAL_IP);
    checkArgument(remoteIp != null, "%s must be specified", PROP_REMOTE_IP);

    return new BgpSessionProperties(localAs, remoteAs, localIp, remoteIp);
  }

  @JsonProperty(PROP_LOCAL_AS)
  public long getLocalAs() {
    return _localAs;
  }

  @JsonProperty(PROP_REMOTE_AS)
  public long getRemoteAs() {
    return _remoteAs;
  }

  @JsonProperty(PROP_LOCAL_IP)
  public @Nonnull Ip getLocalIp() {
    return _localIp;
  }

  @JsonProperty(PROP_REMOTE_IP)
  public @Nonnull Ip getRemoteIp() {
    return _remoteIp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpSessionProperties)) {
      return false;
    }
    BgpSessionProperties bgpSessionProperties = (BgpSessionProperties) o;
    return _localAs == bgpSessionProperties._localAs
        && _remoteAs == bgpSessionProperties._remoteAs
        && Objects.equals(_localIp, bgpSessionProperties._localIp)
        && Objects.equals(_remoteIp, bgpSessionProperties._remoteIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_localAs, _remoteAs, _localIp, _remoteIp);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("localAs", _localAs)
        .add("remoteAs", _remoteAs)
        .add("localIp", _localIp)
        .add("remoteIp", _remoteIp)
        .toString();
  }
}
