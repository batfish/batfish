package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.answers.ParseEnvironmentRoutingTablesAnswerElement;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.junit.Test;

public class ParseEnvironmentRoutingTableResultTest {

  @Test
  public void testApplyTo() {
    String filename = "file.cfg";

    ParseTreeSentences parseTree = new ParseTreeSentences();
    parseTree.setSentences(ImmutableList.of("test"));

    Warnings warnings = new Warnings();
    warnings.getParseWarnings().add(new ParseWarning(1, "text", "context", null));

    ParseEnvironmentRoutingTableResult result =
        new ParseEnvironmentRoutingTableResult(
            0,
            new BatfishLoggerHistory(),
            Paths.get(filename),
            "name",
            new RoutesByVrf(),
            warnings,
            parseTree);

    SortedMap<String, RoutesByVrf> routingTables = new TreeMap<>();
    ParseEnvironmentRoutingTablesAnswerElement answerElement =
        new ParseEnvironmentRoutingTablesAnswerElement();

    result.applyTo(routingTables, new BatfishLogger("debug", false), answerElement);

    SortedMap<String, ParseTreeSentences> answerParseTrees = answerElement.getParseTrees();
    SortedMap<String, Warnings> answerWarnings = answerElement.getWarnings();

    // Confirm result parse tree was properly applied to answerElement
    assertThat(answerParseTrees, hasEntry(filename, parseTree));

    // Confirm result warning was properly applied to answerElement
    assertThat(answerWarnings, hasEntry(filename, warnings));
  }
}
