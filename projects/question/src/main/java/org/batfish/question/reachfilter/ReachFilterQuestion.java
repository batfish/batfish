package org.batfish.question.reachfilter;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.DENY;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.MATCH_LINE;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.PERMIT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ReachFilterParameters;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.FlexibleUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/** A question to determine which flows match a particular ACL action. */
public final class ReachFilterQuestion extends Question {

  private static final String DEFAULT_DST_IP_SPECIFIER_FACTORY =
      FlexibleUniverseIpSpaceSpecifierFactory.NAME;

  private static final String DEFAULT_SRC_IP_SPECIFIER_FACTORY =
      FlexibleUniverseIpSpaceSpecifierFactory.NAME;

  private static final String FILTER_SPECIFIER_FACTORY = FlexibleFilterSpecifierFactory.NAME;

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filterRegex";

  // TODO: this should probably be "action" for consistency
  private static final String PROP_QUERY = "query";

  private static final String PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY =
      "destinationIpSpaceSpecifierFactory";

  private static final String PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT = "dst";

  private static final String PROP_DST_PORTS = "dstPorts";

  private static final String PROP_DST_PROTOCOLS = "dstProtocols";

  private static final String PROP_NODE_SPECIFIER_FACTORY = "nodeSpecifierFactory";

  private static final String PROP_NODE_SPECIFIER_INPUT = "nodeSpecifierInput";

  private static final String PROP_SRC_PORTS = "srcPorts";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY =
      "sourceIpSpaceSpecifierFactory";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT = "src";

  public enum Type {
    PERMIT,
    DENY,
    MATCH_LINE
  }

  // Invariant: null unless _type == MATCH_LINE
  @Nullable private Integer _lineNumber;

  @Nullable private final String _filterSpecifierInput;

  @Nonnull private final String _nodeSpecifierFactory;

  @Nullable private final String _nodeSpecifierInput;

  @Nonnull private final String _destinationIpSpaceSpecifierFactory;

  @Nullable private final String _destinationIpSpaceSpecifierInput;

  @Nonnull private final SortedSet<SubRange> _dstPorts;

  @Nonnull private final SortedSet<SubRange> _srcPorts;

  @Nonnull private final SortedSet<Protocol> _dstProtocols;

  @Nonnull private final String _sourceIpSpaceSpecifierFactory;

  @Nullable private final String _sourceIpSpaceSpecifierInput;

  @Nonnull private Type _type = PERMIT;

