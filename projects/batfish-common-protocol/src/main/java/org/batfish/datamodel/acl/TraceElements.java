package org.batfish.datamodel.acl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.TraceElement;

/** Utility methods for creating {@link TraceEvent TraceEvents}. */
@ParametersAreNonnullByDefault
public final class TraceElements {
  private TraceElements() {}

  public static TraceElement matchedByAclLine(IpAccessList acl, int index) {
    AclLine line = acl.getLines().get(index);
    String lineDescription =
        line.getName() == null ? String.format("at index %s", index) : line.getName();
    return TraceElement.of("Matched line " + lineDescription);
  }

  public static TraceElement permittedByNamedIpSpace(
      Ip ip,
      String ipDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      @Nonnull String name) {
    String type;
    String displayName;
    if (ipSpaceMetadata != null) {
      type = ipSpaceMetadata.getSourceType();
      displayName = ipSpaceMetadata.getSourceName();
    } else {
      type = IpSpace.class.getSimpleName();
      displayName = name;
    }
    return TraceElement.of(
        String.format("%s %s permitted by '%s' named '%s'", ipDescription, ip, type, displayName));
  }
}
