package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  public Link(String srcId, String dstId, LinkType type) {
    super(getId(srcId, dstId));
    _srcId = srcId;
    _dstId = dstId;
    _type = type;
  }

  @JsonCreator
  public Link(@JsonProperty(PROP_SRC_ID) String srcId, @JsonProperty(PROP_DST_ID) String dstId) {
    this(srcId, dstId, LinkType.UNKNOWN);
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

  @JsonProperty(PROP_TYPE)
  public void setType(LinkType type) {
    _type = type;
  }
}
