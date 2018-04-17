package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class SimpleJsonIpSpace implements JacksonSerializableIpSpace {

  private static final String PROP_IP = "ip";

  private static final String PROP_IP_WILDCARD = "ipWildcard";

  private static final String PROP_PREFIX = "prefix";

  private final Ip _ip;

  private final IpSpace _ipSpace;

  private final IpWildcard _ipWildcard;

  private final Prefix _prefix;

  public SimpleJsonIpSpace(Ip ip) {
    this(ip, null, null);
  }

  @JsonCreator
  private SimpleJsonIpSpace(
      @JsonProperty(PROP_IP) Ip ip,
      @JsonProperty(PROP_IP_WILDCARD) IpWildcard ipWildcard,
      @JsonProperty(PROP_PREFIX) Prefix prefix) {
    _ip = ip;
    _ipWildcard = ipWildcard;
    _prefix = prefix;
    _ipSpace = ip != null ? ip : ipWildcard != null ? ipWildcard : prefix;
  }

  public SimpleJsonIpSpace(IpWildcard ipWildcard) {
    this(null, ipWildcard, null);
  }

  public SimpleJsonIpSpace(Prefix prefix) {
    this(null, null, prefix);
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return _ipSpace.accept(visitor);
  }

  @Override
  public IpSpace complement() {
    return _ipSpace.complement();
  }

  @Override
  public boolean containsIp(Ip ip) {
    return _ipSpace.containsIp(ip);
  }

  @JsonProperty(PROP_IP)
  public Ip getIp() {
    return _ip;
  }

  @JsonProperty(PROP_IP_WILDCARD)
  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @JsonProperty(PROP_PREFIX)
  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public IpSpace unwrap() {
    return _ipSpace;
  }
}
