package org.batfish.question.initialization;

import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateErrors;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateParseWarnings;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateWarnings;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import java.util.Map;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.question.initialization.IssueAggregation.ErrorDetailsTriplet;
import org.batfish.question.initialization.IssueAggregation.ParseWarningTriplet;
import org.junit.Test;

/** Tests of {@link IssueAggregation}. */
public class IssueAggregationTest {

  @Test
  public void testAggregateDuplicateErrors() {
    ErrorDetails error = new ErrorDetails("message1");
    ErrorDetails errorDup = new ErrorDetails("message1");
    ErrorDetails errorUnique = new ErrorDetails("message2");
    ImmutableMap<String, ErrorDetails> errors =
        ImmutableMap.of("dup1", error, "dup2", errorDup, "unique", errorUnique);

    // Confirm only the duplicate errors are aggregated
    assertThat(
        aggregateDuplicateErrors(errors),
        equalTo(
            ImmutableMap.of(
                new ErrorDetailsTriplet(error),
                ImmutableSortedSet.of("dup1", "dup2"),
                new ErrorDetailsTriplet(errorUnique),
                ImmutableSortedSet.of("unique"))));
  }

  @Test
  public void testAggregateDuplicateErrorsDifferentLines() {
    ErrorDetails error =
        new ErrorDetails("message1", new ParseExceptionContext("line", 1234, "ctx"));
    ErrorDetails errorDupDifferentLine =
        new ErrorDetails("message1", new ParseExceptionContext("line", 5678, "ctx"));
    ErrorDetails errorUnique =
        new ErrorDetails("message1", new ParseExceptionContext("line2", 90, "ctx"));

    ImmutableMap<String, ErrorDetails> errors =
        ImmutableMap.of("dup1", error, "dup2", errorDupDifferentLine, "unique", errorUnique);

    // Confirm the duplicate errors are aggregated despite different parse line numbers and that
    // unique error is not aggregated
    assertThat(
        aggregateDuplicateErrors(errors),
        equalTo(
            ImmutableMap.of(
                new ErrorDetailsTriplet(error),
                ImmutableSortedSet.of("dup1", "dup2"),
                new ErrorDetailsTriplet(errorUnique),
                ImmutableSortedSet.of("unique"))));
  }

  @Test
  public void testAggregateDuplicateParseWarnings() {
    Warnings f1Warnings = new Warnings();
    f1Warnings
        .getParseWarnings()
        .addAll(
            ImmutableList.of(
                new ParseWarning(3, "dup", "[configuration]", null),
                new ParseWarning(4, "dup", "[configuration]", null),
                new ParseWarning(5, "unique", "[configuration]", null)));
    Warnings f2Warnings = new Warnings();
    f2Warnings
        .getParseWarnings()
        .addAll(ImmutableList.of(new ParseWarning(23, "dup", "[configuration]", null)));

    Map<String, Warnings> fileWarnings = ImmutableMap.of("f1", f1Warnings, "f2", f2Warnings);

    // Confirm that only the duplicate parse warnings are aggregated
    Map<ParseWarningTriplet, Multimap<String, Integer>> aggregatedWarnings =
        aggregateDuplicateParseWarnings(fileWarnings);
    ParseWarningTriplet expectedKey1 = new ParseWarningTriplet("dup", "[configuration]", null);
    ParseWarningTriplet expectedKey2 = new ParseWarningTriplet("unique", "[configuration]", null);

    assertThat(aggregatedWarnings.keySet(), contains(expectedKey1, expectedKey2));
    assertThat(aggregatedWarnings.get(expectedKey1).values(), containsInAnyOrder(3, 4, 23));
    assertThat(aggregatedWarnings.get(expectedKey2).values(), contains(5));
  }

  @Test
  public void testAggregateDuplicateWarnings() {
    Warnings f1Warnings = new Warnings();
    f1Warnings
        .getRedFlagWarnings()
        .addAll(
            ImmutableList.of(
                new Warning("dup warning", null), new Warning("unique warning", null)));
    Warnings f2Warnings = new Warnings();
    f2Warnings.getRedFlagWarnings().addAll(ImmutableList.of(new Warning("dup warning", null)));

    Map<String, Warnings> fileWarnings = ImmutableMap.of("f1", f1Warnings, "f2", f2Warnings);

    // Confirm that only the duplicate redflags are aggregated
    assertThat(
        aggregateDuplicateWarnings(fileWarnings, Warnings::getRedFlagWarnings),
        equalTo(
            ImmutableMap.of(
                new Warning("dup warning", null),
                ImmutableSortedSet.of("f1", "f2"),
                new Warning("unique warning", null),
                ImmutableSortedSet.of("f1"))));
  }
}
