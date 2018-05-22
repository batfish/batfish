package org.batfish.question.ipowners;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

class IpOwnersAnswerElement extends TableAnswerElement {

  static final String COL_NODE = "Hostname";
  static final String COL_VRFNAME = "VRF";
  static final String COL_IP = "IP";
  static final String COL_INTERFACE_NAME = "Interface";
  static final String COL_ACTIVE = "Active";

  @JsonCreator
  public IpOwnersAnswerElement(@JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  IpOwnersAnswerElement() {
    super(getTableMetadata());
  }

  /** Create table metadata for this answer. */
  private static TableMetadata getTableMetadata() {
    List<ColumnMetadata> columnMetadata = getColumnMetadata();
    DisplayHints displayHints = new DisplayHints();
    displayHints.setTextDesc(
        String.format(
            "On node ${%s} in VRF ${%s}, interface ${%s} has IP ${%s}.",
            COL_NODE, COL_VRFNAME, COL_INTERFACE_NAME, COL_IP));
    return new TableMetadata(columnMetadata, displayHints);
  }

  /** Create column metadata. */
  private static List<ColumnMetadata> getColumnMetadata() {
    return ImmutableList.of(
        new ColumnMetadata(COL_NODE, Schema.NODE, "Node hostname"),
        new ColumnMetadata(COL_VRFNAME, Schema.STRING, "VRF name"),
        new ColumnMetadata(COL_INTERFACE_NAME, Schema.STRING, "Interface name"),
        new ColumnMetadata(COL_IP, Schema.IP, "IP address"),
        new ColumnMetadata(
            COL_ACTIVE, Schema.BOOLEAN, "Whether the interface with given IP is active"));
  }
}
