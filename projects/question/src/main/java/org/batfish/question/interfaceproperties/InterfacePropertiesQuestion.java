package org.batfish.question.interfaceproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of interfaces in a tabular format. {@link #_nodes}, {@link
 * #_interfaces}, and {@link #_properties} determine which nodes, interfaces, and properties are
 * included. The default is to include everything.
 */
public class InterfacePropertiesQuestion extends Question {

  static final boolean DEFAULT_EXCLUDE_SHUT_INTERFACES = false;

  private static final String PROP_EXCLUDE_SHUT_INTERFACES = "excludeShutInterfaces";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private InterfacesSpecifier _interfaces;
  @Nonnull private NodesSpecifier _nodes;
  private boolean _onlyActive;
  @Nonnull private InterfacePropertySpecifier _properties;

  public InterfacePropertiesQuestion(
      @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES) Boolean excludeShutInterfaces,
      @JsonProperty(PROP_INTERFACES) InterfacesSpecifier interfaceRegex,
      @JsonProperty(PROP_NODES) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTIES) InterfacePropertySpecifier propertySpec) {
    _onlyActive = firstNonNull(excludeShutInterfaces, DEFAULT_EXCLUDE_SHUT_INTERFACES);
    _interfaces = firstNonNull(interfaceRegex, InterfacesSpecifier.ALL);
    _nodes = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _properties = firstNonNull(propertySpec, InterfacePropertySpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "interfaceProperties";
  }

  @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES)
  public boolean getOnlyActive() {
    return _onlyActive;
  }

  @JsonProperty(PROP_INTERFACES)
  public InterfacesSpecifier getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  public InterfacePropertySpecifier getProperties() {
    return _properties;
  }
}
