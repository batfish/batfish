package org.batfish.datamodel;

import static org.batfish.datamodel.Names.Type.NODE_ROLE;
import static org.batfish.datamodel.Names.Type.REFERENCE_OBJECT;
import static org.batfish.datamodel.Names.Type.TABLE_COLUMN;
import static org.batfish.datamodel.Names.VALID_PATTERNS;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

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

  @Test
  public void testNodeRoleNames() {
    for (String name : NODE_ROLE_VALID_NAMES) {
      assertThat(name, matchesPattern(VALID_PATTERNS.get(NODE_ROLE)));
    }

    for (String name : NODE_ROLE_INVALID_NAMES) {
      assertThat(name, not(matchesPattern(VALID_PATTERNS.get(NODE_ROLE))));
    }
  }

  @Test
  public void testReferenceObjectNames() {
    for (String name : REFERENCE_OBJECT_VALID_NAMES) {
      assertThat(name, matchesPattern(VALID_PATTERNS.get(REFERENCE_OBJECT)));
    }

    for (String name : REFERENCE_OBJECT_INVALID_NAMES) {
      assertThat(name, not(matchesPattern(VALID_PATTERNS.get(REFERENCE_OBJECT))));
    }
  }

  @Test
  public void testTableColumnNames() {

    List<String> valid_names =
        ImmutableList.of(
            "simple", "~startTilde", "_startsUnderScore", "has-", "has.", "has:", "has/");

    List<String> invalid_names =
        ImmutableList.of("-startDash", "has space", ".startDot", "@startAt", "");

    for (String name : valid_names) {
      assertThat(name, matchesPattern(VALID_PATTERNS.get(TABLE_COLUMN)));
    }

    for (String name : invalid_names) {
      assertThat(name, not(matchesPattern(VALID_PATTERNS.get(TABLE_COLUMN))));
    }
  }

  @Test
  public void testZoneToZoneFilter() {
    assertThat(zoneToZoneFilter("a", "b"), equalTo("zone~a~to~zone~b"));
  }
}
