package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class NtpServer extends ComparableStructure<String> {

  private String _vrf;

  @JsonCreator
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
