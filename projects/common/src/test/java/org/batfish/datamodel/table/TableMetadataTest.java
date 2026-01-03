package org.batfish.datamodel.table;

import static org.batfish.datamodel.table.TableMetadata.toColumnMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.answers.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TableMetadataTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void constructorDuplicateColumnNames() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("col1", Schema.STRING, "desc"),
            new ColumnMetadata("col2", Schema.STRING, "desc"),
            new ColumnMetadata("col1", Schema.STRING, "desc"));

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Cannot have two columns with the same name");

    new TableMetadata(columns, "desc");
  }

  @Test
  public void testToColumnMap() {
    ColumnMetadata metadata1 = new ColumnMetadata("col1", Schema.STRING, "desc1");
    ColumnMetadata metadata2 = new ColumnMetadata("col2", Schema.STRING, "desc2");
    List<ColumnMetadata> columns = ImmutableList.of(metadata1, metadata2);
    assertThat(
        toColumnMap(columns), equalTo(ImmutableMap.of("col1", metadata1, "col2", metadata2)));
  }
}
