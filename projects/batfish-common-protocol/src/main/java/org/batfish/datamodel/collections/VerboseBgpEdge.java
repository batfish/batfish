package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;

public final class VerboseBgpEdge implements Serializable {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final BgpPeerConfig _session1;
  @Nonnull private final BgpPeerConfig _session2;

  @JsonCreator
  public VerboseBgpEdge(
      @Nonnull @JsonProperty(PROP_NODE1_SESSION) BgpPeerConfig s1,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION) BgpPeerConfig s2,
      @Nonnull @JsonProperty(PROP_EDGE_SUMMARY) IpEdge e) {
    this._session1 = s1;
    this._session2 = s2;
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_SESSION)
  public BgpPeerConfig getNode1Session() {
    return _session1;
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public BgpPeerConfig getNode2Session() {
    return _session2;
  }
}
