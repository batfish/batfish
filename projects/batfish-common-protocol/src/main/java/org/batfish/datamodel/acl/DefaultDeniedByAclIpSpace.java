package org.batfish.datamodel.acl;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpaceMetadata;

public final class DefaultDeniedByAclIpSpace extends IpSpaceTraceEvent {

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private static String computeDescription(
      @Nonnull String name,
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata) {
    String displayName =
        ipSpaceMetadata != null
            ? String.format(
                "'%s' named '%s'", ipSpaceMetadata.getSourceType(), ipSpaceMetadata.getSourceName())
            : String.format("'%s' named '%s'", AclIpSpace.class.getSimpleName(), name);
    return String.format("%s %s default-denied by %s", ipDescription, ip, displayName);
  }

  @JsonCreator
  private static @Nonnull DefaultDeniedByAclIpSpace create(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_IP) Ip ip,
      @JsonProperty(PROP_IP_DESCRIPTION) String ipDescription,
      @JsonProperty(PROP_DESCRIPTION) String description) {
    return new DefaultDeniedByAclIpSpace(
        requireNonNull(name),
        requireNonNull(ip),
        requireNonNull(ipDescription),
        requireNonNull(description));
  }

  private final String _name;

  public DefaultDeniedByAclIpSpace(
      @Nonnull String name,
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata) {
    super(computeDescription(name, ip, ipDescription, ipSpaceMetadata), ip, ipDescription);
    _name = name;
  }

  private DefaultDeniedByAclIpSpace(
      @Nonnull String name,
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nonnull String description) {
    super(description, ip, ipDescription);
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DefaultDeniedByAclIpSpace)) {
      return false;
    }
    DefaultDeniedByAclIpSpace rhs = (DefaultDeniedByAclIpSpace) obj;
    return getDescription().equals(rhs.getDescription())
        && getIp().equals(rhs.getIp())
        && getIpDescription().equals(rhs.getIpDescription())
        && _name.equals(rhs._name);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDescription(), getIp(), getIpDescription(), _name);
  }
}
