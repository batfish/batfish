package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.junit.Test;

public class RowTest {

  static Row initRowThree() {
    return new Row().put("col1", "value1").put("col2", "value2").put("col3", "value3");
  }

  static TableMetadata initMetadataThree(
      boolean keyCol1,
      boolean keyCol2,
      boolean keyCol3,
      boolean valueCol1,
      boolean valueCol2,
      boolean valueCol3) {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("col1", Schema.STRING, "desc", keyCol1, valueCol1),
            new ColumnMetadata("col2", Schema.STRING, "desc", keyCol2, valueCol2),
            new ColumnMetadata("col3", Schema.STRING, "desc", keyCol3, valueCol3));
    return new TableMetadata(columns, null);
  }

  @Test
  public void get() {
    // check that non-list values are same after put and get
    assertThat(new Row().put("col", 42).get("col", Schema.INTEGER), equalTo(42));
    assertThat(
        new Row().put("col", new Node("node")).get("col", Schema.NODE), equalTo(new Node("node")));

    // check the same for lists
    assertThat(
        new Row().put("col", ImmutableList.of(4, 2)).get("col", Schema.list(Schema.INTEGER)),
        equalTo(ImmutableList.of(4, 2)));
    assertThat(
        new Row()
            .put("col", ImmutableList.of(new Node("n1"), new Node("n2")))
            .get("col", Schema.list(Schema.NODE)),
        equalTo(ImmutableList.of(new Node("n1"), new Node("n2"))));
  }

  @Test
  public void getKey() {
    Row row = initRowThree();
    TableMetadata metadataNoKeys = initMetadataThree(false, false, false, false, false, false);
    TableMetadata metadataOneKey = initMetadataThree(false, true, false, false, false, false);
    TableMetadata metadataTwoKeys = initMetadataThree(true, false, true, false, false, false);

    assertThat(row.getKey(metadataNoKeys), equalTo(ImmutableList.of()));
    assertThat(row.getKey(metadataOneKey), equalTo(ImmutableList.of("value2")));
    assertThat(row.getKey(metadataTwoKeys), equalTo(ImmutableList.of("value1", "value3")));
  }

  @Test
  public void getValue() {
    Row row = initRowThree();
    TableMetadata metadataNoValues = initMetadataThree(false, false, false, false, false, false);
    TableMetadata metadataOneValue = initMetadataThree(false, false, false, false, true, false);
    TableMetadata metadataTwoValues = initMetadataThree(true, false, true, true, false, true);

    assertThat(row.getValue(metadataNoValues), equalTo(ImmutableList.of()));
    assertThat(row.getValue(metadataOneValue), equalTo(ImmutableList.of("value2")));
    assertThat(row.getValue(metadataTwoValues), equalTo(ImmutableList.of("value1", "value3")));
  }
}
