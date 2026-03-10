package org.batfish.common.autocomplete;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;

/** Metadata about a {@link org.batfish.datamodel.Configuration} needed for autocomplete. */
@ParametersAreNonnullByDefault
public final class NodeCompletionMetadata implements Serializable {
  private static final String PROP_HUMAN_NAME = "humanName";

  private final @Nullable String _humanName;

  public NodeCompletionMetadata(@Nullable String humanName) {
    _humanName = humanName;
  }

  @JsonCreator
  private static NodeCompletionMetadata jsonCreator(
      @JsonProperty(PROP_HUMAN_NAME) @Nullable String humanName) {
    return new NodeCompletionMetadata(humanName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeCompletionMetadata)) {
      return false;
    }
    NodeCompletionMetadata that = (NodeCompletionMetadata) o;
    return Objects.equals(_humanName, that._humanName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_humanName);
  }

  /**
   * @return The node's {@link Configuration#getHumanName() human-readable name}.
   */
  @JsonProperty(PROP_HUMAN_NAME)
  public @Nullable String getHumanName() {
    return _humanName;
  }
}
