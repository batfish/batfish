package org.batfish.question.filtertable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.table.Row;

/**
 * A class that captures the filtering condition. Limitations: 1) We support only the base condition
 * at the moment -- this class can be a disjunction of conjunctions of base filters; 2) The values
 * to compare must be atomic values -- other Schemas can be added later.
 */
@ParametersAreNonnullByDefault
public class Filter {

  public enum Operator {
    EQ("=="),
    GE(">="),
    GT(">"),
    IS("IS"),
    ISNOT("ISNOT"),
    LE("<="),
    LT("<"),
    NE("!=");

    private static final Map<String, Operator> MAP = initMap();

    @JsonCreator
    public static Operator fromString(String shorthand) {
      Operator value = MAP.get(shorthand);
      if (value == null) {
        throw new IllegalArgumentException(
            "No " + Operator.class.getSimpleName() + " with shorthand: '" + shorthand + "'");
      }
      return value;
    }

    private static Map<String, Operator> initMap() {
      ImmutableMap.Builder<String, Operator> map = ImmutableMap.builder();
      for (Operator value : Operator.values()) {
        String shorthand = value._shorthand;
        map.put(shorthand, value);
      }
      return map.build();
    }

    private final String _shorthand;

    Operator(String shorthand) {
      _shorthand = shorthand;
    }

    @Override
    public String toString() {
      return _shorthand;
    }
  }

  public static class Operand {
    public enum Type {
      BOOLEAN,
      COLUMN,
      INTEGER,
      NULL,
      STRING
    }

    private @Nonnull Type _type;
    private @Nullable Object _value;

