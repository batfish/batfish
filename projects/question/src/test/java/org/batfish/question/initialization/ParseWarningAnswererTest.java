package org.batfish.question.initialization;

import static org.batfish.question.initialization.ParseWarningAnswerer.COL_COMMENT;
import static org.batfish.question.initialization.ParseWarningAnswerer.COL_FILELINES;
import static org.batfish.question.initialization.ParseWarningAnswerer.COL_FILENAME;
import static org.batfish.question.initialization.ParseWarningAnswerer.COL_LINE;
import static org.batfish.question.initialization.ParseWarningAnswerer.COL_PARSER_CONTEXT;
import static org.batfish.question.initialization.ParseWarningAnswerer.COL_TEXT;
import static org.batfish.question.initialization.ParseWarningAnswerer.createMetadata;
import static org.batfish.question.initialization.ParseWarningAnswerer.getAggregateRow;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.initialization.IssueAggregation.ParseWarningTriplet;
import org.junit.Test;

/** Tests of {@link ParseWarningAnswerer}. */
public class ParseWarningAnswererTest {

  @Test
  public void testAggregateRow() {
    Row row =
        getAggregateRow(
            new ParseWarningTriplet("dup", "[configuration]", null),
            ImmutableMultimap.of("f1", 3, "f1", 4, "f2", 23),
            createMetadata(new ParseWarningQuestion(true)).toColumnMap());

    Row expected =
        Row.of(
            COL_FILELINES,
            ImmutableList.of(
                new FileLines("f1", ImmutableSortedSet.of(3, 4)),
                new FileLines("f2", ImmutableSortedSet.of(23))),
            COL_TEXT,
            "dup",
            COL_PARSER_CONTEXT,
            "[configuration]",
            COL_COMMENT,
            "(not provided)");

    assertThat(row, equalTo(expected));
  }

  @Test
  public void testGetRowWithoutComment() {
    ParseWarning input = new ParseWarning(3, "text", "[configuration]", null);
    Row expected =
        Row.of(
            COL_FILENAME,
            "nohost",
            COL_LINE,
            input.getLine(),
            COL_TEXT,
            input.getText(),
            COL_PARSER_CONTEXT,
            input.getParserContext(),
            COL_COMMENT,
            "(not provided)");

    Row row =
        ParseWarningAnswerer.getRow(
            "nohost", input, createMetadata(new ParseWarningQuestion(false)).toColumnMap());
    assertThat(row, equalTo(expected));
  }

  @Test
  public void testGetRowWithComment() {
    ParseWarning input = new ParseWarning(3, "text", "[configuration]", "comment");
    Row expected =
        Row.of(
            COL_FILENAME,
            "nohost",
            COL_LINE,
            input.getLine(),
            COL_TEXT,
            input.getText(),
            COL_PARSER_CONTEXT,
            input.getParserContext(),
            COL_COMMENT,
            input.getComment());

    Row row =
        ParseWarningAnswerer.getRow(
            "nohost", input, createMetadata(new ParseWarningQuestion(false)).toColumnMap());
    assertThat(row, equalTo(expected));
  }

  @Test
  public void testAnswererFlow() {
    TestBatfish batfish = new TestBatfish();
    ParseWarningAnswerer answerer = new ParseWarningAnswerer(new ParseWarningQuestion(), batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_FILENAME,
                        "f",
                        COL_LINE,
                        3,
                        COL_TEXT,
                        "text",
                        COL_PARSER_CONTEXT,
                        "ctx",
                        COL_COMMENT,
                        "comment"))));
  }

  private static class TestBatfish extends IBatfishTestAdapter {
    @Override
    public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement(
        NetworkSnapshot snapshot) {
      ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
      Warnings warnings = new Warnings();
      warnings.getParseWarnings().add(new ParseWarning(3, "text", "ctx", "comment"));
      pvcae.setWarnings(ImmutableSortedMap.of("nowarnings", new Warnings(), "f", warnings));
      return pvcae;
    }
  }
}
