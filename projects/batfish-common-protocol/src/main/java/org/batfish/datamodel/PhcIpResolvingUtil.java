package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** Utility functions to resolve src/dst Ips from {@link PacketHeaderConstraints} */
public class PhcIpResolvingUtil {
  /**
   * Return the resolved {@link IpSpace} from specified Ips; if failed try to resolve from the
   * source locations; otherwise return {@link UniverseIpSpace}
   */
  public static IpSpace resolveSrcIpSpaceOrUniverse(
      String srcIpStr, Set<Location> srcLocations, SpecifierContext specifierContext) {
    return resolveIpSpaceOrUniverse(srcIpStr, srcLocations, specifierContext);
  }

  /**
   * Return the resolved {@link IpSpace} from specified Ips; otherwise return {@link
   * UniverseIpSpace}
   */
  public static IpSpace resolveDstIpSpaceOrUniverse(
      String dstIpStr, SpecifierContext specifierContext) {
    return resolveIpSpaceOrUniverse(dstIpStr, ImmutableSet.of(), specifierContext);
  }

  @VisibleForTesting
  static IpSpace resolveIpSpaceOrUniverse(
      String ipStr, Set<Location> locations, SpecifierContext specifierContext) {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
            ipStr, InferFromLocationIpSpaceSpecifier.INSTANCE)
        .resolve(locations, specifierContext).getEntries().stream()
        .findFirst()
        .map(Entry::getIpSpace)
        .orElse(UniverseIpSpace.INSTANCE);
  }
}
