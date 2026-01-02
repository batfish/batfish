package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RowBuilderTest {

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
