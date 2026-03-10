package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.cisco_xr.HsrpAddressFamily.Type;

/** Settings at the {@code router hsrp > interface foo} level. */
@ParametersAreNonnullByDefault
public final class HsrpInterface implements Serializable {
  public HsrpInterface(String name) {
    _name = name;
    _addressFamilies = new EnumMap<>(Type.class);
  }

  public @Nullable HsrpAddressFamily getAddressFamily(Type type) {
    return _addressFamilies.get(type);
  }

  public @Nonnull HsrpAddressFamily getOrCreateAddressFamily(Type type) {
    return _addressFamilies.computeIfAbsent(type, HsrpAddressFamily::new);
  }

  public @Nonnull Map<Type, HsrpAddressFamily> getAddressFamilies() {
    return Collections.unmodifiableMap(_addressFamilies);
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final Map<Type, HsrpAddressFamily> _addressFamilies;
  private final String _name;
}
