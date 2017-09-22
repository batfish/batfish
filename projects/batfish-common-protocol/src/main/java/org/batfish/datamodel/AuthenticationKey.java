package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class AuthenticationKey extends ComparableStructure<String> {

  public enum IsisOption {
    BASIC,
    ISIS_ENHANCED
  }

  private static final String PROP_BGP_ALGORITHM = "bgpAlgorithm";

  private static final String PROP_ISIS_ALGORITHM = "isisAlgorithm";

  private static final String PROP_ISIS_OPTION = "isisOption";

  private static final String PROP_SECRET = "secret";

  private static final String PROP_START_TIME = "startTime";

  private static final long serialVersionUID = 1L;

  private BgpAuthenticationAlgorithm _bgpAlgorithm;

  private IsisAuthenticationAlgorithm _isisAlgorithm;

  private IsisOption _isisOption;

  private String _secret;

  private String _startTime;

  public AuthenticationKey(String name) {
    super(name);
    _isisAlgorithm = IsisAuthenticationAlgorithm.MD5;
    _isisOption = IsisOption.BASIC;
  }

  @JsonCreator
  public AuthenticationKey(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_BGP_ALGORITHM) BgpAuthenticationAlgorithm bgpAlgorithm,
      @JsonProperty(PROP_ISIS_ALGORITHM) IsisAuthenticationAlgorithm isisAlgorithm,
      @JsonProperty(PROP_ISIS_OPTION) IsisOption isisOption,
      @JsonProperty(PROP_SECRET) String secret,
      @JsonProperty(PROP_START_TIME) String startTime) {
    super(name);
    _bgpAlgorithm = bgpAlgorithm;
    _isisAlgorithm = isisAlgorithm;
    _isisOption = isisOption;
    _secret = secret;
    _startTime = startTime;
  }

  @JsonProperty(PROP_BGP_ALGORITHM)
  public BgpAuthenticationAlgorithm getBgpAlgorithm() {
    return _bgpAlgorithm;
  }

  @JsonProperty(PROP_ISIS_ALGORITHM)
  public IsisAuthenticationAlgorithm getIsisAlgorithm() {
    return _isisAlgorithm;
  }

  @JsonProperty(PROP_ISIS_OPTION)
  public IsisOption getIsisOption() {
    return _isisOption;
  }

  @JsonProperty(PROP_SECRET)
  public String getSecret() {
    return _secret;
  }

  @JsonProperty(PROP_START_TIME)
  public String getStartTime() {
    return _startTime;
  }

  @JsonProperty(PROP_BGP_ALGORITHM)
  public void setBgpAlgorithm(BgpAuthenticationAlgorithm bgpAlgorithm) {
    _bgpAlgorithm = bgpAlgorithm;
  }

  @JsonProperty(PROP_ISIS_ALGORITHM)
  public void setIsisAlgorithm(IsisAuthenticationAlgorithm isisAlgorithm) {
    _isisAlgorithm = isisAlgorithm;
  }

  @JsonProperty(PROP_ISIS_OPTION)
  public void setIsisOption(IsisOption isisOption) {
    _isisOption = isisOption;
  }

  @JsonProperty(PROP_SECRET)
  public void setSecret(String secret) {
    _secret = secret;
  }

  @JsonProperty(PROP_START_TIME)
  public void setStartTime(String startTime) {
    _startTime = startTime;
  }
}
