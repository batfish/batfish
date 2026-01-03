package org.batfish.datamodel.acl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;

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
    return matchedByAclLine(line.getName(), line.getVendorStructureId().orElse(null));
  }

  public static TraceElement matchedByAclLine(
      String lineName, @Nullable VendorStructureId vendorStructureId) {
    if (vendorStructureId == null) {
      return TraceElement.of("Matched line " + lineName);
    } else {
      return TraceElement.builder().add("Matched line ").add(lineName, vendorStructureId).build();
    }
  }

  public static TraceElement permittedByNamedIpSpace(
      Ip ip,
      String ipDescription,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      @Nonnull String name) {
    if (ipSpaceMetadata == null) {
      return TraceElement.of(String.format("%s %s permitted by '%s'", ipDescription, ip, name));
    }
    String type = ipSpaceMetadata.getSourceType();
    String displayName = ipSpaceMetadata.getSourceName();
    VendorStructureId vendorStructureId = ipSpaceMetadata.getVendorStructureId();
    if (vendorStructureId == null) {
      return TraceElement.of(
          String.format("%s %s permitted by %s named '%s'", ipDescription, ip, type, displayName));
    } else {
      return TraceElement.builder()
          .add(String.format("%s %s permitted by %s ", ipDescription, ip, type))
          .add(displayName, vendorStructureId)
          .build();
    }
  }
}
