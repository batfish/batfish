package org.batfish.question.specifiers;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.specifier.SpecifierFactories.SpecifierType.FILTER;
import static org.batfish.specifier.SpecifierFactories.SpecifierType.INTERFACE;
import static org.batfish.specifier.SpecifierFactories.SpecifierType.IP_SPACE;
import static org.batfish.specifier.SpecifierFactories.SpecifierType.LOCATION;
import static org.batfish.specifier.SpecifierFactories.SpecifierType.NODE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.ShorthandFilterSpecifier;
import org.batfish.specifier.ShorthandInterfaceSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.SpecifierFactories.Version;

/**
 * Allows users to see how different specifiers ({@link LocationSpecifier}, {@link
 * IpSpaceSpecifier}, {@link NodeSpecifier}, {@link FilterSpecifier}, and {@link
 * InterfaceSpecifier}) are resolved.
 */
@ParametersAreNonnullByDefault
public final class SpecifiersQuestion extends Question {

  public enum QueryType {
    FILTER,
    INTERFACE,
    IP_SPACE,
    IP_SPACE_OF_LOCATION,
    LOCATION,
    NODE
  }

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filterSpecifierInput";
  private static final String PROP_INTERFACE_SPECIFIER_INPUT = "interfaceSpecifierInput";
  private static final String PROP_IP_SPACE_SPECIFIER_INPUT = "ipSpaceSpecifierInput";
  private static final String PROP_LOCATION_SPECIFIER_INPUT = "locationSpecifierInput";
  private static final String PROP_NODE_SPECIFIER_INPUT = "nodeSpecifierInput";

  private static final String PROP_QUERY_TYPE = "queryType";

  private static final String PROP_SPECIFIER_FACTORY_VERSION = "specifierFactoryVersion";

  @Nonnull private Version _specifierFactoryVersion = SpecifierFactories.ACTIVE_VERSION;

  @Nullable private String _filterSpecifierInput;
  @Nullable private String _interfaceSpecifierInput;
  @Nullable private String _ipSpaceSpecifierInput;
  @Nullable private String _locationSpecifierInput;
  @Nullable private String _nodeSpecifierInput;

  @Nonnull private QueryType _queryType;

  SpecifiersQuestion(@JsonProperty(PROP_QUERY_TYPE) QueryType queryType) {
    checkArgument(queryType != null, "'queryType must be specified");
    _queryType = queryType;
  }

  @JsonIgnore
  FilterSpecifier getFilterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(
        _filterSpecifierInput,
        new ShorthandFilterSpecifier(FiltersSpecifier.ALL),
        SpecifierFactories.getFactory(_specifierFactoryVersion, FILTER));
  }

  @JsonIgnore
  InterfaceSpecifier getInterfaceSpecifier() {
    return SpecifierFactories.getInterfaceSpecifierOrDefault(
        _interfaceSpecifierInput,
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL),
        SpecifierFactories.getFactory(_specifierFactoryVersion, INTERFACE));
  }

  @JsonIgnore
  IpSpaceSpecifier getIpSpaceSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _ipSpaceSpecifierInput,
        InferFromLocationIpSpaceSpecifier.INSTANCE,
        SpecifierFactories.getFactory(_specifierFactoryVersion, IP_SPACE));
  }

  @JsonIgnore
  LocationSpecifier getLocationSpecifier() {
    return SpecifierFactories.getLocationSpecifierOrDefault(
        _locationSpecifierInput,
        AllInterfacesLocationSpecifier.INSTANCE,
        SpecifierFactories.getFactory(_specifierFactoryVersion, LOCATION));
  }

  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _nodeSpecifierInput,
        AllNodesNodeSpecifier.INSTANCE,
        SpecifierFactories.getFactory(_specifierFactoryVersion, NODE));
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "specifiers";
  }

  @Nullable
  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  public String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @Nullable
  @JsonProperty(PROP_INTERFACE_SPECIFIER_INPUT)
  public String getInterfaceSpecifierInput() {
    return _interfaceSpecifierInput;
  }

  @Nullable
  @JsonProperty(PROP_IP_SPACE_SPECIFIER_INPUT)
  public String getIpSpaceSpecifierInput() {
    return _ipSpaceSpecifierInput;
  }

  @Nullable
  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public String getLocationSpecifierInput() {
    return _locationSpecifierInput;
  }

  @Nullable
  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  public String getNodeSpecifierInput() {
    return _nodeSpecifierInput;
  }

  @JsonProperty(PROP_QUERY_TYPE)
  public QueryType getQueryType() {
    return _queryType;
  }

  @JsonProperty(PROP_SPECIFIER_FACTORY_VERSION)
  public Version getSpecifierVersion() {
    return _specifierFactoryVersion;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  public void setFilterSpecifierInput(String filterSpecifierInput) {
    _filterSpecifierInput = filterSpecifierInput;
  }

  @JsonProperty(PROP_INTERFACE_SPECIFIER_INPUT)
  public void setInterfaceSpecifierInput(String interfaceSpecifierInput) {
    _interfaceSpecifierInput = interfaceSpecifierInput;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_INPUT)
  public void setIpSpaceSpecifierInput(String ipSpaceSpecifierInput) {
    _ipSpaceSpecifierInput = ipSpaceSpecifierInput;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public void setLocationSpecifierInput(String locationSpecifierInput) {
    _locationSpecifierInput = locationSpecifierInput;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  public void setNodeSpecifierInput(String nodeSpecifierInput) {
    _nodeSpecifierInput = nodeSpecifierInput;
  }

  @JsonProperty(PROP_SPECIFIER_FACTORY_VERSION)
  public void setSpecifierVersion(Version factoryVersion) {
    _specifierFactoryVersion = factoryVersion;
  }
}
