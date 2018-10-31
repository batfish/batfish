package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.NoSuchElementException;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row.TypedRowBuilder;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RowTest {

  public static class RowBuilderTest {

    @Rule public ExpectedException _thrown = ExpectedException.none();

    @Test
    public void testGetTypeReference() {
      String column = "col";
      List<String> stringList = ImmutableList.of("a", "b");
      Row row = Row.builder().put(column, stringList).build();
      List<String> extracted = row.get(column, new TypeReference<List<String>>() {});

      assertThat(stringList, equalTo(extracted));
    }

    @Test
    public void testGetTypeReferenceInvalid() {
      String column = "col";
      Row row = Row.builder().put(column, "text").build();

      _thrown.expect(ClassCastException.class);
      row.get(column, new TypeReference<Integer>() {});
    }

    @Test
    public void putAllCorrect() {
      Row row = Row.builder().put("col1", 20).put("col2", 21).put("col3", 24).build();

      // all columns should be copied over when we don't limit
      assertThat(
          Row.builder().putAll(row).build(),
          equalTo(Row.builder().put("col1", 20).put("col2", 21).put("col3", 24).build()));

      // only specified columns should be copied
      assertThat(
          Row.builder().putAll(row, ImmutableSet.of("col1", "col3")).build(),
          equalTo(Row.builder().put("col1", 20).put("col3", 24).build()));
    }

    @Test
    public void putAllFail() {
      Row row = Row.builder().put("col1", 20).build();

      // Specifying a non-existent column throws an exception
      _thrown.expect(NoSuchElementException.class);
      _thrown.expectMessage("is not present");
      Row.builder().putAll(row, ImmutableSet.of("col2"));
    }

    @Test
    public void rowOfCorrect() {
      assertThat(Row.builder().rowOf(), equalTo(Row.builder().build()));
      assertThat(Row.builder().rowOf("a", 5), equalTo(Row.builder().put("a", 5).build()));
      assertThat(
          Row.builder().rowOf("a", 5, "b", 7),
          equalTo(Row.builder().put("a", 5).put("b", 7).build()));
    }

    @Test
    public void rowOfOddElements() {
      _thrown.expect(IllegalArgumentException.class);
      _thrown.expectMessage("expecting an even number of parameters, not 1");
      Row.builder().rowOf("a");
    }

    @Test
    public void rowOfArgumentsWrong() {
      _thrown.expect(IllegalArgumentException.class);
      _thrown.expectMessage("argument 2 must be a string, but is: 7");
      Row.builder().rowOf("a", 5, 7, "b");
    }
  }

  public static class TypedRowBuilderTest {

    @Rule public ExpectedException _thrown = ExpectedException.none();

    @Test
    public void putBadColumn() {
      TypedRowBuilder builder =
          Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

      // we should not be able to put in a non-existent column
      _thrown.expect(IllegalArgumentException.class);
      _thrown.expectMessage("not present");
      builder.put("badcol", 2);
    }

    @Test
    public void putBaseType() {
      TypedRowBuilder builder =
          Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

      // we should be able to put an Integer but not a string
      Row row = builder.put("col", 2).build();
      assertThat(row.get("col", Schema.INTEGER), Matchers.equalTo(2));

      _thrown.expect(IllegalArgumentException.class);
      _thrown.expectMessage("Cannot convert");
      builder.put("col", "string");
    }

    @Test
    public void putListType() {
      TypedRowBuilder builder =
          Row.builder(
              ImmutableMap.of(
                  "col", new ColumnMetadata("col", Schema.list(Schema.INTEGER), "desc")));

      // we should be able to put a list but not a base type
      Row row = builder.put("col", ImmutableList.of(2)).build();
      assertThat(
          row.get("col", Schema.list(Schema.INTEGER)), Matchers.equalTo(ImmutableList.of(2)));

      _thrown.expect(IllegalArgumentException.class);
      _thrown.expectMessage("Cannot convert");
      builder.put("col", 2);
    }

    @Test
    public void putNullValue() {
      TypedRowBuilder builder =
          Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")));

      // we should be able to put null
      Row row = builder.put("col", null).build();
      assertThat(row.get("col", Schema.INTEGER), Matchers.is(nullValue()));
    }

    @Test
    public void fillInEmptyColumnsOnBuild() {
      Row row =
          Row.builder(ImmutableMap.of("col", new ColumnMetadata("col", Schema.INTEGER, "desc")))
              .build();
      assertThat(row.getColumnNames(), contains("col"));
      assertThat(row.hasNonNull("col"), equalTo(false));
    }
  }

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
