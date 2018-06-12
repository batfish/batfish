package org.batfish.datamodel.table;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.table.Row.RowBuilder;

@ParametersAreNonnullByDefault
public class TypedRowBuilder extends RowBuilder {

  Map<String, ColumnMetadata> _columns;

  public TypedRowBuilder(Map<String, ColumnMetadata> columns) {
    checkArgument(columns != null, "Columns cannot be null to instantiate TypedRowBuilder");
    _columns = columns;
  }

  @Override
  public TypedRowBuilder put(String column, Object object) {
    checkArgument(
        _columns.containsKey(column), Row.getMissingColumnErrorMessage(column, _columns.keySet()));
    SchemaUtils.isValidObject(object, _columns.get(column).getSchema());
    super.put(column, object);
    return this;
  }
}
