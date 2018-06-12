package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.answers.Schema;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypedRowBuilderTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void putBadColumn() {
    TypedRowBuilder builder =
        new TypedRowBuilder(
            ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

    // we should not be able to put in a non-existent column
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("not present");
    builder.put("badcol", 2);
  }

  @Test
  public void putBaseType() {
    TypedRowBuilder builder =
        new TypedRowBuilder(
            ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

    // we should be able to put an Integer but not a string
    Row row = builder.put("col", 2).build();
    assertThat(row.get("col", Schema.INTEGER), equalTo(2));

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Cannot convert");
    builder.put("col", "string");
  }

  @Test
  public void putListType() {
    TypedRowBuilder builder =
        new TypedRowBuilder(
            ImmutableMap.of("col", new ColumnMetadata("col", Schema.list(Schema.INTEGER), "desc")));

    // we should be able to put a list but not a base type
    Row row = builder.put("col", ImmutableList.of(2)).build();
    assertThat(row.get("col", Schema.list(Schema.INTEGER)), equalTo(ImmutableList.of(2)));

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Cannot convert");
    builder.put("col", 2);
  }

  @Test
  public void putNullValue() {
    TypedRowBuilder builder =
        new TypedRowBuilder(
            ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

    // we should be able to put null
    Row row = builder.put("col", null).build();
    assertThat(row.get("col", Schema.INTEGER), Matchers.is(nullValue()));
  }

  @Test
  public void rowOfCorrect() {
    assertThat(TypedRowBuilder.rowOf(ImmutableMap.of()), equalTo(Row.of()));
    assertThat(
        TypedRowBuilder.rowOf(
            ImmutableMap.of("a", new ColumnMetadata("a", Schema.INTEGER, "desc")), "a", 5),
        equalTo(Row.of("a", 5)));
    assertThat(
        TypedRowBuilder.rowOf(
            ImmutableMap.of(
                "a",
                new ColumnMetadata("a", Schema.INTEGER, "desc"),
                "b",
                new ColumnMetadata("b", Schema.INTEGER, "desc")),
            "a",
            5,
            "b",
            7),
        equalTo(Row.of("a", 5, "b", 7)));
  }

  @Test
  public void rowOfOddObjects() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("expecting an even number of parameters, not 1");
    TypedRowBuilder.rowOf(ImmutableMap.of(), "a");
  }

  @Test
  public void rowOfArgumentsWrong() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("argument 0 must be a string, but is: 5");
    TypedRowBuilder.rowOf(
        ImmutableMap.of("a", new ColumnMetadata("a", Schema.INTEGER, "desc")), 5, 10);
  }
}
