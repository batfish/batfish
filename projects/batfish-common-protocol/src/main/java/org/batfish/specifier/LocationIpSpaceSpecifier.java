package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;

/**
 * Specify the {@link IpSpace} inferred from a set of {@link Location locations} (specified by a
 * {@link LocationSpecifier}). This set of "{@link IpSpace} locations" is independent of the set of
 * locations that are to be assigned {@link IpSpace IpSpaces}, which input via the {@link #resolve}
 * method (we could call these the "assignment locations"). All the inferred {@link IpSpace
 * IpSpaces} are unioned together into a single large space, which is then assigned to each
 * assignment location.
 *
 * <p>Example: We want to analyze the behavior of a set of packets starting at a core router, where
 * the packets look like they were sent from some set of hosts. Here, the {@link IpSpace} locations
 * are the host locations, and the assignment locations are the core router locations.
 */
public final class LocationIpSpaceSpecifier implements IpSpaceSpecifier {
  private final @Nonnull LocationSpecifier _locationSpecifier;

  LocationIpSpaceSpecifier(@Nonnull LocationSpecifier locationSpecifier) {
    _locationSpecifier = locationSpecifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocationIpSpaceSpecifier)) {
      return false;
    }
    LocationIpSpaceSpecifier that = (LocationIpSpaceSpecifier) o;
    return Objects.equals(_locationSpecifier, that._locationSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_locationSpecifier);
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> key, SpecifierContext ctxt) {
    Set<Location> locations = _locationSpecifier.resolve(ctxt);
    return IpSpaceAssignment.builder().assign(key, computeIpSpace(locations, ctxt)).build();
  }

  @Nonnull
  public static IpSpace computeIpSpace(Set<Location> locations, SpecifierContext ctxt) {
    return firstNonNull(
        AclIpSpace.union(
            InferFromLocationIpSpaceSpecifier.INSTANCE
                .resolve(locations, ctxt)
                .getEntries()
                .stream()
                .map(Entry::getIpSpace)
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }
}
