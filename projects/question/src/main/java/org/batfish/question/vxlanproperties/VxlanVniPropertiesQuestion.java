package org.batfish.question.vxlanproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/** A question that returns a table with VXLAN network segments and their properties. */
public final class VxlanVniPropertiesQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private String _nodes;
  @Nonnull private VxlanVniPropertySpecifier _properties;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "vxlanVniProperties";
  }

  @JsonCreator
  private static VxlanVniPropertiesQuestion create(
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_PROPERTIES) VxlanVniPropertySpecifier propertySpec) {
    return new VxlanVniPropertiesQuestion(
        nodes, firstNonNull(propertySpec, VxlanVniPropertySpecifier.ALL));
  }

  VxlanVniPropertiesQuestion(
      @Nullable String nodes, @Nonnull VxlanVniPropertySpecifier propertySpec) {
    _nodes = nodes;
    _properties = propertySpec;
  }

  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME).buildNodeSpecifier(_nodes);
  }

  @JsonProperty(PROP_PROPERTIES)
  public VxlanVniPropertySpecifier getProperties() {
    return _properties;
  }
}
