package org.batfish.question.ipsecsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Return status of all IPSec sessions in the network */
public class IpsecSessionStatusQuestion extends Question {
  static final Pattern DEFAULT_STATUS = Pattern.compile(".*");
  private static final String PROP_NODES = "nodes";
  private static final String PROP_REMOTE_NODES = "remoteNodes";
  private static final String PROP_STATUS = "status";

  private static final String QUESTION_NAME = "ipsecSessionStatus";

  @Nullable private String _nodes;

  @Nullable private String _remoteNodes;

  @Nonnull private Pattern _status;

  @JsonCreator
  private static IpsecSessionStatusQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) String remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status) {
    return new IpsecSessionStatusQuestion(
        nodes,
        remoteNodes,
        Strings.isNullOrEmpty(status) ? DEFAULT_STATUS : Pattern.compile(status.toUpperCase()));
  }

  public IpsecSessionStatusQuestion(
      @Nullable String nodes, @Nullable String remoteNodes, @Nonnull Pattern status) {
    _nodes = nodes;
    _remoteNodes = remoteNodes;
    _status = status;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return QUESTION_NAME;
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @Nullable
  @JsonProperty(PROP_REMOTE_NODES)
  public String getRemoteNodes() {
    return _remoteNodes;
  }

  @JsonIgnore
  NodeSpecifier getRemoteNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _remoteNodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_STATUS)
  public String getStatus() {
    return _status.toString();
  }

  boolean matchesStatus(@Nullable IpsecSessionStatus status) {
    return status != null && _status.matcher(status.toString()).matches();
  }
}
