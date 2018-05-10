package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Comparator;
import java.util.Map;
import org.batfish.datamodel.answers.Schema;
import org.junit.Test;

public class RowTest {

  Row initRowThree() {
    return new Row().put("col1", "value1").put("col2", "value2").put("col3", "value3");
  }

  TableMetadata initMetadataThree(
      boolean keyCol1,
      boolean keyCol2,
      boolean keyCol3,
      boolean valueCol1,
      boolean valueCol2,
      boolean valueCol3) {
    Map<String, ColumnMetadata> columns =
        new ImmutableSortedMap.Builder<String, ColumnMetadata>(Comparator.naturalOrder())
            .put("col1", new ColumnMetadata(Schema.STRING, "desc", keyCol1, valueCol1))
            .put("col2", new ColumnMetadata(Schema.STRING, "desc", keyCol2, valueCol2))
            .put("col3", new ColumnMetadata(Schema.STRING, "desc", keyCol3, valueCol3))
            .build();
    return new TableMetadata(columns, null);
  }

  @Test
  public void getKeyTest() {
    Row row = initRowThree();
    TableMetadata metadataNoKeys = initMetadataThree(false, false, false, false, false, false);
    TableMetadata metadataOneKey = initMetadataThree(false, true, false, false, false, false);
    TableMetadata metadataTwoKeys = initMetadataThree(true, false, true, false, false, false);

    assertThat(row.getKey(metadataNoKeys), equalTo(""));
    assertThat(row.getKey(metadataOneKey), equalTo("[\"value2\"]"));
    assertThat(row.getKey(metadataTwoKeys), equalTo("[\"value1\"][\"value3\"]"));
  }

  @Test
  public void getValueTest() {
    Row row = initRowThree();
    TableMetadata metadataNoValues = initMetadataThree(false, false, false, false, false, false);
    TableMetadata metadataOneValue = initMetadataThree(false, false, false, false, true, false);
    TableMetadata metadataTwoValues = initMetadataThree(true, false, true, true, false, true);

    assertThat(row.getValue(metadataNoValues), equalTo(""));
    assertThat(row.getValue(metadataOneValue), equalTo("[\"value2\"]"));
    assertThat(row.getValue(metadataTwoValues), equalTo("[\"value1\"][\"value3\"]"));
  }
}
