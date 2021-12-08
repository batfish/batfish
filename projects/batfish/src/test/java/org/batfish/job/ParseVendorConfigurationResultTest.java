package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
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
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection.SilentSyntaxElem;
import org.batfish.job.ParseVendorConfigurationJob.FileResult;
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

    SilentSyntaxCollection silentSyntax = new SilentSyntaxCollection();
    silentSyntax.addElement(new SilentSyntaxElem("rule1", 1, "text"));

    Warnings fileWarnings = new Warnings();
    fileWarnings.getParseWarnings().add(new ParseWarning(1, "text", "context", "comment"));

    ParseVendorConfigurationResult result =
        new ParseVendorConfigurationResult(
            0,
            new BatfishLoggerHistory(),
            ImmutableMap.of(
                filename,
                new FileResult(parseTree, silentSyntax, fileWarnings)
                    .setParseStatus(ParseStatus.PARTIALLY_UNRECOGNIZED)),
            ConfigurationFormat.CISCO_IOS,
            config,
            new Warnings(),
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
    assertThat(answerWarnings, hasEntry(filename, fileWarnings));

    // Confirm that status was applied
    assertThat(
        answerElement.getParseStatus(), hasEntry(filename, ParseStatus.PARTIALLY_UNRECOGNIZED));
  }

  @Test
  public void testApplyTo_multiFile() {
    VendorConfiguration config = new CiscoConfiguration();
    config.setHostname("hostname");

    List<String> filenames = ImmutableList.of("file1", "file2");
    List<ParseTreeSentences> parseTrees =
        ImmutableList.of(new ParseTreeSentences(), new ParseTreeSentences());
    parseTrees.get(0).setSentences(ImmutableList.of("test1"));
    parseTrees.get(1).setSentences(ImmutableList.of("test2"));

    List<SilentSyntaxCollection> silentSyntaxes =
        ImmutableList.of(new SilentSyntaxCollection(), new SilentSyntaxCollection());
    silentSyntaxes.get(0).addElement(new SilentSyntaxElem("rule1", 1, "text"));
    silentSyntaxes.get(1).addElement(new SilentSyntaxElem("rule2", 1, "text"));

    List<Warnings> fileWarnings = ImmutableList.of(new Warnings(), new Warnings());
    fileWarnings.get(0).getParseWarnings().add(new ParseWarning(1, "text", "context", "comment"));
    fileWarnings.get(1).getParseWarnings().add(new ParseWarning(2, "text", "context", "comment"));

    Warnings globalWarnings = new Warnings(true, true, true);
    globalWarnings.redFlag("No good");

    ParseVendorConfigurationResult result =
        new ParseVendorConfigurationResult(
            0,
            new BatfishLoggerHistory(),
            ImmutableMap.of(
                filenames.get(0),
                new FileResult(parseTrees.get(0), silentSyntaxes.get(0), fileWarnings.get(0))
                    .setParseStatus(ParseStatus.PARTIALLY_UNRECOGNIZED),
                filenames.get(1),
                new FileResult(parseTrees.get(1), silentSyntaxes.get(1), fileWarnings.get(1))
                    .setParseStatus(ParseStatus.PASSED)),
            ConfigurationFormat.CISCO_IOS,
            config,
            globalWarnings,
            HashMultimap.create());

    SortedMap<String, VendorConfiguration> configs = new TreeMap<>();
    ParseVendorConfigurationAnswerElement answerElement =
        new ParseVendorConfigurationAnswerElement();

    result.applyTo(configs, new BatfishLogger("debug", false), answerElement);

    SortedMap<String, ParseTreeSentences> answerParseTrees = answerElement.getParseTrees();
    SortedMap<String, Warnings> answerWarnings = answerElement.getWarnings();

    // Confirm result parse trees were properly applied to answerElement
    assertThat(answerParseTrees, hasEntry(filenames.get(0), parseTrees.get(0)));
    assertThat(answerParseTrees, hasEntry(filenames.get(1), parseTrees.get(1)));

    // Confirm result warning was properly applied to answerElement
    assertThat(answerWarnings, hasEntry(filenames.get(0), fileWarnings.get(0)));
    assertThat(answerWarnings, hasEntry(filenames.get(1), fileWarnings.get(1)));

    // Confirm that global warnings were applied
    assertThat(answerWarnings, hasEntry("[file1, file2]", globalWarnings));

    // Confirm that status was applied
    assertThat(
        answerElement.getParseStatus(),
        hasEntry(filenames.get(0), ParseStatus.PARTIALLY_UNRECOGNIZED));
    assertThat(answerElement.getParseStatus(), hasEntry(filenames.get(1), ParseStatus.PASSED));
  }
}
