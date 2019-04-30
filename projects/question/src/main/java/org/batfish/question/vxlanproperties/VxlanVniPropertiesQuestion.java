package org.batfish.question.vxlanproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns a table with VXLAN network segments and their properties. */
@ParametersAreNonnullByDefault
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
  private static @Nonnull VxlanVniPropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_PROPERTIES) VxlanVniPropertySpecifier propertySpec) {
    return new VxlanVniPropertiesQuestion(
        nodes, firstNonNull(propertySpec, VxlanVniPropertySpecifier.ALL));
  }

  public VxlanVniPropertiesQuestion(
      @Nullable String nodes, VxlanVniPropertySpecifier propertySpec) {
    _nodes = nodes;
    _properties = propertySpec;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_PROPERTIES)
  public @Nonnull VxlanVniPropertySpecifier getProperties() {
    return _properties;
  }
}
