package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;

public class Aaa implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private AaaAccounting _accounting;

  private AaaAuthentication _authentication;

  private boolean _newModel;

  public AaaAccounting getAccounting() {
    return _accounting;
  }

  public AaaAuthentication getAuthentication() {
    return _authentication;
  }

  public boolean getNewModel() {
    return _newModel;
  }

  public void setAccounting(AaaAccounting accounting) {
    _accounting = accounting;
  }

  public void setAuthentication(AaaAuthentication aaaAuthentication) {
    _authentication = aaaAuthentication;
  }

  public void setNewModel(boolean newModel) {
    _newModel = newModel;
  }
}
