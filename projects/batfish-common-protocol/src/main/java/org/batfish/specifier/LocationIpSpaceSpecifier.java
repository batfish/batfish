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
 * Specify the {@lnk IpSpace} inferred from a set of {@link Location locations} (specified by a
 * {@link LocationSpecifier}.
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
    return Objects.hash(_locationSpecifier);
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> key, SpecifierContext ctxt) {
    Set<Location> locations = _locationSpecifier.resolve(ctxt);
    IpSpace ipSpace =
        AclIpSpace.union(
            InferFromLocationIpSpaceSpecifier.INSTANCE
                .resolve(locations, ctxt)
                .getEntries()
                .stream()
                .map(Entry::getIpSpace)
                .collect(Collectors.toList()));
    return IpSpaceAssignment.builder()
        .assign(key, firstNonNull(ipSpace, EmptyIpSpace.INSTANCE))
        .build();
  }
}
