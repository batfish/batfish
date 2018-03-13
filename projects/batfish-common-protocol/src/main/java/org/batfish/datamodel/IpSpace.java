package org.batfish.datamodel;

import javax.annotation.Nonnull;

public interface IpSpace {
  public static final IpSpace UNIVERSE =
      new IpSpace() {
        @Override
        public boolean contains(@Nonnull Ip ip) {
          return true;
        }
      };

  boolean contains(@Nonnull Ip ip);
}
