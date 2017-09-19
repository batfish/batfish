package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import org.batfish.common.util.ComparableStructure;

public class AuthenticationKey extends ComparableStructure<String> {

  public enum KeyOption {
    BASIC,
    ISIS_ENHANCED
  }

  private static final String PROP_ALGORITHM = "algorithm";

  private static final String PROP_OPTION = "option";

  private static final String PROP_SECRET = "secret";

  private static final String PROP_START_TIME = "startTime";

  private static final long serialVersionUID = 1L;

  private BgpAuthenticationAlgorithm _algorithm;

  private AuthenticationKey.KeyOption _option;

  private String _secret;
  
  private Date _startTime;

  public AuthenticationKey(String name) {
    super(name);
    this._algorithm = BgpAuthenticationAlgorithm.MD5;
    this._option = AuthenticationKey.KeyOption.BASIC;
  }

  @JsonCreator
  public AuthenticationKey(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_ALGORITHM) BgpAuthenticationAlgorithm algorithm,
      @JsonProperty(PROP_OPTION) AuthenticationKey.KeyOption option,
      @JsonProperty(PROP_SECRET) String secret,
      @JsonProperty(PROP_START_TIME) Date startTime) {
    super(name);
    _algorithm = algorithm;
    _option = option;
    _secret = secret;
    _startTime = startTime;
  }

  @JsonProperty(PROP_ALGORITHM)
  public BgpAuthenticationAlgorithm getAlgorithm() {
    return _algorithm;
  }

  @JsonProperty(PROP_OPTION)
  public AuthenticationKey.KeyOption getOption() {
    return _option;
  }

  @JsonProperty(PROP_SECRET)
  public String getSecret() {
    return _secret;
  }

  @JsonProperty(PROP_START_TIME)
  public Date getStartTime() {
    return _startTime;
  }

  @JsonProperty(PROP_ALGORITHM)
  public void setAlgorithm(BgpAuthenticationAlgorithm algorithm) {
    this._algorithm = algorithm;
  }

  @JsonProperty(PROP_OPTION)
  public void setOption(AuthenticationKey.KeyOption option) {
    this._option = option;
  }

  @JsonProperty(PROP_SECRET)
  public void setSecret(String secret) {
    this._secret = secret;
  }

  @JsonProperty(PROP_START_TIME)
  public void setStartTime(Date startTime) {
    this._startTime = startTime;
  }
}
