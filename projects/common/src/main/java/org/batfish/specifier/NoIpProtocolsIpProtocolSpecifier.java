package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpProtocol;

/** A {@link IpProtocolSpecifier} the specifies the empty set of IP protocols */
public final class NoIpProtocolsIpProtocolSpecifier implements IpProtocolSpecifier {
  public static final NoIpProtocolsIpProtocolSpecifier INSTANCE =
      new NoIpProtocolsIpProtocolSpecifier();

  private NoIpProtocolsIpProtocolSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof NoIpProtocolsIpProtocolSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<IpProtocol> resolve() {
    return ImmutableSet.of();
  }
}
