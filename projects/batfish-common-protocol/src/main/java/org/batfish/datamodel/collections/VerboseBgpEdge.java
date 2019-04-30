package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;

public final class VerboseBgpEdge implements Serializable {
  private static final String PROP_EDGE_SUMMARY = "edgeSummary";
  private static final String PROP_NODE1_SESSION = "node1Session";
  private static final String PROP_NODE1_SESSION_ID = "node1SessionId";
  private static final String PROP_NODE2_SESSION = "node2Session";
  private static final String PROP_NODE2_SESSION_ID = "node2SessionId";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final BgpPeerConfig _session1;
  @Nonnull private final BgpPeerConfig _session2;
  @Nonnull private final BgpPeerConfigId _session1id;
  @Nonnull private final BgpPeerConfigId _session2id;

  @JsonCreator
  public VerboseBgpEdge(
      @Nonnull @JsonProperty(PROP_NODE1_SESSION) BgpPeerConfig s1,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION) BgpPeerConfig s2,
      @Nonnull @JsonProperty(PROP_NODE1_SESSION_ID) BgpPeerConfigId s1id,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION_ID) BgpPeerConfigId s2id,
      @Nonnull @JsonProperty(PROP_EDGE_SUMMARY) IpEdge e) {
    _session1 = s1;
    _session2 = s2;
    _session1id = s1id;
    _session2id = s2id;
    _edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  @Nonnull
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_SESSION)
  @Nonnull
  public BgpPeerConfig getNode1Session() {
    return _session1;
  }

  @JsonProperty(PROP_NODE2_SESSION)
  @Nonnull
  public BgpPeerConfig getNode2Session() {
    return _session2;
  }

  @JsonProperty(PROP_NODE1_SESSION_ID)
  @Nonnull
  public BgpPeerConfigId getSession1Id() {
    return _session1id;
  }

  @JsonProperty(PROP_NODE2_SESSION_ID)
  @Nonnull
  public BgpPeerConfigId getSession2Id() {
    return _session2id;
  }
}
