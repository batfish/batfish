package org.batfish.identifiers;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class Id {

  private final String _id;

  public Id(String id) {
    _id = id;
  }

  @Override
  public final boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Id)) {
      return false;
    }
    Id rhs = (Id) obj;
    return getClass().equals(rhs.getClass()) && _id.equals(rhs._id);
  }

  public @Nonnull String getId() {
    return _id;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_id);
  }
}
