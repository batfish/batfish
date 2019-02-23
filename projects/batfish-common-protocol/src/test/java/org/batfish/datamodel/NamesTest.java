package org.batfish.datamodel;

import static org.batfish.datamodel.Names.Type.NODE_ROLE;
import static org.batfish.datamodel.Names.Type.REFERENCE_OBJECT;
import static org.batfish.datamodel.Names.Type.TABLE_COLUMN;
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

  public static List<String> NODE_ROLE_VALID_NAMES =
      ImmutableList.of("a", "_startsUnderscore", "has_", "has-", "has1", "1startsDigit");

  public static List<String> NODE_ROLE_INVALID_NAMES =
      ImmutableList.of("has/", "has.", "has:", "has\\", "");

  public static List<String> REFERENCE_OBJECT_VALID_NAMES =
      ImmutableList.of("a", "_startsUnderscore", "has_", "has-", "has1");

  public static List<String> REFERENCE_OBJECT_INVALID_NAMES =
      ImmutableList.of("1", "has/", "has.", "has:", "");

  public static List<String> TABLE_COLUMN_VALID_NAMES =
      ImmutableList.of(
          "simple", "~startTilde", "_startsUnderScore", "has-", "has.", "has:", "has/");

  public static List<String> TABLE_COLUMN_INVALID_NAMES =
      ImmutableList.of("-startDash", "has space", ".startDot", "@startAt", "");

  @Test
  public void testNodeRoleNames() {
    for (String name : NODE_ROLE_VALID_NAMES) {
      assertTrue(name, VALID_PATTERNS.get(NODE_ROLE).matcher(name).matches());
    }

    for (String name : NODE_ROLE_INVALID_NAMES) {
      assertFalse(name, VALID_PATTERNS.get(NODE_ROLE).matcher(name).matches());
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
