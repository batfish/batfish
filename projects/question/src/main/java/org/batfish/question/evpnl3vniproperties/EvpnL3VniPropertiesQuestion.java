package org.batfish.question.evpnl3vniproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question that returns a table with VXLAN network segments and their properties. */
@ParametersAreNonnullByDefault
public final class EvpnL3VniPropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";

  @Nullable private String _nodes;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "evpnL3VniProperties";
  }

  @JsonCreator
  private static @Nonnull EvpnL3VniPropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes) {
    return new EvpnL3VniPropertiesQuestion(nodes);
  }

  public EvpnL3VniPropertiesQuestion(@Nullable String nodes) {
    _nodes = nodes;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof EvpnL3VniPropertiesQuestion)) {
      return false;
    }
    EvpnL3VniPropertiesQuestion that = (EvpnL3VniPropertiesQuestion) o;
    return Objects.equals(_nodes, that._nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_nodes);
  }
}
