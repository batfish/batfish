package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableSortedSet;
import javax.annotation.Nonnull;
import org.batfish.referencelibrary.AddressGroup;

/** Visitor that generates an {@link AddressGroup} for a {@link VirtualServerTarget}. */
public class VirtualServerTargetToAddressGroup implements VirtualServerTargetVisitor<AddressGroup> {

  private @Nonnull final String _name;

  public VirtualServerTargetToAddressGroup(String name) {
    _name = name;
  }

  @Override
  public @Nonnull AddressGroup visitAddress(VirtualServerTargetAddress address) {
    return new AddressGroup(ImmutableSortedSet.of(address.getAddress().toString()), _name);
  }
}
