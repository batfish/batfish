package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

public class NtpServer extends ComparableStructure<String> {

  private static final String PROP_KEY = "key";

  private @Nullable Long _key;
  private String _vrf;

  @JsonCreator
  public NtpServer(@JsonProperty(PROP_NAME) String hostname) {
    super(hostname);
  }

  @JsonProperty(PROP_KEY)
  public @Nullable Long getKey() {
    return _key;
  }

  public void setKey(@Nullable Long key) {
    _key = key;
  }

  public String getVrf() {
    return _vrf;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
