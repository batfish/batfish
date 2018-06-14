package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;

public class NssaSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private OspfDefaultOriginateType _defaultLsaType;

  private boolean _noSummaries;

  public NssaSettings() {
    _defaultLsaType = OspfDefaultOriginateType.NONE;
  }

  public OspfDefaultOriginateType getDefaultLsaType() {
    return _defaultLsaType;
  }

  public boolean getNoSummaries() {
    return _noSummaries;
  }

  public void setDefaultLsaType(OspfDefaultOriginateType defaultLsaType) {
    _defaultLsaType = defaultLsaType;
  }

  public void setNoSummaries(boolean noSummaries) {
    _noSummaries = noSummaries;
  }
}
