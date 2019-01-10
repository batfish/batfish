package org.batfish.question.vxlanproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;

/** A question that returns a table with VXLAN network segments and their properties. */
public final class VxlanVniPropertiesQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private NodesSpecifier _nodes;
  @Nonnull private VxlanVniPropertySpecifier _properties;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "vxlanVniProperties";
  }

  VxlanVniPropertiesQuestion(
      @JsonProperty(PROP_NODES) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTIES) VxlanVniPropertySpecifier propertySpec) {
    _nodes = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _properties = firstNonNull(propertySpec, VxlanVniPropertySpecifier.ALL);
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  public VxlanVniPropertySpecifier getProperties() {
    return _properties;
  }
}
