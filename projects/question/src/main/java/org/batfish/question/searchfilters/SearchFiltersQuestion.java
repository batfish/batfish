package org.batfish.question.searchfilters;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.specifier.AllFiltersFilterSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question to determine which flows match a particular ACL action. */
public final class SearchFiltersQuestion extends Question {
  private static final String PROP_COMPLEMENT_HEADERSPACE = "invertSearch";
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_START_LOCATION = "startLocation";
  private static final String PROP_ACTION = "action";

  // Retained for backwards compatibility
  private static final String PROP_GENERATE_EXPLANATIONS = "explain";

  private static final PacketHeaderConstraints DEFAULT_HEADER_CONSTRAINTS =
      PacketHeaderConstraints.unconstrained();
  private static final String DEFAULT_ACTION = "permit";

  private final boolean _complementHeaderSpace;
  @Nullable private final String _filters;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;
  @Nullable private final String _nodes;
  @Nonnull private final SearchFiltersQuery _query;
  @Nullable private final String _startLocation;

  // Redundant with _query. Present for JSON serialization only, to avoid needing a new type of
  // parameter in the question template.
  @Nonnull private String _action;

  @JsonCreator
  private static SearchFiltersQuestion create(
      @Nullable @JsonProperty(PROP_COMPLEMENT_HEADERSPACE) Boolean complementHeaderSpace,
      @Nullable @JsonProperty(PROP_FILTERS) String filters,
      @Nullable @JsonProperty(PROP_GENERATE_EXPLANATIONS) Boolean generateExplanations,
      @Nullable @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headerConstraints,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_START_LOCATION) String start,
      @Nullable @JsonProperty(PROP_ACTION) String action) {
    return new SearchFiltersQuestion(
        firstNonNull(complementHeaderSpace, false),
        filters,
        firstNonNull(headerConstraints, DEFAULT_HEADER_CONSTRAINTS),
        nodes,
        start,
        firstNonNull(action, DEFAULT_ACTION));
  }

  private SearchFiltersQuestion(
      boolean complementHeaderSpace,
      @Nullable String filters,
      @Nonnull PacketHeaderConstraints headerConstraints,
      @Nullable String nodes,
      @Nullable String start,
      @Nonnull String action) {
    _complementHeaderSpace = complementHeaderSpace;
    _filters = filters;
    _headerConstraints = headerConstraints;
    _nodes = nodes;
    _startLocation = start;
    _query = generateQuery(action);
    _action = action;
  }

  SearchFiltersQuestion() {
    this(false, null, DEFAULT_HEADER_CONSTRAINTS, null, null, DEFAULT_ACTION);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "searchfilters";
  }

  private SearchFiltersQuery generateQuery(String type) {
    if (type.equalsIgnoreCase("permit")) {
      return PermitQuery.INSTANCE;
    } else if (type.equalsIgnoreCase("deny")) {
      return DenyQuery.INSTANCE;
    } else if (type.toLowerCase().startsWith("matchline")) {
      int lineNum = Integer.parseInt(type.toLowerCase().substring("matchLine".length()).trim());
      if (lineNum < 0) {
        throw new BatfishException("Line index for matchLine must be zero or higher");
      }
      return new MatchLineQuery(lineNum);
    } else {
      throw new BatfishException(
          String.format(
              "Unrecognized action '%s'. Must be one of 'permit', 'deny', or 'matchLine <line"
                  + " index>'.",
              type));
    }
  }

  @JsonProperty(PROP_ACTION)
  private String getAction() {
    return _action;
  }

  @JsonProperty(PROP_COMPLEMENT_HEADERSPACE)
  public boolean getComplementHeaderSpace() {
    return _complementHeaderSpace;
  }

  @Nullable
  @JsonProperty(PROP_FILTERS)
  public String getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_HEADERS)
  @Nonnull
  public PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_START_LOCATION)
  @Nullable
  public String getStartLocation() {
    return _startLocation;
  }

  @Nonnull
  @JsonIgnore
  FilterSpecifier getFilterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(
        _filters, AllFiltersFilterSpecifier.INSTANCE);
  }

  @Nonnull
  @JsonIgnore
  public SearchFiltersQuery getQuery() {
    return _query;
  }

  @Nonnull
  private LocationSpecifier getStartLocationSpecifier() {
    return SpecifierFactories.getLocationSpecifierOrDefault(
        _startLocation, LocationSpecifier.ALL_LOCATIONS);
  }

  @Nonnull
  private IpSpaceSpecifier getSourceSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _headerConstraints.getSrcIps(), new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE));
  }

  @Nonnull
  private IpSpaceSpecifier getDestinationSpecifier() {
    return SpecifierFactories.getIpSpaceSpecifierOrDefault(
        _headerConstraints.getDstIps(), new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE));
  }

  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @VisibleForTesting
  @Nonnull
  AclLineMatchExpr getHeaderSpaceExpr() {
    return PacketHeaderConstraintsUtil.toAclLineMatchExpr(_headerConstraints);
  }

  @Nonnull
  @VisibleForTesting
  SearchFiltersParameters toSearchFiltersParameters() {
    return SearchFiltersParameters.builder()
        .setDestinationIpSpaceSpecifier(getDestinationSpecifier())
        .setHeaderSpaceExpr(getHeaderSpaceExpr())
        .setSourceIpSpaceSpecifier(getSourceSpecifier())
        .setStartLocationSpecifier(getStartLocationSpecifier())
        .setComplementHeaderSpace(_complementHeaderSpace)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  public static final class Builder {
    private boolean _complementHeaderSpace;
    private @Nullable String _filters;
    private @Nullable PacketHeaderConstraints _headers;
    private @Nullable String _nodeSpecifierInput;
    private @Nullable String _startLocation;
    private @Nullable String _type;

    private Builder() {}

    public Builder setAction(String type) {
      _type = type;
      return this;
    }

    public Builder setComplementHeaderSpace(boolean complementHeaderSpace) {
      _complementHeaderSpace = complementHeaderSpace;
      return this;
    }

    public Builder setFilterSpecifier(String filterSpecifier) {
      _filters = filterSpecifier;
      return this;
    }

    public Builder setHeaders(@Nullable PacketHeaderConstraints headers) {
      _headers = headers;
      return this;
    }

    public Builder setNodeSpecifier(String nodeSpecifier) {
      _nodeSpecifierInput = nodeSpecifier;
      return this;
    }

    public Builder setStartLocation(String start) {
      _startLocation = start;
      return this;
    }

    public SearchFiltersQuestion build() {
      return new SearchFiltersQuestion(
          _complementHeaderSpace,
          _filters,
          firstNonNull(_headers, DEFAULT_HEADER_CONSTRAINTS),
          _nodeSpecifierInput,
          _startLocation,
          firstNonNull(_type, DEFAULT_ACTION));
    }
  }
}
