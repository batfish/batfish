package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class SntpServer extends ComparableStructure<String> {

  public Integer _version;

  @JsonCreator
  public SntpServer(@JsonProperty(PROP_NAME) String hostname) {
    super(hostname);
  }

  public Integer getVersion() {
    return _version;
  }

  public void setVersion(Integer version) {
    _version = version;
  }
}
