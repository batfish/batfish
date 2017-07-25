package org.batfish.datamodel.routing_policy;

public class Result {

  private boolean _booleanValue;

  private boolean _exit;

  private boolean _fallThrough;

  private boolean _return;

  public boolean getBooleanValue() {
    return _booleanValue;
  }

  public boolean getExit() {
    return _exit;
  }

  public boolean getFallThrough() {
    return _fallThrough;
  }

  public boolean getReturn() {
    return _return;
  }

  public void setBooleanValue(boolean booleanValue) {
    _booleanValue = booleanValue;
  }

  public void setExit(boolean exit) {
    _exit = exit;
  }

  public void setFallThrough(boolean fallThrough) {
    _fallThrough = fallThrough;
  }

  public void setReturn(boolean ret) {
    _return = ret;
  }
}
