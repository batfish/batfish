package org.batfish.datamodel.acl;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An event in an {@link AclTrace}. */
@ParametersAreNonnullByDefault
public final class TraceEvent implements Serializable {
  public static final String PROP_DESCRIPTION = "description";

  private final @Nonnull String _description;

  public TraceEvent(String description) {
    _description = description;
  }

  @JsonCreator
  private static TraceEvent jsonCreator(
      @Nullable @JsonProperty(PROP_DESCRIPTION) String description) {
    return new TraceEvent(firstNonNull(description, ""));
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nonnull String getDescription() {
    return _description;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TraceEvent)) {
      return false;
    }
    TraceEvent other = (TraceEvent) o;
    return _description.equals(other._description);
  }

  @Override
  public int hashCode() {
    return _description.hashCode();
  }
}
