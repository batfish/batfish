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

public final class DeniedByAclIpSpaceLine extends IpSpaceTraceEvent {
  private static final String PROP_INDEX = "index";
  private static final String PROP_LINE_DESCRIPTION = "lineDescription";
  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private static String computeDescription(
      @Nonnull String name,
      int index,
      @Nonnull String lineDescription,
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata) {
    String displayName =
        ipSpaceMetadata != null
            ? String.format(
                "'%s' named '%s'", ipSpaceMetadata.getSourceType(), ipSpaceMetadata.getSourceName())
            : String.format("'%s' named '%s'", AclIpSpace.class.getSimpleName(), name);
    return String.format(
        "%s %s denied by %s line %d: %s", ipDescription, ip, displayName, index, lineDescription);
  }

  @JsonCreator
  private static @Nonnull DeniedByAclIpSpaceLine create(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_INDEX) int index,
      @JsonProperty(PROP_LINE_DESCRIPTION) @Nonnull String lineDescription,
      @JsonProperty(PROP_IP) Ip ip,
      @JsonProperty(PROP_IP_DESCRIPTION) String ipDescription,
      @JsonProperty(PROP_DESCRIPTION) String description) {
    return new DeniedByAclIpSpaceLine(
        requireNonNull(name),
        index,
        requireNonNull(lineDescription),
        requireNonNull(ip),
        requireNonNull(ipDescription),
        requireNonNull(description));
  }

  private final int _index;

  private final String _lineDescription;

  private final String _name;

  public DeniedByAclIpSpaceLine(
      @Nonnull String name,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      int index,
      @Nonnull String lineDescription,
      @Nonnull Ip ip,
      @Nonnull String ipDescription) {
    super(
        computeDescription(name, index, lineDescription, ip, ipDescription, ipSpaceMetadata),
        ip,
        ipDescription);
    _name = name;
    _index = index;
    _lineDescription = lineDescription;
  }

  private DeniedByAclIpSpaceLine(
      @Nonnull String name,
      int index,
      @Nonnull String lineDescription,
      @Nonnull Ip ip,
      @Nonnull String ipDescription,
      @Nonnull String description) {
    super(description, ip, ipDescription);
    _name = name;
    _index = index;
    _lineDescription = lineDescription;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DeniedByAclIpSpaceLine)) {
      return false;
    }
    DeniedByAclIpSpaceLine rhs = (DeniedByAclIpSpaceLine) obj;
    return getDescription().equals(rhs.getDescription())
        && _index == rhs._index
        && getIp().equals(rhs.getIp())
        && getIpDescription().equals(rhs.getIpDescription())
        && _lineDescription.equals(rhs._lineDescription)
        && _name.equals(rhs._name);
  }

  @JsonProperty(PROP_INDEX)
  public int getIndex() {
    return _index;
  }

  @JsonProperty(PROP_LINE_DESCRIPTION)
  public @Nonnull String getLineDescription() {
    return _lineDescription;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_index, _lineDescription, _name);
  }
}
