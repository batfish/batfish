package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;

/** Based on node configurations, determines the compatibility of IBGP and EBGP sessions. */
public class BgpSessionCompatibilityQuestion extends BgpSessionQuestion {

  /** Create a new BGP session compatibility question with default parameters. */
  public BgpSessionCompatibilityQuestion() {
    super(null, null, null, null);
  }

  /**
   * Create a new BGP session compatibility question.
   *
   * @param nodes Regular expression to match the nodes names for one end of the sessions. Default
   *     is '.*' (all nodes).
   * @param remoteNodes Regular expression to match the nodes names for the other end of the
   *     sessions. Default is '.*' (all nodes).
   * @param status Regular expression to match status type (see {@link ConfiguredSessionStatus})
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  @JsonCreator
  public BgpSessionCompatibilityQuestion(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_REMOTE_NODES) String remoteNodes,
      @Nullable @JsonProperty(PROP_STATUS) String status,
      @Nullable @JsonProperty(PROP_TYPE) String type) {
    super(nodes, remoteNodes, status, type);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "bgpSessionCompatibility";
  }
}
