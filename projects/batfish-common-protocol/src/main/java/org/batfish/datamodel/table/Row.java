package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

public class Row implements Comparable<Row> {

  private final ObjectNode _data;

  @JsonCreator
  public Row(ObjectNode data) {
    _data = MoreObjects.firstNonNull(data, BatfishObjectMapper.mapper().createObjectNode());
  }

  @Override
  public int compareTo(Row o) {
    try {
      String myStr = BatfishObjectMapper.mapper().writeValueAsString(_data);
      String oStr = BatfishObjectMapper.mapper().writeValueAsString(o._data);
      return myStr.compareTo(oStr);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Exception in row comparison", e);
    }
  }

  @JsonValue
  public ObjectNode getData() {
    return _data;
  }
}
