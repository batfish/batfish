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

/** Utility methods for creating {@link TraceElement TraceElements}. */
@ParametersAreNonnullByDefault
public final class TraceElements {
  private TraceElements() {}

  public static @Nullable TraceElement matchedByAclLine(IpAccessList acl, int index) {
    return matchedByAclLine(acl.getLines().get(index));
  }

  public static @Nullable TraceElement matchedByAclLine(AclLine line) {
    if (line.getName() == null) {
      return null;
    }
    return matchedByAclLine(line.getName());
  }

  public static TraceElement matchedByAclLine(String lineName) {
    return TraceElement.of("Matched line " + lineName);
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
