package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Represents a bgp process on a router */
@JsonSchemaDescription("A BGP routing process")
public class BgpProcess implements Serializable {

  private static final String PROP_GENERATED_ROUTES = "generatedRoutes";

  private static final String PROP_MULTIPATH_EBGP = "multipathEbgp";

  private static final String PROP_MULTIPATH_IBGP = "multipathIbgp";

  private static final String PROP_NEIGHBORS = "neighbors";

  private static final String PROP_ROUTER_ID = "routerId";

  private static final String PROP_TIE_BREAKER = "tieBreaker";

  /** */
  private static final long serialVersionUID = 1L;

  /**
   * The set of <i>neighbor-independent</i> generated routes that may be advertised by this process
   * if permitted by their respective generation policies
   */
  private SortedSet<GeneratedRoute> _generatedRoutes;

  private boolean _multipathEbgp;

  private boolean _multipathIbgp;

  /**
   * A map of all the bgp neighbors with which the router owning this process is configured to peer,
   * keyed by prefix
   */
  private SortedMap<Prefix, BgpNeighbor> _neighbors;

  private transient PrefixSpace _originationSpace;

  private Ip _routerId;

  private BgpTieBreaker _tieBreaker;

  /** Constructs a BgpProcess */
  public BgpProcess() {
    _neighbors = new TreeMap<>();
    _generatedRoutes = new TreeSet<>();
    _tieBreaker = BgpTieBreaker.ARRIVAL_ORDER;
  }

  /** @return {@link #_generatedRoutes} */
  @JsonProperty(PROP_GENERATED_ROUTES)
  @JsonPropertyDescription(
      "IPV4 routes generated in the BGP RIB that are not imported into the main RIB for this VRF")
  public SortedSet<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @JsonProperty(PROP_MULTIPATH_EBGP)
  public boolean getMultipathEbgp() {
    return _multipathEbgp;
  }

  @JsonProperty(PROP_MULTIPATH_IBGP)
  public boolean getMultipathIbgp() {
    return _multipathIbgp;
  }

  /** @return {@link #_neighbors} */
  @JsonProperty(PROP_NEIGHBORS)
  @JsonPropertyDescription("Neighbor relationships configured for this BGP process")
  public SortedMap<Prefix, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  @JsonIgnore
  public PrefixSpace getOriginationSpace() {
    return _originationSpace;
  }

  @JsonProperty(PROP_ROUTER_ID)
  @JsonPropertyDescription(
      "The configured router ID for this BGP process. Note that it can be overridden for "
          + "individual neighbors.")
  public Ip getRouterId() {
    return _routerId;
  }

  @JsonProperty(PROP_TIE_BREAKER)
  public BgpTieBreaker getTieBreaker() {
    return _tieBreaker;
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  public void setGeneratedRoutes(SortedSet<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  @JsonProperty(PROP_MULTIPATH_EBGP)
  public void setMultipathEbgp(boolean multipathEbgp) {
    _multipathEbgp = multipathEbgp;
  }

  @JsonProperty(PROP_MULTIPATH_IBGP)
  public void setMultipathIbgp(boolean multipathIbgp) {
    _multipathIbgp = multipathIbgp;
  }

  @JsonProperty(PROP_NEIGHBORS)
  public void setNeighbors(SortedMap<Prefix, BgpNeighbor> neighbors) {
    _neighbors = neighbors;
  }

  public void setOriginationSpace(PrefixSpace originationSpace) {
    _originationSpace = originationSpace;
  }

  @JsonProperty(PROP_ROUTER_ID)
  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  @JsonProperty(PROP_TIE_BREAKER)
  public void setTieBreaker(BgpTieBreaker tieBreaker) {
    _tieBreaker = tieBreaker;
  }
}
