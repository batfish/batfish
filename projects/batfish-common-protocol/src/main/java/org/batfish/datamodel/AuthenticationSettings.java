package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class AuthenticationSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String PROP_AUTH_ALGORITHM = "authAlgorithm";

  private static final String PROP_AUTH_KEY = "authKey";

  private static final String PROP_AUTH_KEY_CHAIN_NAME = "authKeyChainName";

  private BgpAuthenticationAlgorithm _authAlgorithm;

  private String _authKey;

  private String _authKeyChainName;

  @JsonCreator
  public AuthenticationSettings(
      @JsonProperty(PROP_AUTH_ALGORITHM) BgpAuthenticationAlgorithm authAlgorithm,
      @JsonProperty(PROP_AUTH_KEY) String authKey,
      @JsonProperty(PROP_AUTH_KEY_CHAIN_NAME) String authKeyChainName) {
    _authAlgorithm = authAlgorithm;
    _authKey = authKey;
    _authKeyChainName = authKeyChainName;
  }

  @JsonProperty(PROP_AUTH_ALGORITHM)
  public BgpAuthenticationAlgorithm getAuthAlgorithm() {
    return _authAlgorithm;
  }

  @JsonProperty(PROP_AUTH_KEY)
  public String getAuthKey() {
    return _authKey;
  }

  @JsonProperty(PROP_AUTH_KEY_CHAIN_NAME)
  public String getAuthKeyChainName() {
    return _authKeyChainName;
  }

  @JsonProperty(PROP_AUTH_ALGORITHM)
  public void setAuthAlgorithm(BgpAuthenticationAlgorithm authAlgorithm) {
    _authAlgorithm = authAlgorithm;
  }

  @JsonProperty(PROP_AUTH_KEY)
  public void setAuthKey(String authKey) {
    _authKey = authKey;
  }

  @JsonProperty(PROP_AUTH_KEY_CHAIN_NAME)
  public void setAuthKeyChainName(String authKeyChainName) {
    _authKeyChainName = authKeyChainName;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AuthenticationSettings)) {
      return false;
    }
    AuthenticationSettings other = (AuthenticationSettings) o;
    return Objects.equals(_authAlgorithm, other._authAlgorithm)
        && Objects.equals(_authKey, other._authKey)
        && Objects.equals(_authKeyChainName, other._authKeyChainName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_authAlgorithm, _authKey, _authKeyChainName);
  }
}
