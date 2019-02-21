package org.batfish.datamodel;

import static org.batfish.datamodel.Names.ObjectType.NODE;
import static org.batfish.datamodel.Names.ObjectType.REFERENCE_OBJECT;
import static org.batfish.datamodel.Names.ObjectType.TABLE_COLUMN;
import static org.batfish.datamodel.Names.VALID_PATTERNS;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

/** Tests of {@link Names}. */
public class NamesTest {

  public static List<String> INTERFACE_VALID_NAMES =
      ImmutableList.of("a", "has.", "has-", "has1digit", "has/");

  public static List<String> INTERFACE_INVALID_NAMES =
      ImmutableList.of("1startsWithDigit", ".startsWithDot", "-startsWithDash", "has:", "has_");

  public static List<String> NODE_VALID_NAMES = ImmutableList.of("a", "has.", "has-", "has1digit");

  public static List<String> NODE_INVALID_NAMES =
      ImmutableList.of(
          "1startsWithDigit", ".startsWithDot", "-startsWithDash", "has:", "has/", "has_");

  public static List<String> REFERENCE_OBJECT_VALID_NAMES =
      ImmutableList.of(
          "_a",
          "a_",
          "a-", // dash is allowed
          "a1" // digits are allowed
          );

  public static List<String> REFERENCE_OBJECT_INVALID_NAMES =
      ImmutableList.of(
          "1", // can't begin with a digit
          "a/b", // slash not allowed
          "a.b", // dot not allowed
          "a:b" // colon not allowed
          );

  public static List<String> TABLE_COLUMN_VALID_NAMES =
      ImmutableList.of(
          "simple",
          "~startWithTilde",
          "_startWithUnderScore",
          "contain-",
          "contain.",
          "contain@",
          "contain:",
          "contain/");

  public static List<String> TABLE_COLUMN_INVALID_NAMES =
      ImmutableList.of(
          "-startWithDash",
          "contain space",
          ".startWithDot",
          "@startWithAt",
          "nameWith!@#$%^&*()+Characters",
          "");

  @Test
  public void testInterfaceNames() {
    for (String name : INTERFACE_VALID_NAMES) {
      assertTrue(name, VALID_PATTERNS.get(NODE).matcher(name).matches());
    }

    for (String name : INTERFACE_INVALID_NAMES) {
      assertFalse(name, VALID_PATTERNS.get(NODE).matcher(name).matches());
    }
  }

  @Test
  public void testNodeNames() {
    for (String name : NODE_VALID_NAMES) {
      assertTrue(name, VALID_PATTERNS.get(NODE).matcher(name).matches());
    }

    for (String name : NODE_INVALID_NAMES) {
      assertFalse(name, VALID_PATTERNS.get(NODE).matcher(name).matches());
    }
  }

  @Test
  public void testReferenceObjectNames() {
    for (String name : REFERENCE_OBJECT_VALID_NAMES) {
      assertTrue(name, VALID_PATTERNS.get(REFERENCE_OBJECT).matcher(name).matches());
    }

    for (String name : REFERENCE_OBJECT_INVALID_NAMES) {
      assertFalse(name, VALID_PATTERNS.get(REFERENCE_OBJECT).matcher(name).matches());
    }
  }

  @Test
  public void testTableColumnNames() {

    for (String name : TABLE_COLUMN_VALID_NAMES) {
      assertTrue(name, VALID_PATTERNS.get(TABLE_COLUMN).matcher(name).matches());
    }

    for (String name : TABLE_COLUMN_INVALID_NAMES) {
      assertFalse(name, VALID_PATTERNS.get(TABLE_COLUMN).matcher(name).matches());
    }
  }

  @Test
  public void testZoneToZoneFilter() {
    assertThat(zoneToZoneFilter("a", "b"), equalTo("zone~a~to~zone~b"));
  }
}
