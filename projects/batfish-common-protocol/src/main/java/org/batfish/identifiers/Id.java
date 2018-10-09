package org.batfish.identifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class Id {
  private static final String PROP_ID = "id";

  private final String _id;

  @JsonCreator
  public Id(@JsonProperty(PROP_ID) String id) {
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

  @JsonProperty(PROP_ID)
  public @Nonnull String getId() {
    return _id;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_id);
  }

  @Override
  public String toString() {
    return _id;
  }
}
