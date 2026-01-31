package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfMaxMetricRouterLsa implements Serializable {

  // From https://tools.ietf.org/html/rfc6987#section-2
  public static final int DEFAULT_OSPF_MAX_METRIC = 0xFFFF;

  public @Nullable Integer getExternalLsa() {
    return _externalLsa;
  }

  public void setExternalLsa(@Nullable Integer externalLsa) {
    _externalLsa = externalLsa;
  }

  public boolean getIncludeStub() {
    return _includeStub;
  }

  public void setIncludeStub(boolean includeStub) {
    _includeStub = includeStub;
  }

  public @Nullable Integer getSummaryLsa() {
    return _summaryLsa;
  }

  public void setSummaryLsa(@Nullable Integer summaryLsa) {
    _summaryLsa = summaryLsa;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable Integer _externalLsa;
  private boolean _includeStub;
  private @Nullable Integer _summaryLsa;
}
