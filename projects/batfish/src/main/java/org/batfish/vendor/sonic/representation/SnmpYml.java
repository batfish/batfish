package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents information in snmp.yml file that is part of the SONiC file bundle. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnmpYml implements Serializable {
  private static final String SNMP_ROCOMMUNITY = "snmp_rocommunity";
  private static final String SNMP_ROCOMMUNITY6 = "snmp_rocommunity6";

  private final @Nullable String _roCommunity;
  private final @Nullable String _roCommunity6;

  private SnmpYml(@Nullable String roCommunity, @Nullable String roCommunity6) {
    _roCommunity = roCommunity;
    _roCommunity6 = roCommunity6;
  }

  @JsonCreator
  private static SnmpYml create(
      @JsonProperty(SNMP_ROCOMMUNITY) @Nullable String roCommunity,
      @JsonProperty(SNMP_ROCOMMUNITY6) @Nullable String roCommunity6) {
    return new SnmpYml(roCommunity, roCommunity6);
  }

  public @Nullable String getRoCommunity() {
    return _roCommunity;
  }

  public @Nullable String getRoCommunity6() {
    return _roCommunity6;
  }
}
