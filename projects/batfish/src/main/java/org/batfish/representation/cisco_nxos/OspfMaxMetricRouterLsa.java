package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfMaxMetricRouterLsa implements Serializable {

  public @Nullable OspfMetric getExternalLsa() {
    return _externalLsa;
  }

  public void setExternalLsa(@Nullable OspfMetric externalLsa) {
    _externalLsa = externalLsa;
  }

  public boolean getIncludeStub() {
    return _includeStub;
  }

  public void setIncludeStub(boolean includeStub) {
    _includeStub = includeStub;
  }

  public @Nullable OspfMetric getSummaryLsa() {
    return _summaryLsa;
  }

  public void setSummaryLsa(@Nullable OspfMetric summaryLsa) {
    _summaryLsa = summaryLsa;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable OspfMetric _externalLsa;
  private boolean _includeStub;
  private @Nullable OspfMetric _summaryLsa;
}
