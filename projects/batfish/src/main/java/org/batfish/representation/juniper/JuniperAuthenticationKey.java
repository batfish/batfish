package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.isis.IsisAuthenticationAlgorithm;
import org.batfish.datamodel.isis.IsisOption;

public class JuniperAuthenticationKey implements Serializable {

  private static final IsisAuthenticationAlgorithm DEFAULT_ISIS_AUTHENTICATION_ALGORITHM =
      IsisAuthenticationAlgorithm.MD5;

  private static final IsisOption DEFAULT_ISIS_OPTION = IsisOption.BASIC;

  private IsisAuthenticationAlgorithm _isisAuthenticationAlgorithm;

  private IsisOption _isisOption;

  private final String _name;

  private String _secret;

  private String _startTime;

  public JuniperAuthenticationKey(String name) {
    _name = name;
    _isisAuthenticationAlgorithm = DEFAULT_ISIS_AUTHENTICATION_ALGORITHM;
    _isisOption = DEFAULT_ISIS_OPTION;
  }

  public IsisAuthenticationAlgorithm getIsisAuthenticationAlgorithm() {
    return _isisAuthenticationAlgorithm;
  }

  public IsisOption getIsisOption() {
    return _isisOption;
  }

  public String getName() {
    return _name;
  }

  public String getSecret() {
    return _secret;
  }

  public String getStartTime() {
    return _startTime;
  }

  public void setIsisAuthenticationAlgorithm(IsisAuthenticationAlgorithm isisAlgorithm) {
    _isisAuthenticationAlgorithm = isisAlgorithm;
  }

  public void setIsisOption(IsisOption isisOption) {
    _isisOption = isisOption;
  }

  public void setSecret(String secret) {
    _secret = secret;
  }

  public void setStartTime(String startTime) {
    _startTime = startTime;
  }
}
