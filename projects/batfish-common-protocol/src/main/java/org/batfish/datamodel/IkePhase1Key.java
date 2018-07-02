package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Objects;
import org.batfish.common.util.ComparableStructure;

/** Represents a key to be used with IKE phase 1 policy */
public class IkePhase1Key extends ComparableStructure<String> {
  private static final long serialVersionUID = 1L;

  private static final String PROP_KEY_TYPE = "keyType";

  private static final String PROP_KEY = "key";

  private static final String PROP_REMOTE_IDENTITY = "remoteIdentity";

  private static final String PROP_LOCAL_INTERFACE = "localInterface";

  private IkeKeyType _keyType;

  private String _key;

  private IpWildcard _remoteIdentity;

  private String _localInterface;

  @JsonCreator
  public IkePhase1Key(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }

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
  public String getKey() {
    return _key;
  }

  public boolean match(String localInterface, Prefix matchIdentity) {
    return _remoteIdentity != null
        && matchIdentity != null
        && _remoteIdentity.supersetOf(new IpWildcard(matchIdentity))
        && (_localInterface == null || Objects.equals(_localInterface, localInterface));
  }

  @JsonProperty(PROP_KEY_TYPE)
  public void setKeyType(IkeKeyType keyType) {
    _keyType = keyType;
  }

  @JsonProperty(PROP_KEY)
  public void setKey(String key) {
    _key = key;
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
