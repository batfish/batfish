package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.pojo.Node;
import org.junit.jupiter.api.Test;

class SchemaUtilsTest {

  private static JsonNode toJson(Object object) {
    return BatfishObjectMapper.mapper().valueToTree(object);
  }

  @Test
  void convertTypeCorrect() {
    assertThat(SchemaUtils.convertType(toJson(2), Schema.INTEGER), equalTo(2));
    assertThat(
        SchemaUtils.convertType(toJson(ImmutableList.of(1, 2)), Schema.list(Schema.INTEGER)),
        equalTo(ImmutableList.of(1, 2)));
    assertThat(
        SchemaUtils.convertType(
            toJson(ImmutableList.of(ImmutableList.of(1), ImmutableList.of(2))),
            Schema.list(Schema.list(Schema.INTEGER))),
        equalTo(ImmutableList.of(ImmutableList.of(1), ImmutableList.of(2))));
  }

  @Test
  void convertTypeClassCastException() {
    Throwable exception =
        assertThrows(
            ClassCastException.class,
            () -> SchemaUtils.convertType(toJson(ImmutableList.of(1)), Schema.INTEGER));
    assertThat(exception.getMessage(), containsString("Cannot recover"));
  }

  @Test
  void testIsValidObject_Node() {
    assertFalse(SchemaUtils.isValidObject("nodeName", Schema.NODE));
    assertTrue(SchemaUtils.isValidObject(new Node("node"), Schema.NODE));
  }
}
