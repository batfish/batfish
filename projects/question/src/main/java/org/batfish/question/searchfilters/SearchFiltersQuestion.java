package org.batfish.question.searchfilters;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.searchfilters.SearchFiltersQuestion.Type.DENY;
import static org.batfish.question.searchfilters.SearchFiltersQuestion.Type.MATCH_LINE;
import static org.batfish.question.searchfilters.SearchFiltersQuestion.Type.PERMIT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.SearchFiltersParameters;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.FlexibleUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/** A question to determine which flows match a particular ACL action. */
public final class SearchFiltersQuestion extends Question {

  private static final String PROP_ACTION = "action";
  private static final String PROP_COMPLEMENT_HEADERSPACE = "invertSearch";
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_GENERATE_EXPLANATIONS = "explain";
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_START_LOCATION = "startLocation";

  public enum Type {
    PERMIT,
    DENY,
    MATCH_LINE
  }

  private final boolean _complementHeaderSpace;
  // Invariant: null unless _type == MATCH_LINE
  @Nullable private final String _filters;
  private final boolean _generateExplanations;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;
  @Nullable private Integer _lineNumber;
  @Nullable private final String _nodes;
  @Nullable private final String _startLocation;
  @Nonnull private Type _type = PERMIT;

  @JsonCreator
  private SearchFiltersQuestion(
      @JsonProperty(PROP_COMPLEMENT_HEADERSPACE) boolean complementHeaderSpace,
      @JsonProperty(PROP_FILTERS) @Nullable String filters,
      @JsonProperty(PROP_GENERATE_EXPLANATIONS) boolean generateExplanations,
      @JsonProperty(PROP_HEADERS) @Nullable PacketHeaderConstraints headerConstraints,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_START_LOCATION) @Nullable String start,
      @JsonProperty(PROP_ACTION) @Nullable String type) {
    _complementHeaderSpace = complementHeaderSpace;
    _filters = filters;
    _generateExplanations = generateExplanations;
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _nodes = nodes;
    _startLocation = start;
    setQuery(firstNonNull(type, "permit"));
  }

  SearchFiltersQuestion() {
    this(false, null, false, null, null, null, null);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "searchfilters";
  }

  @JsonProperty(PROP_ACTION)
  private void setQuery(String query) {
    if (query.equals("permit")) {
      _type = PERMIT;
      _lineNumber = null;
    } else if (query.equals("deny")) {
      _type = DENY;
      _lineNumber = null;
    } else if (query.startsWith("matchLine")) {
      _type = MATCH_LINE;
      _lineNumber = Integer.parseInt(query.substring("matchLine".length()).trim());
    } else {
      throw new BatfishException("Unrecognized query: " + query);
    }
  }

  @JsonProperty(PROP_COMPLEMENT_HEADERSPACE)
  public boolean getComplementHeaderSpace() {
    return _complementHeaderSpace;
  }

  @JsonProperty(PROP_GENERATE_EXPLANATIONS)
  public boolean getGenerateExplanations() {
    return _generateExplanations;
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
  public FilterSpecifier getFilterSpecifier() {
    return FilterSpecifierFactory.load(FlexibleFilterSpecifierFactory.NAME)
        .buildFilterSpecifier(_filters);
  }

  @JsonIgnore
  @Nullable
  public Integer getLineNumber() {
    return _lineNumber;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @Nonnull
  private LocationSpecifier getStartLocationSpecifier() {
    return new FlexibleLocationSpecifierFactory().buildLocationSpecifier(_startLocation);
  }

  @Nonnull
  private IpSpaceSpecifier getSourceSpecifier() {
    return IpSpaceSpecifierFactory.load(FlexibleUniverseIpSpaceSpecifierFactory.NAME)
        .buildIpSpaceSpecifier(_headerConstraints.getSrcIps());
  }

  @Nonnull
  private IpSpaceSpecifier getDestinationSpecifier() {
    return IpSpaceSpecifierFactory.load(FlexibleUniverseIpSpaceSpecifierFactory.NAME)
        .buildIpSpaceSpecifier(_headerConstraints.getDstIps());
  }

  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME).buildNodeSpecifier(_nodes);
  }

  @VisibleForTesting
  @Nonnull
  HeaderSpace getHeaderSpace() {
    return PacketHeaderConstraintsUtil.toHeaderSpaceBuilder(_headerConstraints)
        .setNegate(_complementHeaderSpace)
        .build();
  }

  @Nonnull
  @VisibleForTesting
  SearchFiltersParameters toSearchFiltersParameters() {
    return SearchFiltersParameters.builder()
        .setDestinationIpSpaceSpecifier(getDestinationSpecifier())
        .setGenerateExplanations(_generateExplanations)
        .setHeaderSpace(getHeaderSpace())
        .setSourceIpSpaceSpecifier(getSourceSpecifier())
        .setStartLocationSpecifier(getStartLocationSpecifier())
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  public static final class Builder {
    private boolean _complementHeaderSpace;
    private @Nullable String _filters;
    private boolean _generateExplanations;
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

    public Builder setGenerateExplanations(boolean generateExplanations) {
      _generateExplanations = generateExplanations;
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
          _generateExplanations,
          _headers,
          _nodeSpecifierInput,
          _startLocation,
          _type);
    }
  }
}
