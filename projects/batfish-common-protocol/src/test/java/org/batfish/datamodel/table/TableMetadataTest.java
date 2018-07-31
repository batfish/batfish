package org.batfish.datamodel.table;

import com.google.common.collect.ImmutableList;
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
}
