package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class BgpAuthenticationSettings implements Serializable {

  private static final String PROP_AUTHENTICATION_ALGORITHM = "authenticationAlgorithm";
  private static final String PROP_AUTHENTICATION_KEY = "authenticationKey";
  private static final String PROP_AUTHENTICATION_KEY_CHAIN_NAME = "authenticationKeyChainName";

  private BgpAuthenticationAlgorithm _authenticationAlgorithm;

  private String _authenticationKey;

  private String _authenticationKeyChainName;

  @JsonProperty(PROP_AUTHENTICATION_ALGORITHM)
  public BgpAuthenticationAlgorithm getAuthenticationAlgorithm() {
    return _authenticationAlgorithm;
  }

  @JsonProperty(PROP_AUTHENTICATION_KEY)
  public String getAuthenticationKey() {
    return _authenticationKey;
  }

  @JsonProperty(PROP_AUTHENTICATION_KEY_CHAIN_NAME)
  public String getAuthenticationKeyChainName() {
    return _authenticationKeyChainName;
  }

  @JsonProperty(PROP_AUTHENTICATION_ALGORITHM)
  public void setAuthenticationAlgorithm(BgpAuthenticationAlgorithm authenticationAlgorithm) {
    _authenticationAlgorithm = authenticationAlgorithm;
  }

  @JsonProperty(PROP_AUTHENTICATION_KEY)
  public void setAuthenticationKey(String authenticationKey) {
    _authenticationKey = authenticationKey;
  }

  @JsonProperty(PROP_AUTHENTICATION_KEY_CHAIN_NAME)
  public void setAuthenticationKeyChainName(String authenticationKeyChainName) {
    _authenticationKeyChainName = authenticationKeyChainName;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BgpAuthenticationSettings)) {
      return false;
    }
    BgpAuthenticationSettings other = (BgpAuthenticationSettings) o;
    return Objects.equals(_authenticationAlgorithm, other._authenticationAlgorithm)
        && Objects.equals(_authenticationKey, other._authenticationKey)
        && Objects.equals(_authenticationKeyChainName, other._authenticationKeyChainName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_authenticationAlgorithm, _authenticationKey, _authenticationKeyChainName);
  }
}
