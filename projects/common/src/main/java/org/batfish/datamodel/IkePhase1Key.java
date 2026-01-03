package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.visitors.IpSpaceRepresentative;

/** Represents a key to be used with IKE phase 1 policy */
public class IkePhase1Key implements Serializable {

  private static final String PROP_KEY_TYPE = "keyType";
  private static final String PROP_KEY_HASH = "keyHash";
  private static final String PROP_REMOTE_IDENTITY = "remoteIdentity";
  private static final String PROP_LOCAL_INTERFACE = "localInterface";

  private IkeKeyType _keyType;

  private String _keyHash;

  private @Nonnull IpSpace _remoteIdentity;

  private @Nonnull String _localInterface;

  @JsonCreator
  public IkePhase1Key() {
    _remoteIdentity = IpWildcard.parse("0.0.0.0:0.0.0.0").toIpSpace();
    _localInterface = UNSET_LOCAL_INTERFACE;
  }

  /** Type of key. */
  @JsonProperty(PROP_KEY_TYPE)
  public IkeKeyType getKeyType() {
    return _keyType;
  }

  /** Identity of the remote peer which matches this key. */
  @JsonProperty(PROP_REMOTE_IDENTITY)
  public @Nonnull IpSpace getRemoteIdentity() {
    return _remoteIdentity;
  }

  /** Local interface on which this key can be used. */
  @JsonProperty(PROP_LOCAL_INTERFACE)
  public @Nonnull String getLocalInterface() {
    return _localInterface;
  }

  /** Value of the key hash. */
  @JsonProperty(PROP_KEY_HASH)
  public String getKeyHash() {
    return _keyHash;
  }

  /**
   * Returns true if this {@link IkePhase1Key} can be used with the given localInterface and
   * matchIdentity
   *
   * @param localInterface {@link Interface} name on which this {@link IkePhase1Key} is intended to
   *     be used
   * @param matchIdentity {@link IpWildcard} for the remote peers with which this {@link
   *     IkePhase1Key} is intended to be used
   * @return true if this {@link IkePhase1Key} can be used with the given localInterface and
   *     matchIdentity
   */
  public boolean match(String localInterface, IpWildcard matchIdentity) {
    return matchIdentity != null
        && new IpSpaceRepresentative()
            .getRepresentative(AclIpSpace.intersection(_remoteIdentity, matchIdentity.toIpSpace()))
            .isPresent()
        && (_localInterface.equals(UNSET_LOCAL_INTERFACE)
            || Objects.equals(_localInterface, localInterface));
  }

  @JsonProperty(PROP_KEY_TYPE)
  public void setKeyType(@Nullable IkeKeyType keyType) {
    _keyType = keyType;
  }

  @JsonProperty(PROP_KEY_HASH)
  public void setKeyHash(@Nullable String keyHash) {
    _keyHash = keyHash;
  }

  @JsonProperty(PROP_REMOTE_IDENTITY)
  public void setRemoteIdentity(@Nullable IpSpace remoteIdentity) {
    _remoteIdentity = firstNonNull(remoteIdentity, IpWildcard.parse("0.0.0.0:0.0.0.0").toIpSpace());
  }

  @JsonProperty(PROP_LOCAL_INTERFACE)
  public void setLocalInterface(@Nullable String localInterface) {
    _localInterface = firstNonNull(localInterface, UNSET_LOCAL_INTERFACE);
  }
}
