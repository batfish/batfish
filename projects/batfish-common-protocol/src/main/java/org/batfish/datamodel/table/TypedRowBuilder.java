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

  /**
   * Returns a row with provided column names and values (in {@code objects} after checking if the
   * columns and values are what is expected per {@code columns}.
   *
   * <p>{@code objects} should be an even number of parameters, where the 0th and every even
   * parameter is a {@link String} representing the name of a column.
   */
  public static Row rowOf(Map<String, ColumnMetadata> columns, Object... objects) {
    checkArgument(
        objects.length % 2 == 0, "expecting an even number of parameters, not %s", objects.length);
    RowBuilder builder = new TypedRowBuilder(columns);
    for (int i = 0; i + 1 < objects.length; i += 2) {
      checkArgument(
          objects[i] instanceof String, "argument %s must be a string, but is: %s", i, objects[i]);
      builder.put((String) objects[i], objects[i + 1]);
    }
    return builder.build();
  }
}
