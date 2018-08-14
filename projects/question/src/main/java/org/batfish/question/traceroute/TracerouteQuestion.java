package org.batfish.question.traceroute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.questions.IPacketTraceQuestion;
import org.batfish.specifier.FlexibleInferFromLocationIpSpaceSpecifierFactory;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;

/**
 * A question to perform a traceroute.
 *
 * <p>This question performs a virtual traceroute in the network from ingress node. The destination
 * IP is randomly picked if not explicitly specified. Other IP headers are also randomly picked if
 * unspecified, with a bias toward generating packets similar to a real traceroute (see below).
 *
 * <p>Unlike a real traceroute, this traceroute is directional. That is, for it to succeed, the
 * reverse connectivity is not needed. This feature can help debug connectivity issues by decoupling
 * the two directions.
 */
public final class TracerouteQuestion extends IPacketTraceQuestion {
  private static final String DEFAULT_SOURCE_LOCATION_SPECIFIER_FACTORY =
      FlexibleLocationSpecifierFactory.NAME;

  private static final String DEFAULT_SOURCE_IP_SPACE_SPECIFIER_FACTORY =
      FlexibleInferFromLocationIpSpaceSpecifierFactory.NAME;

  private static final String PROP_IGNORE_ACLS = "ignoreAcls";

  private static final String PROP_SOURCE_LOCATION_SPECIFIER_FACTORY =
      "traceStartLocationSpecifierFactory";

  private static final String PROP_SOURCE_LOCATION_SPECIFIER_INPUT = "traceStart";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY = "srcIpSpaceSpecifierFactory";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT = "srcIpSpace";

  private boolean _ignoreAcls = false;

  private String _sourceLocationSpecifierFactory = DEFAULT_SOURCE_LOCATION_SPECIFIER_FACTORY;

  private @Nullable String _sourceLocationSpecifierInput = null;

  private String _sourceIpSpaceSpecifierFactory = DEFAULT_SOURCE_IP_SPACE_SPECIFIER_FACTORY;

  private @Nullable String _sourceIpSpaceSpecifierInput = null;

  public TracerouteQuestion() {}

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(PROP_IGNORE_ACLS)
  public boolean getIgnoreAcls() {
    return _ignoreAcls;
  }

  @JsonIgnore
  public Set<Builder> getFlowBuilders() {
    return Collections.singleton(createBaseFlowBuilder());
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_FACTORY)
  public @Nonnull String getSourceLocationSpecifierFactory() {
    return _sourceLocationSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_INPUT)
  public @Nullable String getSourceLocationSpecifierInput() {
    return _sourceLocationSpecifierInput;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public @Nonnull String getSourceIpSpaceSpecifierFactory() {
    return _sourceIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  public @Nullable String getSourceIpSpaceSpecifierInput() {
    return _sourceIpSpaceSpecifierInput;
  }

  @Override
  public String getName() {
    return "traceroute2";
  }

  @Override
  public String prettyPrint() {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("traceroute %s", prettyPrintBase()));
      if (!_sourceLocationSpecifierFactory.equals(DEFAULT_SOURCE_LOCATION_SPECIFIER_FACTORY)) {
        sb.append(
            String.format(
                ", %s=%s",
                PROP_SOURCE_LOCATION_SPECIFIER_FACTORY, _sourceLocationSpecifierFactory));
      }
      if (_sourceLocationSpecifierInput != null) {
        sb.append(
            String.format(
                ", %s=%s", PROP_SOURCE_LOCATION_SPECIFIER_INPUT, _sourceLocationSpecifierInput));
      }
      if (_sourceIpSpaceSpecifierFactory.equals(DEFAULT_SOURCE_IP_SPACE_SPECIFIER_FACTORY)) {
        sb.append(
            String.format(
                ", %s=%s", PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY, _sourceIpSpaceSpecifierFactory));
      }
      if (_sourceIpSpaceSpecifierInput != null) {
        sb.append(
            String.format(
                ", %s=%s", PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY, _sourceIpSpaceSpecifierInput));
      }
      return sb.toString();
    } catch (Exception e) {
      try {
        return "Pretty printing failed. Printing Json\n" + toJsonString();
      } catch (BatfishException e1) {
        throw new BatfishException("Both pretty and json printing failed\n");
      }
    }
  }

  @JsonProperty(PROP_IGNORE_ACLS)
  public void setIgnoreAcls(boolean ignoreAcls) {
    _ignoreAcls = ignoreAcls;
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_FACTORY)
  public void setSourceLocationSpecifierFactory(String sourceLocationSpecifierFactory) {
    _sourceLocationSpecifierFactory = sourceLocationSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_INPUT)
  public void setSourceLocationSpecifierInput(String sourceLocationSpecifierInput) {
    _sourceLocationSpecifierInput = sourceLocationSpecifierInput;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public void setSourceIpSpaceSpecifierFactory(String sourceIpSpaceSpecifierFactory) {
    _sourceIpSpaceSpecifierFactory = sourceIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  public void setSourceIpSpaceSpecifierInput(String sourceIpSpaceSpecifierInput) {
    _sourceIpSpaceSpecifierInput = sourceIpSpaceSpecifierInput;
  }
}