  @JsonCreator
  private ReachFilterQuestion(
      @JsonProperty(PROP_FILTER_SPECIFIER_INPUT) @Nullable String filterSpecifierInput,
      @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY) @Nullable
          String destinationIpSpaceSpecifierFactory,
      @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT) @Nullable
          String destinationIpSpaceSpecifierInput,
      @JsonProperty(PROP_DST_PORTS) @Nullable SortedSet<SubRange> dstPorts,
      @JsonProperty(PROP_SRC_PORTS) @Nullable SortedSet<SubRange> srcPorts,
      @JsonProperty(PROP_DST_PROTOCOLS) @Nullable SortedSet<Protocol> dstProtocols,
      @JsonProperty(PROP_NODE_SPECIFIER_FACTORY) @Nullable String nodeSpecifierFactory,
      @JsonProperty(PROP_NODE_SPECIFIER_INPUT) @Nullable String nodesSpecifierInput,
      @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY) @Nullable
          String sourceIpSpaceSpecifierFactory,
      @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT) @Nullable
          String sourceIpSpaceSpecifierInput,
      @JsonProperty(PROP_QUERY) @Nullable String type) {
    _filterSpecifierInput = filterSpecifierInput;
    _nodeSpecifierFactory = firstNonNull(nodeSpecifierFactory, FlexibleNodeSpecifierFactory.NAME);
    _nodeSpecifierInput = nodesSpecifierInput;
    _destinationIpSpaceSpecifierFactory =
        firstNonNull(destinationIpSpaceSpecifierFactory, DEFAULT_DST_IP_SPECIFIER_FACTORY);
    _destinationIpSpaceSpecifierInput = destinationIpSpaceSpecifierInput;
    _dstPorts = firstNonNull(dstPorts, ImmutableSortedSet.of());
    _srcPorts = firstNonNull(srcPorts, ImmutableSortedSet.of());
    _dstProtocols = firstNonNull(dstProtocols, ImmutableSortedSet.of());
    _sourceIpSpaceSpecifierFactory =
        firstNonNull(sourceIpSpaceSpecifierFactory, DEFAULT_SRC_IP_SPECIFIER_FACTORY);
    _sourceIpSpaceSpecifierInput = sourceIpSpaceSpecifierInput;
    setQuery(firstNonNull(type, "permit"));
  }

  ReachFilterQuestion() {
    this(null, null, null, null, null, null, null, null, null, null, null);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "reachfilter";
  }

  @Nonnull
  @JsonIgnore
  public FilterSpecifier getFilterSpecifier() {
    return FilterSpecifierFactory.load(FILTER_SPECIFIER_FACTORY)
        .buildFilterSpecifier(_filterSpecifierInput);
  }

  @Nullable
  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  private String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_FACTORY)
  @Nonnull
  public String getNodeSpecifierFactory() {
    return _nodeSpecifierFactory;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  @Nullable
  public String getNodeSpecifierInput() {
    return _nodeSpecifierInput;
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

  @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY)
  public String getDestinationIpSpaceSpecifierFactory() {
    return _destinationIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT)
  @Nullable
  public String getDestinationIpSpaceSpecifierInput() {
    return _destinationIpSpaceSpecifierInput;
  }

  @JsonProperty(PROP_DST_PORTS)
  public SortedSet<SubRange> getDstPorts() {
    return _dstPorts;
  }

  @JsonProperty(PROP_DST_PROTOCOLS)
  public SortedSet<Protocol> getDstProtocols() {
    return _dstProtocols;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public String getSourceIpSpaceSpecifierFactory() {
    return _sourceIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  @Nullable
  public String getSourceIpSpaceSpecifierInput() {
    return _sourceIpSpaceSpecifierInput;
  }

  @JsonProperty(PROP_SRC_PORTS)
  public SortedSet<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  @Nonnull
  private IpSpaceSpecifier getDestinationSpecifier() {
    return IpSpaceSpecifierFactory.load(_destinationIpSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_destinationIpSpaceSpecifierInput);
  }

  @Nonnull
  private IpSpaceSpecifier getSourceSpecifier() {
    return IpSpaceSpecifierFactory.load(_destinationIpSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_sourceIpSpaceSpecifierInput);
  }

  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return NodeSpecifierFactory.load(_nodeSpecifierFactory).buildNodeSpecifier(_nodeSpecifierInput);
  }

  @VisibleForTesting
  @Nonnull
  HeaderSpace getHeaderSpace() {
    return HeaderSpace.builder()
        .setDstPorts(_dstPorts)
        .setDstProtocols(_dstProtocols)
        .setSrcPorts(_srcPorts)
        .build();
  }

  @JsonProperty(PROP_QUERY)
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

  @Nonnull
  @VisibleForTesting
  ReachFilterParameters toReachFilterParameters() {
    return ReachFilterParameters.builder()
        .setHeaderSpace(getHeaderSpace())
        .setSourceIpSpaceSpecifier(getSourceSpecifier())
        .setDestinationIpSpaceSpecifier(getDestinationSpecifier())
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  static final class Builder {
    private String _filterSpecifierInput;
    private String _nodeSpecifierFactory;
    private String _nodeSpecifierInput;
    private String _destinationIpSpaceSpecifierFactory;
    private String _destinationIpSpaceSpecifierInput;
    private SortedSet<SubRange> _dstPorts;
    private SortedSet<SubRange> _srcPorts;
    private SortedSet<Protocol> _dstProtocols;
    private String _sourceIpSpaceSpecifierFactory;
    private String _sourceIpSpaceSpecifierInput;
    private String _type;

    private Builder() {}

    public Builder setFilterSpecifierInput(String filterSpecifierInput) {
      this._filterSpecifierInput = filterSpecifierInput;
      return this;
    }

    public Builder setNodeSpecifierFactory(String nodesSpecifierFactory) {
      this._nodeSpecifierFactory = nodesSpecifierFactory;
      return this;
    }

    public Builder setNodeSpecifierInput(String nodesSpecifierInput) {
      this._nodeSpecifierInput = nodesSpecifierInput;
      return this;
    }

    public Builder setDestinationIpSpaceSpecifierFactory(
        String destinationIpSpaceSpecifierFactory) {
      this._destinationIpSpaceSpecifierFactory = destinationIpSpaceSpecifierFactory;
      return this;
    }

    public Builder setDestinationIpSpaceSpecifierInput(String destinationIpSpaceSpecifierInput) {
      this._destinationIpSpaceSpecifierInput = destinationIpSpaceSpecifierInput;
      return this;
    }

    public Builder setDstPorts(SortedSet<SubRange> dstPorts) {
      this._dstPorts = dstPorts;
      return this;
    }

    public Builder setSrcPorts(SortedSet<SubRange> srcPorts) {
      this._srcPorts = srcPorts;
      return this;
    }

    public Builder setDstProtocols(SortedSet<Protocol> dstProtocols) {
      this._dstProtocols = dstProtocols;
      return this;
    }

    public Builder setSourceIpSpaceSpecifierFactory(String sourceIpSpaceSpecifierFactory) {
      this._sourceIpSpaceSpecifierFactory = sourceIpSpaceSpecifierFactory;
      return this;
    }

    public Builder setSourceIpSpaceSpecifierInput(String sourceIpSpaceSpecifierInput) {
      this._sourceIpSpaceSpecifierInput = sourceIpSpaceSpecifierInput;
      return this;
    }

    public Builder setQuery(String type) {
      this._type = type;
      return this;
    }

    public ReachFilterQuestion build() {
      return new ReachFilterQuestion(
          _filterSpecifierInput,
          _destinationIpSpaceSpecifierFactory,
          _destinationIpSpaceSpecifierInput,
          _dstPorts,
          _srcPorts,
          _dstProtocols,
          _nodeSpecifierFactory,
          _nodeSpecifierInput,
          _sourceIpSpaceSpecifierFactory,
          _sourceIpSpaceSpecifierInput,
          _type);
    }
  }
}
