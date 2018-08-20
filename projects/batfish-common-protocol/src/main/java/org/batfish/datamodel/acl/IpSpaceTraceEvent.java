package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

public abstract class IpSpaceTraceEvent implements TraceEvent {

  protected static final String PROP_IP = "ip";

  protected static final String PROP_IP_DESCRIPTION = "ipDescription";

  private static final long serialVersionUID = 1L;

  private final String _description;

  private final Ip _ip;

  private final String _ipDescription;

  protected IpSpaceTraceEvent(
      @JsonProperty(PROP_DESCRIPTION) @Nonnull String description,
      @JsonProperty(PROP_IP) @Nonnull Ip ip,
      @JsonProperty(PROP_IP_DESCRIPTION) @Nonnull String ipDescription) {
    _description = description;
    _ip = ip;
    _ipDescription = ipDescription;
  }

  @Override
  public final @Nonnull String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_IP)
  public final @Nonnull Ip getIp() {
    return _ip;
  }

  @JsonProperty(PROP_IP_DESCRIPTION)
  public final @Nonnull String getIpDescription() {
    return _ipDescription;
  }

  @Override
  public final String toString() {
    return _description;
  }
}
