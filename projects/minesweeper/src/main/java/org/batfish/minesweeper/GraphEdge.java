package org.batfish.minesweeper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.Interface;

/**
 * An edge in the network graph. Contains the router and interface as well as the peer router and
 * interface. Each edge can also be abstract, as in the case of iBGP, or concrete where it
 * corresponds to a particular edge in the topology. There is an invariant that whenever peer ==
 * null, it must be that end == null
 *
 * @author Ryan Beckett
 */
public class GraphEdge {

  private static final String START_VAR = "start";

  private static final String END_VAR = "end";

  private static final String ROUTER_VAR = "router";

  private static final String PEER_VAR = "peer";

  private static final String ABSTRACT_VAR = "isAbstract";

  private static final String NULL_VAR = "isNullEdge";

  private Interface _start;

  private Interface _end;

  private String _router;

  private String _peer;

  private boolean _isAbstract;

  private boolean _isNullEdge;

  @JsonCreator
  public GraphEdge(
      @JsonProperty(START_VAR) Interface start,
      @Nullable @JsonProperty(END_VAR) Interface end,
      @JsonProperty(ROUTER_VAR) String router,
      @Nullable @JsonProperty(PEER_VAR) String peer,
      @JsonProperty(ABSTRACT_VAR) boolean isAbstract,
      @JsonProperty(NULL_VAR) boolean isNullEdge) {
    _start = start;
    _end = (peer == null ? null : end);
    _router = router;
    _peer = (end == null ? null : peer);
    _isAbstract = isAbstract;
    _isNullEdge = isNullEdge;
  }

  @JsonProperty(START_VAR)
  public Interface getStart() {
    return _start;
  }

  @JsonProperty(END_VAR)
  public Interface getEnd() {
    return _end;
  }

  @JsonProperty(ROUTER_VAR)
  public String getRouter() {
    return _router;
  }

  @JsonProperty(PEER_VAR)
  public String getPeer() {
    return _peer;
  }

  @JsonProperty(ABSTRACT_VAR)
  public boolean isAbstract() {
    return _isAbstract;
  }

  @JsonProperty(NULL_VAR)
  public boolean isNullEdge() {
    return _isNullEdge;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GraphEdge)) {
      return false;
    }
    GraphEdge other = (GraphEdge) o;
    return _isAbstract == other._isAbstract
        && _isNullEdge == other._isNullEdge
        && Objects.equals(_router, other._router)
        && Objects.equals(_start, other._start)
        && Objects.equals(_peer, other._peer)
        && Objects.equals(_end, other._end);
  }

  @Override
  public int hashCode() {
    int result = _start != null ? _start.hashCode() : 0;
    result = 31 * result + (_end != null ? _end.hashCode() : 0);
    result = 31 * result + (_router != null ? _router.hashCode() : 0);
    result = 31 * result + (_peer != null ? _peer.hashCode() : 0);
    result = 31 * result + (_isAbstract ? 1 : 0);
    result = 31 * result + (_isNullEdge ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return _router
        + ","
        + (_start == null ? "_" : _start.getName())
        + " --> "
        + (_peer == null ? "_" : _peer)
        + ","
        + (_end == null ? "_" : _end.getName());
  }
}
