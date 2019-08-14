package org.batfish.datamodel;

import javax.annotation.Nonnull;

public final class DhcpInterfaceAddress extends InterfaceAddress {

  private static final DhcpInterfaceAddress INSTANCE = new DhcpInterfaceAddress();

  public static @Nonnull DhcpInterfaceAddress instance() {
    return INSTANCE;
  }

  private DhcpInterfaceAddress() {}

  @Override
  public int compareTo(InterfaceAddress o) {
    if (this == o || o instanceof DhcpInterfaceAddress) {
      return 0;
    }
    return getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof DhcpInterfaceAddress;
  }

  @Override
  public int hashCode() {
    return 0xB5220E5; // randomly generated
  }
}
