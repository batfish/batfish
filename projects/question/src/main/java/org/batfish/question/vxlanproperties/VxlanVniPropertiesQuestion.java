package org.batfish.question.vxlanproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question that returns a table with VXLAN network segments and their properties. */
@ParametersAreNonnullByDefault
public final class VxlanVniPropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private String _nodes;
  @Nullable private String _properties;

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "vxlanVniProperties";
  }

  @JsonCreator
  private static @Nonnull VxlanVniPropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_PROPERTIES) String properties) {
    return new VxlanVniPropertiesQuestion(nodes, properties);
  }

  public VxlanVniPropertiesQuestion(@Nullable String nodes, @Nullable String properties) {
    _nodes = nodes;
    _properties = properties;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  public @Nullable String getProperties() {
    return _properties;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof VxlanVniPropertiesQuestion)) {
      return false;
    }
    VxlanVniPropertiesQuestion that = (VxlanVniPropertiesQuestion) o;
    return Objects.equals(_nodes, that._nodes) && Objects.equals(_properties, that._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _properties);
  }
}
