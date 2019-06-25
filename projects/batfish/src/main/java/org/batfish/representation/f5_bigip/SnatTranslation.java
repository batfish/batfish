package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for an snat-translation. */
@ParametersAreNonnullByDefault
public final class SnatTranslation implements Serializable {

  private @Nullable Ip _address;

  private @Nullable Ip6 _address6;

  private final @Nonnull String _name;

  public SnatTranslation(String name) {
    _name = name;
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }
}
