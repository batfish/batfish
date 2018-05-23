package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.NoSuchElementException;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RowTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  Row initRowThree() {
    return Row.builder().put("col1", "value1").put("col2", "value2").put("col3", "value3").build();
  }

  TableMetadata initMetadataThree(
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

  @Test
  public void selectColumns() {
    Row row = Row.builder().put("col1", 20).put("col2", 21).put("col3", 24).build();

    // check expected results after selecting two columns
    Row newRow = Row.selectColumns(row, ImmutableSet.of("col1", "col3"));
    assertThat(newRow, equalTo(Row.builder().put("col1", 20).put("col3", 24).build()));

    // selecting a non-existent column throws an exception
    _thrown.expect(NoSuchElementException.class);
    _thrown.expectMessage("is not present");
    Row.selectColumns(newRow, ImmutableSet.of("col2"));
  }

  @Test
  public void testOfCorrect() {
    assertThat(Row.of(), equalTo(Row.builder().build()));
    assertThat(Row.of("a", 5), equalTo(Row.builder().put("a", 5).build()));
    assertThat(Row.of("a", 5, "b", 7), equalTo(Row.builder().put("a", 5).put("b", 7).build()));
  }

  @Test
  public void testOfOddElements() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("expecting an even number of parameters, not 1");
    Row.of("a");
  }

  @Test
  public void testOfArgumentsWrong() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("argument 2 must be a string, but is: 7");
    Row.of("a", 5, 7, "b");
  }
}
