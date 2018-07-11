package org.batfish.datamodel.acl;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;

public final class PermittedByNamedIpSpace extends IpSpaceTraceEvent {

  private static final String PROP_DESCRIPTION = "description";

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private static String computeDescription(
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nonnull String ipSpaceDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      @Nonnull String name) {
    String type;
    String displayName;
    String description;
    if (ipSpaceMetadata != null) {
      type = ipSpaceMetadata.getSourceType();
      displayName = ipSpaceMetadata.getSourceName();
      description = "";
    } else {
      type = IpSpace.class.getSimpleName();
      displayName = name;
      description = String.format(": %s", ipSpaceDescription);
    }
    return String.format(
        "%s %s permitted by '%s' named '%s'%s", ipDescription, ip, type, displayName, description);
  }

  @JsonCreator
  private static PermittedByNamedIpSpace create(
      @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_IP) Ip ip,
      @JsonProperty(PROP_IP_DESCRIPTION) String ipDescription,
      @JsonProperty(PROP_NAME) String name) {
    return new PermittedByNamedIpSpace(
        requireNonNull(description),
        requireNonNull(ip),
        requireNonNull(ipDescription),
        requireNonNull(name));
  }

  private final String _name;

  public PermittedByNamedIpSpace(
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nonnull String ipSpaceDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      @Nonnull String name) {
    this(
        computeDescription(ip, ipDescription, ipSpaceDescription, ipSpaceMetadata, name),
        ip,
        ipDescription,
        name);
  }

  private PermittedByNamedIpSpace(
      @Nonnull String description,
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nonnull String name) {
    super(description, ip, ipDescription);
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PermittedByNamedIpSpace)) {
      return false;
    }
    PermittedByNamedIpSpace rhs = (PermittedByNamedIpSpace) obj;
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
