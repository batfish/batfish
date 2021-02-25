package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.vendor.VendorConfiguration;
import org.junit.Test;

public class ParseVendorConfigurationResultTest {

  @Test
  public void testApplyTo() {
    String filename = "file.cfg";
    VendorConfiguration config = new CiscoConfiguration();
    config.setHostname("hostname");

    ParseTreeSentences parseTree = new ParseTreeSentences();
    parseTree.setSentences(ImmutableList.of("test"));

    Warnings warnings = new Warnings();
    warnings.getParseWarnings().add(new ParseWarning(1, "text", "context", null));

    ParseVendorConfigurationResult result =
        new ParseVendorConfigurationResult(
            0,
            new BatfishLoggerHistory(),
            filename,
            ConfigurationFormat.CISCO_IOS,
            config,
            warnings,
            parseTree,
            ParseStatus.PASSED,
            HashMultimap.create());

    SortedMap<String, VendorConfiguration> configs = new TreeMap<>();
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();

    result.applyTo(configs, new BatfishLogger("debug", false), answerElement);

    SortedMap<String, ParseTreeSentences> answerParseTrees = answerElement.getParseTrees();
    SortedMap<String, Warnings> answerWarnings = answerElement.getWarnings();

    // Confirm result parse tree was properly applied to answerElement
    assertThat(answerParseTrees, hasEntry(filename, parseTree));

    // Confirm result warning was properly applied to answerElement
    assertThat(answerWarnings, hasEntry(filename, warnings));
  }
}
