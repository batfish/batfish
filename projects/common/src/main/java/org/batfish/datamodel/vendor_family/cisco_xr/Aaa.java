package org.batfish.datamodel.vendor_family.cisco_xr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class Aaa implements Serializable {
  private static final String PROP_ACCOUNTING = "accounting";
  private static final String PROP_AUTHENTICATION = "authentication";
  private static final String PROP_NEW_MODEL = "newModel";

  private AaaAccounting _accounting;

  private AaaAuthentication _authentication;

  private boolean _newModel;

  @JsonProperty(PROP_ACCOUNTING)
  public AaaAccounting getAccounting() {
    return _accounting;
  }

  @JsonProperty(PROP_AUTHENTICATION)
  public AaaAuthentication getAuthentication() {
    return _authentication;
  }

  @JsonProperty(PROP_NEW_MODEL)
  public boolean getNewModel() {
    return _newModel;
  }

  @JsonProperty(PROP_ACCOUNTING)
  public void setAccounting(AaaAccounting accounting) {
    _accounting = accounting;
  }

  @JsonProperty(PROP_AUTHENTICATION)
  public void setAuthentication(AaaAuthentication aaaAuthentication) {
    _authentication = aaaAuthentication;
  }

  @JsonProperty(PROP_NEW_MODEL)
  public void setNewModel(boolean newModel) {
    _newModel = newModel;
  }
}
