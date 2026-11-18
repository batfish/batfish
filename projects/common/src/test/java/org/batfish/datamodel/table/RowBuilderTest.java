package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class RowBuilderTest {

  @Test
  void testGetTypeReference() {
    String column = "col";
    List<String> stringList = ImmutableList.of("a", "b");
    Row row = Row.builder().put(column, stringList).build();
    List<String> extracted = row.get(column, new TypeReference<List<String>>() {});

    assertThat(stringList, equalTo(extracted));
  }

  @Test
  void testGetTypeReferenceInvalid() {
    String column = "col";
    Row row = Row.builder().put(column, "text").build();

    assertThrows(ClassCastException.class, () -> row.get(column, new TypeReference<Integer>() {}));
  }

  @Test
  void putAllCorrect() {
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
  void putAllFail() {
    Row row = Row.builder().put("col1", 20).build();

    // Specifying a non-existent column throws an exception
    Throwable exception =
        assertThrows(
            NoSuchElementException.class, () -> Row.builder().putAll(row, ImmutableSet.of("col2")));
    assertThat(exception.getMessage(), containsString("is not present"));
  }

  @Test
  void rowOfCorrect() {
    assertThat(Row.builder().rowOf(), equalTo(Row.builder().build()));
    assertThat(Row.builder().rowOf("a", 5), equalTo(Row.builder().put("a", 5).build()));
    assertThat(
        Row.builder().rowOf("a", 5, "b", 7),
        equalTo(Row.builder().put("a", 5).put("b", 7).build()));
  }

  @Test
  void rowOfOddElements() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Row.builder().rowOf("a"));
    assertThat(
        exception.getMessage(), containsString("expecting an even number of parameters, not 1"));
  }

  @Test
  void rowOfArgumentsWrong() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> Row.builder().rowOf("a", 5, 7, "b"));
    assertThat(exception.getMessage(), containsString("argument 2 must be a string, but is: 7"));
  }
}
