package org.batfish.datamodel.table;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SchemaUtils;
import org.batfish.datamodel.table.Row.RowBuilder;

@ParametersAreNonnullByDefault
public class TypedRowBuilder extends RowBuilder {

  Map<String, ColumnMetadata> _columns;

  public TypedRowBuilder(Map<String, ColumnMetadata> columns) {
    checkArgument(columns != null, "Columns cannot be null to instantiate TypedRowBuilder");
    _columns = columns;
  }

  /**
   * Puts {@code object} into column {@code column} of the row, after checking if the object is
   * compatible with the Schema of the column
   */
  @Override
  public TypedRowBuilder put(String column, @Nullable Object object) {
    checkArgument(
        _columns.containsKey(column), Row.missingColumnErrorMessage(column, _columns.keySet()));
    Schema expectedSchema = _columns.get(column).getSchema();
    checkArgument(
        SchemaUtils.isValidObject(object, expectedSchema),
        String.format(
            "Cannot convert '%s' to Schema '%s' of column '%s", object, expectedSchema, column));
    super.put(column, object);
    return this;
  }
}
