package org.batfish.specifier;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceAssignmentSpecifier} that is constant (independent of the input {@link
 * Location}). All input {@link Location}s are assigned the specified {@link IpSpace}.
 */
public final class ConstantIpSpaceAssignmentSpecifier implements IpSpaceAssignmentSpecifier {
  private final IpSpace _ipSpace;

  public ConstantIpSpaceAssignmentSpecifier(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConstantIpSpaceAssignmentSpecifier)) {
      return false;
    }
    ConstantIpSpaceAssignmentSpecifier that = (ConstantIpSpaceAssignmentSpecifier) o;
    return Objects.equals(_ipSpace, that._ipSpace);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ipSpace);
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    return IpSpaceAssignment.builder().assign(locations, _ipSpace).build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ConstantIpSpaceAssignmentSpecifier.class)
        .add("ipSpace", _ipSpace)
        .toString();
  }
}
