package org.batfish.question.ospfinterface;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question that returns a table with all OSPF interfaces configurations */
@ParametersAreNonnullByDefault
public final class OspfInterfaceConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private final String _nodes;
  @Nullable private final String _properties;

  @JsonCreator
  private static OspfInterfaceConfigurationQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_PROPERTIES) @Nullable String properties) {
    return new OspfInterfaceConfigurationQuestion(nodes, properties);
  }

  public OspfInterfaceConfigurationQuestion(@Nullable String nodes, @Nullable String properties) {
    _nodes = nodes;
    _properties = properties;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "ospfInterfaceConfiguration";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  @Nullable
  public String getProperties() {
    return _properties;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof OspfInterfaceConfigurationQuestion)) {
      return false;
    }
    OspfInterfaceConfigurationQuestion that = (OspfInterfaceConfigurationQuestion) o;
    return Objects.equals(_nodes, that._nodes) && Objects.equals(_properties, that._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _properties);
  }
}
