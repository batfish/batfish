package org.batfish.datamodel.collections;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpEdge;

/** Verbose description of EIGRP adjacency */
public final class VerboseEigrpEdge implements Serializable, Comparable<VerboseEigrpEdge> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";
  private static final String PROP_EDGE = "edge";
  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final EigrpEdge _edge;

  public VerboseEigrpEdge(@Nonnull EigrpEdge edge, @Nonnull IpEdge edgeSummary) {
    _edge = edge;
    _edgeSummary = edgeSummary;
  }

  @JsonCreator
  private static VerboseEigrpEdge create(
      @Nullable @JsonProperty(PROP_EDGE) EigrpEdge edge,
      @Nullable @JsonProperty(PROP_EDGE_SUMMARY) IpEdge edgeSummary) {
    return new VerboseEigrpEdge(requireNonNull(edge), requireNonNull(edgeSummary));
  }

  @JsonProperty(PROP_EDGE)
  public EigrpEdge getEdge() {
    return _edge;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @Override
  public int compareTo(@Nonnull VerboseEigrpEdge o) {
    return Comparator.comparing(VerboseEigrpEdge::getEdgeSummary)
        .thenComparing(VerboseEigrpEdge::getEdge)
        .compare(this, o);
  }
}
