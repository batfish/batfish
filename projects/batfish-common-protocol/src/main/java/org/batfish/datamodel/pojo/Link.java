package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.InterfaceType;

public class Link extends BfObject {
  private static final String PROP_DST_ID = "dstId";
  private static final String PROP_SRC_ID = "srcId";
  private static final String PROP_TYPE = "type";

  public enum LinkType {
    PHYSICAL,
    UNKNOWN,
    VIRTUAL,
  }

  private final String _dstId;

  private final String _srcId;

  LinkType _type;

  public Link(String srcId, String dstId) {
    this(srcId, dstId, LinkType.UNKNOWN);
  }

  public Link(String srcId, String dstId, LinkType type) {
    this(getId(srcId, dstId), srcId, dstId, type);
  }

  @JsonCreator
  public Link(
      @JsonProperty(PROP_ID) String id,
      @JsonProperty(PROP_SRC_ID) String srcId,
      @JsonProperty(PROP_DST_ID) String dstId,
      @JsonProperty(PROP_TYPE) LinkType type) {
    super(firstNonNull(id, getId(srcId, dstId)));
    _srcId = srcId;
    _dstId = dstId;
    _type = firstNonNull(type, LinkType.UNKNOWN);
    if (srcId == null) {
      throw new IllegalArgumentException("Cannot build Link: srcId is null");
    }
    if (dstId == null) {
      throw new IllegalArgumentException("Cannot build Link: dstId is null");
    }
  }

  @JsonProperty(PROP_DST_ID)
  public String getDstId() {
    return _dstId;
  }

  public static String getId(String srcId, String dstId) {
    return "link-" + srcId + "-" + dstId;
  }

  @JsonProperty(PROP_SRC_ID)
  public String getSrcId() {
    return _srcId;
  }

  @JsonProperty(PROP_TYPE)
  public LinkType getType() {
    return _type;
  }

  /** Determines and returns {@link LinkType} from the {@link InterfaceType} of the two ends */
  public static LinkType interfaceTypesToLinkType(
      InterfaceType iface1type, InterfaceType iface2type) {

    if (iface1type != iface2type) {
      return LinkType.UNKNOWN;
    }

    return switch (iface1type) {
      case PHYSICAL, LOGICAL -> LinkType.PHYSICAL;
      case AGGREGATED, AGGREGATE_CHILD, REDUNDANT, REDUNDANT_CHILD, TUNNEL, VLAN, VPN ->
          LinkType.VIRTUAL;

      // loopback and null shouldn't really happen; lets call it unknown
      case LOOPBACK, NULL, UNKNOWN -> LinkType.UNKNOWN;
    };
  }

  @JsonProperty(PROP_TYPE)
  public void setType(LinkType type) {
    _type = type;
  }
}
