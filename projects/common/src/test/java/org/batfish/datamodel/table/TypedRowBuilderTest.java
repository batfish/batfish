package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.answers.Schema;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class TypedRowBuilderTest {

  @Test
  void putBadColumn() {
    Row.TypedRowBuilder builder =
        Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

    // we should not be able to put in a non-existent column
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> builder.put("badcol", 2));
    assertThat(exception.getMessage(), containsString("not present"));
  }

  @Test
  void putBaseType() {
    Row.TypedRowBuilder builder =
        Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

    // we should be able to put an Integer but not a string
    Row row = builder.put("col", 2).build();
    assertThat(row.get("col", Schema.INTEGER), Matchers.equalTo(2));

    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> builder.put("col", "string"));
    assertThat(exception.getMessage(), containsString("Cannot convert"));
  }

  @Test
  void putListType() {
    Row.TypedRowBuilder builder =
        Row.builder(
            ImmutableMap.of("col", new ColumnMetadata("col", Schema.list(Schema.INTEGER), "desc")));

    // we should be able to put a list but not a base type
    Row row = builder.put("col", ImmutableList.of(2)).build();
    assertThat(row.get("col", Schema.list(Schema.INTEGER)), Matchers.equalTo(ImmutableList.of(2)));

    Throwable exception = assertThrows(IllegalArgumentException.class, () -> builder.put("col", 2));
    assertThat(exception.getMessage(), containsString("Cannot convert"));
  }

  @Test
  void putNullValue() {
    Row.TypedRowBuilder builder =
        Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

    // we should be able to put null
    Row row = builder.put("col", null).build();
    assertThat(row.get("col", Schema.INTEGER), Matchers.is(nullValue()));
  }

  @Test
  void fillInEmptyColumnsOnBuild() {
    Row row =
        Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")))
            .build();
    assertThat(row.getColumnNames(), contains("col"));
    assertThat(row.hasNonNull("col"), equalTo(false));
  }
}
