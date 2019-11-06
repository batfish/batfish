package org.batfish.datamodel.vendor_family.cisco_xr;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class NtpServer extends ComparableStructure<String> {

  private String _vrf;

  public NtpServer(@JsonProperty(PROP_NAME) String hostname) {
    super(hostname);
  }

  public String getVrf() {
    return _vrf;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
