package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RowTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Row initRowThree() {
    return Row.builder().put("col1", "value1").put("col2", "value2").put("col3", "value3").build();
  }

  private List<ColumnMetadata> initMetadataThree(
      boolean keyCol1,
      boolean keyCol2,
      boolean keyCol3,
      boolean valueCol1,
      boolean valueCol2,
      boolean valueCol3) {
    return ImmutableList.of(
        new ColumnMetadata("col1", Schema.STRING, "desc", keyCol1, valueCol1),
        new ColumnMetadata("col2", Schema.STRING, "desc", keyCol2, valueCol2),
        new ColumnMetadata("col3", Schema.STRING, "desc", keyCol3, valueCol3));
  }

  @Test
  public void get() {
    // check that non-list values are same after put and get
    assertThat(Row.builder().put("col", 42).build().get("col", Schema.INTEGER), equalTo(42));
    assertThat(
        Row.builder().put("col", new Node("node")).build().get("col", Schema.NODE),
        equalTo(new Node("node")));

    // check the same for lists
    assertThat(
        Row.builder()
            .put("col", ImmutableList.of(4, 2))
            .build()
            .get("col", Schema.list(Schema.INTEGER)),
        equalTo(ImmutableList.of(4, 2)));
    assertThat(
        Row.builder()
            .put("col", ImmutableList.of(new Node("n1"), new Node("n2")))
            .build()
            .get("col", Schema.list(Schema.NODE)),
        equalTo(ImmutableList.of(new Node("n1"), new Node("n2"))));
  }

  @Test
  public void getKey() {
    Row row = initRowThree();
    List<ColumnMetadata> metadataNoKeys =
        initMetadataThree(false, false, false, false, false, false);
    List<ColumnMetadata> metadataOneKey =
        initMetadataThree(false, true, false, false, false, false);
    List<ColumnMetadata> metadataTwoKeys =
        initMetadataThree(true, false, true, false, false, false);

    assertThat(row.getKey(metadataNoKeys), equalTo(ImmutableList.of()));
    assertThat(row.getKey(metadataOneKey), equalTo(ImmutableList.of("value2")));
    assertThat(row.getKey(metadataTwoKeys), equalTo(ImmutableList.of("value1", "value3")));
  }

  @Test
  public void getValue() {
    Row row = initRowThree();
    List<ColumnMetadata> metadataNoValues =
        initMetadataThree(false, false, false, false, false, false);
    List<ColumnMetadata> metadataOneValue =
        initMetadataThree(false, false, false, false, true, false);
    List<ColumnMetadata> metadataTwoValues =
        initMetadataThree(true, false, true, true, false, true);

    assertThat(row.getValue(metadataNoValues), equalTo(ImmutableList.of()));
    assertThat(row.getValue(metadataOneValue), equalTo(ImmutableList.of("value2")));
    assertThat(row.getValue(metadataTwoValues), equalTo(ImmutableList.of("value1", "value3")));
  }

  @Test
  public void ofUntyped() {
    assertThat(Row.of("a", 5), equalTo(Row.builder().put("a", 5).build()));
  }

  @Test
  public void ofTyped() {
    assertThat(
        Row.of(ImmutableMap.of("a", new ColumnMetadata("a", Schema.INTEGER, "desc")), "a", 5),
        equalTo(Row.builder().put("a", 5).build()));
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Column 'b' is not present");
    Row.of(ImmutableMap.of("a", new ColumnMetadata("a", Schema.INTEGER, "desc")), "b", 5);
  }
}
