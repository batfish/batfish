package org.batfish.datamodel;

import static org.batfish.datamodel.Names.Type.REFERENCE_OBJECT;
import static org.batfish.datamodel.Names.Type.TABLE_COLUMN;
import static org.batfish.datamodel.Names.VALID_PATTERNS;
import static org.batfish.datamodel.Names.nameNeedsEscaping;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

/** Tests of {@link Names}. */
public class NamesTest {

  public static List<String> REFERENCE_OBJECT_VALID_NAMES =
      ImmutableList.of(
          "a", "_startsUnderscore", "has_", "has-", "has1", "1", "has/", "has.", "has:", "has(");

  // empty strings and non-ascii characters are not allowed
  public static List<String> REFERENCE_OBJECT_INVALID_NAMES =
      ImmutableList.of("", "has" + (char) 128);

  @Test
  public void testNameNeedsEscaping() {
    assertFalse("null", nameNeedsEscaping(null));
    assertFalse("empty", nameNeedsEscaping(""));
    assertFalse("normal", nameNeedsEscaping("abc"));

    assertTrue("digit start", nameNeedsEscaping("1abc"));
    assertTrue("quote start", nameNeedsEscaping("\"abc"));
    assertTrue("slash start", nameNeedsEscaping("/abc"));
    assertTrue("special char", nameNeedsEscaping("a bc"));
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
    for (String name :
        new String[] {
          "simple", "~startTilde", "_startsUnderScore", "has-", "has.", "has:", "has/"
        }) {
      assertThat(name, matchesPattern(VALID_PATTERNS.get(TABLE_COLUMN)));
    }

    for (String name : new String[] {"-startDash", "has space", ".startDot", "@startAt", ""}) {
      assertThat(name, not(matchesPattern(VALID_PATTERNS.get(TABLE_COLUMN))));
    }
  }

  @Test
  public void testZoneToZoneFilter() {
    assertThat(zoneToZoneFilter("a", "b"), equalTo("zone~a~to~zone~b"));
  }
}
