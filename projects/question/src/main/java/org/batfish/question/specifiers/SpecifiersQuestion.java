package org.batfish.question.specifiers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllFiltersFilterSpecifier;
import org.batfish.specifier.AllInterfacesInterfaceSpecifier;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
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
  private static final String PROP_SPECIFIER_FACTORY_VERSION = "specifierFactoryVersion";
  private static final String PROP_QUERY_TYPE = "queryType";

  @Nullable private String _filterSpecifierInput;
  @Nullable private String _interfaceSpecifierInput;
  @Nullable private String _ipSpaceSpecifierInput;
  @Nullable private String _locationSpecifierInput;
  @Nullable private String _nodeSpecifierInput;

  @Nonnull private QueryType _queryType;
  @Nonnull private Version _specifierFactoryVersion;

  @JsonCreator
  static SpecifiersQuestion create(
      @JsonProperty(PROP_QUERY_TYPE) QueryType queryType,
      @JsonProperty(PROP_SPECIFIER_FACTORY_VERSION) Version version) {
    return new SpecifiersQuestion(queryType, version);
  }

  public SpecifiersQuestion(QueryType queryType) {
    this(queryType, null);
  }

  public SpecifiersQuestion(QueryType queryType, @Nullable Version version) {
    checkArgument(queryType != null, "'queryType must be specified");
    _queryType = queryType;
    _specifierFactoryVersion = firstNonNull(version, SpecifierFactories.ACTIVE_VERSION);
  }

  @JsonIgnore
  FilterSpecifier getFilterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(
        _filterSpecifierInput, AllFiltersFilterSpecifier.INSTANCE, _specifierFactoryVersion);
  }

  @JsonIgnore
  InterfaceSpecifier getInterfaceSpecifier() {
    return SpecifierFactories.getInterfaceSpecifierOrDefault(
        _interfaceSpecifierInput,
        AllInterfacesInterfaceSpecifier.INSTANCE,
        _specifierFactoryVersion);
  }

  @JsonIgnore
  IpSpaceSpecifier getIpSpaceSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _ipSpaceSpecifierInput,
        InferFromLocationIpSpaceSpecifier.INSTANCE,
        _specifierFactoryVersion);
  }

  @JsonIgnore
  LocationSpecifier getLocationSpecifier() {
    return SpecifierFactories.getLocationSpecifierOrDefault(
        _locationSpecifierInput, AllInterfacesLocationSpecifier.INSTANCE, _specifierFactoryVersion);
  }

  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _nodeSpecifierInput, AllNodesNodeSpecifier.INSTANCE, _specifierFactoryVersion);
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
}
