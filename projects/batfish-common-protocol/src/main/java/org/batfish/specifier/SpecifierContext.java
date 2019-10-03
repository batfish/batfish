package org.batfish.specifier;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

/**
 * Collects all the information about the network that is needed by {@link NodeSpecifier}s, {@link
 * LocationSpecifier}s, and {@link IpSpaceSpecifier}s to resolve themselves.
 */
public interface SpecifierContext {
  /** @return The network configurations. */
  @Nonnull
  Map<String, Configuration> getConfigs();

  /** @return the set of {@link ReferenceBook} with name {@code bookName}. */
  Optional<ReferenceBook> getReferenceBook(String bookName);

  /**
   * @return the {@link NodeRoleDimension} if one exists by the provided name {@code dimension}. If
   *     {@code dimension} is null, looks for the default dimension.
   */
  @Nonnull
  Optional<NodeRoleDimension> getNodeRoleDimension(@Nullable String dimension);

  /**
   * Get the {@link IpSpace} owned by the input interface.
   *
   * @param hostname The node the interface belongs to.
   * @param iface The name of the interface.
   * @return The {@link IpSpace} owned by the interface.
   */
  IpSpace getInterfaceOwnedIps(String hostname, String iface);

  /**
   * Get the {@link IpSpace} owned by the input interface.
   *
   * @param hostname The node the interface belongs to.
   * @param iface The name of the interface.
   * @return The {@link IpSpace} owned by the interface.
   */
  IpSpace getInterfaceLinkOwnedIps(String hostname, String iface);
}
