package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import com.google.common.collect.ImmutableList;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.answers.ParseEnvironmentBgpTablesAnswerElement;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.junit.Test;

public class ParseEnvironmentBgpTableResultTest {

  @Test
  public void testApplyTo() {
    String key = "file.cfg";

    ParseTreeSentences parseTree = new ParseTreeSentences();
    parseTree.setSentences(ImmutableList.of("test"));

    Warnings warnings = new Warnings();
    warnings.getParseWarnings().add(new ParseWarning(1, "text", "context", null));

    ParseEnvironmentBgpTableResult result =
        new ParseEnvironmentBgpTableResult(
            0,
            new BatfishLoggerHistory(),
            key,
            "name",
            new BgpAdvertisementsByVrf(),
            warnings,
            parseTree);

    SortedMap<String, BgpAdvertisementsByVrf> bgpTables = new TreeMap<>();
    ParseEnvironmentBgpTablesAnswerElement answerElement =
        new ParseEnvironmentBgpTablesAnswerElement();

    result.applyTo(bgpTables, new BatfishLogger("debug", false), answerElement);

    SortedMap<String, ParseTreeSentences> answerParseTrees = answerElement.getParseTrees();
    SortedMap<String, Warnings> answerWarnings = answerElement.getWarnings();

    // Confirm result parse tree was properly applied to answerElement
    assertThat(answerParseTrees, hasEntry(key, parseTree));

    // Confirm result warning was properly applied to answerElement
    assertThat(answerWarnings, hasEntry(key, warnings));
  }
}
