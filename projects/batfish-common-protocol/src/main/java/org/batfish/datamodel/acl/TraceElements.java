package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;

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

  public static TraceElement permittedByAclLine(IpAccessList acl, int index) {
    return matchedByAclLine(acl, index, "permitted");
  }

  static TraceElement deniedByAclLine(IpAccessList acl, int index) {
    return matchedByAclLine(acl,index,"denied");
  }

  private static TraceElement matchedByAclLine(IpAccessList acl, int index, String action) {
    String type = firstNonNull(acl.getSourceType(), "filter");
    String name = firstNonNull(acl.getSourceName(), acl.getName());
    AclLine line = acl.getLines().get(index);
    String lineDescription = line.getName() == null ? "" : ": " + line.getName();
    String description =
        String.format(
            "Flow %s by %s named %s, index %d%s", action, type, name, index, lineDescription);
    return TraceElement.of(description);
  }


  public static TraceElement defaultDeniedByIpAccessList(IpAccessList ipAccessList) {
    String name = ipAccessList.getName();
    @Nullable String sourceName = ipAccessList.getSourceName();
    @Nullable String sourceType = ipAccessList.getSourceType();
    String description =
        sourceName != null
            ? String.format("Flow did not match '%s' named '%s'", sourceType, sourceName)
            : String.format("Flow did not match ACL named '%s'", name);
    return TraceElement.of(description);
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
