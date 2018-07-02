package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.Objects;

/** Represents a key to be used with IKE phase 1 policy */
public class IkePhase1Key implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String PROP_KEY_TYPE = "keyType";

  private static final String PROP_KEY = "key";

  private static final String PROP_REMOTE_IDENTITY = "remoteIdentity";

  private static final String PROP_LOCAL_INTERFACE = "localInterface";

  private IkeKeyType _keyType;

  private String _keyValue;

  private IpWildcard _remoteIdentity;

  private String _localInterface;

  @JsonCreator
  public IkePhase1Key() {}

  @JsonPropertyDescription("Type of key")
  @JsonProperty(PROP_KEY_TYPE)
  public IkeKeyType getKeyType() {
    return _keyType;
  }

  @JsonPropertyDescription("Identity of the remote peer which matches this key")
  @JsonProperty(PROP_REMOTE_IDENTITY)
  public IpWildcard getRemoteIdentity() {
    return _remoteIdentity;
  }

  @JsonPropertyDescription("Local interface on which this key can be used")
  @JsonProperty(PROP_LOCAL_INTERFACE)
  public String getLocalInterface() {
    return _localInterface;
  }

  @JsonPropertyDescription("Value of the key")
  @JsonProperty(PROP_KEY)
  public String getKeyValue() {
    return _keyValue;
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
    return _remoteIdentity != null
        && matchIdentity != null
        && _remoteIdentity.supersetOf(matchIdentity)
        && (_localInterface == null || Objects.equals(_localInterface, localInterface));
  }

  @JsonProperty(PROP_KEY_TYPE)
  public void setKeyType(IkeKeyType keyType) {
    _keyType = keyType;
  }

  @JsonProperty(PROP_KEY)
  public void setKeyValue(String keyValue) {
    _keyValue = keyValue;
  }

  @JsonProperty(PROP_REMOTE_IDENTITY)
  public void setRemoteIdentity(IpWildcard remoteIdentity) {
    _remoteIdentity = remoteIdentity;
  }

  @JsonProperty(PROP_LOCAL_INTERFACE)
  public void setLocalInterface(String localInterface) {
    _localInterface = localInterface;
  }
}
