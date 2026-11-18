package org.batfish.question.filtertable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.table.Row;
import org.batfish.question.filtertable.Filter.Operand;
import org.batfish.question.filtertable.Filter.Operand.Type;
import org.batfish.question.filtertable.Filter.Operator;
import org.junit.jupiter.api.Test;

class FilterTest {

  @Test
  void constructorOperators() {
    // check that we map the operator correctly
    assertThat(new Filter("col == 42").getOperator(), equalTo(Operator.EQ));
    assertThat(new Filter("col >= 42").getOperator(), equalTo(Operator.GE));
    assertThat(new Filter("col > 42").getOperator(), equalTo(Operator.GT));
    assertThat(new Filter("col IS 42").getOperator(), equalTo(Operator.IS));
    // multiple spaces allowed between IS and NOT
    assertThat(new Filter("col IS  NOT 42").getOperator(), equalTo(Operator.ISNOT));
    assertThat(new Filter("col < 42").getOperator(), equalTo(Operator.LT));
    assertThat(new Filter("col <= 42").getOperator(), equalTo(Operator.LE));
    assertThat(new Filter("col != 42").getOperator(), equalTo(Operator.NE));
  }

  @Test
  void constructorOperands() {
    // check that we parsed the type correctly
    assertThat(
        new Filter("col == true").getRightOperand(), equalTo(new Operand(Type.BOOLEAN, true)));
    assertThat(new Filter("col == 42").getRightOperand(), equalTo(new Operand(Type.INTEGER, 42)));
    assertThat(new Filter("col == null").getRightOperand(), equalTo(new Operand(Type.NULL, null)));
    assertThat(
        new Filter("col == \"forty two\"").getRightOperand(),
        equalTo(new Operand(Type.STRING, "forty two")));
    assertThat(
        new Filter("col == col[innerCol]").getRightOperand(),
        equalTo(new Operand(Type.COLUMN, Arrays.asList("col", "innerCol"))));
    assertThat(
        new Filter("col == col[inner1[inner2]]").getRightOperand(),
        equalTo(new Operand(Type.COLUMN, Arrays.asList("col", "inner1", "inner2"))));

    // nested column operand on the left
    assertThat(
        new Filter("col[inner1[inner2]] == 42").getLeftOperand(),
        equalTo(new Operand(Type.COLUMN, Arrays.asList("col", "inner1", "inner2"))));

    // both constants
    assertThat(
        new Filter("\"forty two\" == 42").getLeftOperand(),
        equalTo(new Operand(Type.STRING, "forty two")));

    // one negative example of malformed column spec
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Filter("42 == a[b]]");
            });
    assertThat(exception.getMessage(), containsString("Invalid table column"));
  }

  @Test
  void extractValueBaseCase() {
    // check extraction of constants in the filter
    assertThat(
        Filter.extractValue(new Operand(Type.NULL, null), Row.builder().build()), equalTo(null));
    assertThat(
        Filter.extractValue(new Operand(Type.INTEGER, 42), Row.builder().build()), equalTo(42));
  }

  @Test
  void extractValueColumnCase() {
    // non-nested cases
    assertThat(
        Filter.extractValue(
            new Operand(Type.COLUMN, Arrays.asList("col")), Row.builder().put("col", 42).build()),
        equalTo(42));

    // 1-level nesting
    JsonNode object = BatfishObjectMapper.mapper().createObjectNode().set("c2", null);
    assertThat(
        Filter.extractValue(
            new Operand(Type.COLUMN, Arrays.asList("c1", "c2")),
            Row.builder().put("c1", object).build()),
        equalTo(null));

    // 2-level nesting
    ((ObjectNode) object)
        .set(
            "c2",
            BatfishObjectMapper.mapper()
                .createObjectNode()
                .set("c3", BatfishObjectMapper.mapper().valueToTree(42)));
    assertThat(
        Filter.extractValue(
            new Operand(Type.COLUMN, Arrays.asList("c1", "c2", "c3")),
            Row.builder().put("c1", object).build()),
        equalTo(42));

    // one negative example of malformed column spec -- c4 does not exist
    Throwable exception =
        assertThrows(
            NoSuchElementException.class,
            () ->
                Filter.extractValue(
                    new Operand(Type.COLUMN, Arrays.asList("c1", "c2", "c4")),
                    Row.builder().put("c1", object).build()));
    assertThat(exception.getMessage(), containsString("Could not extract"));
  }

  @Test
  void matchesDifferentTypes() {
    // throw an exception when objects are different types
    Throwable exception =
        assertThrows(
            ClassCastException.class,
            () -> new Filter("col != true").matches(Row.builder().put("col", 42).build()));
    assertThat(exception.getMessage(), containsString("cannot be cast"));
  }

  @Test
  void matchesNull() {
    // null matches null and does not match non-null
    assertThat(
        new Filter("col IS null").matches(Row.builder().put("col", null).build()), equalTo(true));
    assertThat(
        new Filter("col IS null").matches(Row.builder().put("col", 42).build()), equalTo(false));

    assertThat(
        new Filter("col IS NOT null").matches(Row.builder().put("col", null).build()),
        equalTo(false));
    assertThat(
        new Filter("col IS NOT null").matches(Row.builder().put("col", 42).build()), equalTo(true));

    assertThat(
        new Filter("col IS 42").matches(Row.builder().put("col", null).build()), equalTo(false));
    assertThat(
        new Filter("col IS NOT 42").matches(Row.builder().put("col", null).build()), equalTo(true));
  }

  @Test
  void matchesOperatorArithmeticTest() {
    Filter filter = new Filter("col >= 42");

    // integer compare match should be good and shouldn't match null
    assertThat(filter.matches(Row.builder().put("col", 42).build()), equalTo(true));
    assertThat(filter.matches(Row.builder().put("col", 41).build()), equalTo(false));
    assertThat(filter.matches(Row.builder().put("col", null).build()), equalTo(false));
  }
}