    public Operand(Type type, @Nullable Object value) {
      _type = type;
      _value = value;
      if (type == Operand.Type.COLUMN) {
        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) _value;
        for (String column : columns) {
          Names.checkName(column, "table column", Names.Type.TABLE_COLUMN);
        }
      }
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Operand)) {
        return false;
      }
      return Objects.equals(_type, ((Operand) o)._type)
          && Objects.equals(_value, ((Operand) o)._value);
    }

    public Type getType() {
      return _type;
    }

    public Object getValue() {
      return _value;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_type, _value);
    }

    @Override
    public String toString() {
      return String.format("%s (%s)", _value, _type);
    }
  }

  // this is more permissive than what we want (e.g, a[b], a[b[c]]]); we'll sanity check later
  private static final String COLUMN_NAME_PATTERN_STR = "[a-zA-Z_][\\w\\[\\]]*";
  private static final String VALUE_PATTERN_STR = "null|true|false|\\d+|\".+\"";
  private static final String OPERAND_PATTERN_STR =
      COLUMN_NAME_PATTERN_STR + "|" + VALUE_PATTERN_STR;
  private static final String OPERATOR_PATTERN_STR = "[<>=!]=?|IS(\\s+NOT)?"; // NB: embedded '('

  private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile(COLUMN_NAME_PATTERN_STR);

  private static final Pattern FILTER_PATTERN =
      Pattern.compile(
          "^\\s*("
              + OPERAND_PATTERN_STR
              + ")\\s+("
              + OPERATOR_PATTERN_STR
              + ")\\s+("
              + OPERAND_PATTERN_STR
              + ")\\s*$");

  private @Nonnull String _expression;
  private @Nonnull Operand _leftOperand;
  private @Nonnull Operator _operator;
  private @Nonnull Operand _rightOperand;

  /**
   * Creates Filter object from its string representation. Examples:
   *
   * <ul>
   *   <li>colName LE 42
   *   <li>colName GT "forty two" -&gt; Strings must be double-quoted
   *   <li>colName NE null -&gt; null literal
   *   <li>colName EQ true
   * </ul>
   *
   * @param filterStr The String representation of the Filter
   */
  @JsonCreator
  public Filter(String filterStr) {
    Matcher matcher = FILTER_PATTERN.matcher(filterStr);
    if (matcher.find()) {
      _expression = filterStr;
      _leftOperand = getOperand(matcher.group(1));
      _operator = Operator.fromString(matcher.group(2).replaceAll("\\s+", ""));
      _rightOperand = getOperand(matcher.group(4));
    } else {
      throw new IllegalArgumentException("Illegal filter string pattern: '" + filterStr + "'");
    }
  }

  static Operand getOperand(String value) {
    if (value.equals("null")) {
      return new Operand(Operand.Type.NULL, null);
    } else if (value.equals("true") || value.equals("false")) {
      return new Operand(Operand.Type.BOOLEAN, Boolean.parseBoolean(value));
    } else if (value.startsWith("\"")) { // String value; use after removing double quotes
      return new Operand(Operand.Type.STRING, value.replaceAll("^\"|\"$", ""));
    } else if (COLUMN_NAME_PATTERN.matcher(value).matches()) {
      String[] parts = value.split("\\[");
      if (parts.length > 1) {
        // check if the string ends with an expected number of ']'s and remove that ending
        StringBuilder expectedEnd = new StringBuilder("]");
        for (int i = 1; i < parts.length - 1; i++) {
          expectedEnd.append("]");
        }
        String lastPart = parts[parts.length - 1];
        if (!lastPart.endsWith(expectedEnd.toString())) {
          throw new IllegalArgumentException("Illegal operand: '" + value + "'");
        }
        parts[parts.length - 1] = lastPart.substring(0, lastPart.length() - expectedEnd.length());
      }
      return new Operand(Operand.Type.COLUMN, Arrays.asList(parts));
    }
    return new Operand(Operand.Type.INTEGER, Integer.parseInt(value));
  }

  private static int compareValues(Object columnValue, Object filterValue) {
    if (columnValue instanceof Integer) {
      return ((Integer) columnValue).compareTo((Integer) filterValue);
    } else if (columnValue instanceof String) {
      return ((String) columnValue).compareTo((String) filterValue);
    } else if (columnValue instanceof Boolean) {
      return ((Boolean) columnValue).compareTo((Boolean) filterValue);
    } else {
      throw new IllegalArgumentException("Unhandle type extracted from column: " + columnValue);
    }
  }

  public Operand getLeftOperand() {
    return _leftOperand;
  }

  @JsonValue
  public String getExpression() {
    return _expression;
  }

  public Operator getOperator() {
    return _operator;
  }

  public Operand getRightOperand() {
    return _rightOperand;
  }

  /**
   * Checks if the given row matches the filter
   *
   * @param row The row to check
   * @return The result of checking
   */
  public boolean matches(Row row) {
    Object leftValue = extractValue(_leftOperand, row);
    Object rightValue = extractValue(_rightOperand, row);

    if (leftValue == null && rightValue == null) {
      return (_operator == Operator.IS);
    } else if (leftValue == null || rightValue == null) {
      return (_operator == Operator.ISNOT);
    }

    // at this point both values are non-null. IS is same as EQ, and ISNOT as NE
    return switch (_operator) {
      case EQ, IS -> compareValues(leftValue, rightValue) == 0;
      case GE -> compareValues(leftValue, rightValue) >= 0;
      case GT -> compareValues(leftValue, rightValue) > 0;
      case LE -> compareValues(leftValue, rightValue) <= 0;
      case LT -> compareValues(leftValue, rightValue) < 0;
      case NE, ISNOT -> compareValues(leftValue, rightValue) != 0;
    };
  }

  /** Extract the actual object value from the operand and row */
  @VisibleForTesting
  static Object extractValue(Operand operand, Row row) {
    if (operand.getType() == Operand.Type.COLUMN) {
      @SuppressWarnings("unchecked")
      List<String> columns = (List<String>) operand.getValue();
      JsonNode object = BatfishObjectMapper.mapper().valueToTree(row.get(columns.get(0)));
      try {
        return extractValue(object, columns, 1);
      } catch (BatfishException | JsonProcessingException e) {
        throw new NoSuchElementException(
            String.format(
                "Could not extract value from '\"%s\":%s' using column specification '%s': %s",
                columns.get(0), object, columns, e.getMessage()));
      }
    }
    return operand.getValue();
  }

  private static Object extractValue(JsonNode jsonNode, List<String> columns, int index)
      throws JsonProcessingException {
    if (jsonNode.isValueNode()) {
      if (index == columns.size()) { // we should arrive at the ValueNode in the end
        return BatfishObjectMapper.mapper().treeToValue(jsonNode, Object.class);
      } else {
        throw new BatfishException("Column specification is deeper than column value");
      }
    } else {
      if (index < columns.size()) {
        ObjectNode objectNode = (ObjectNode) jsonNode;
        if (objectNode.has(columns.get(index))) {
          return extractValue(objectNode.get(columns.get(index)), columns, index + 1);
        } else {
          throw new BatfishException("Missing sub column: " + columns.get(index));
        }
      } else {
        throw new BatfishException("Column specification is shallower than column value");
      }
    }
  }
}
