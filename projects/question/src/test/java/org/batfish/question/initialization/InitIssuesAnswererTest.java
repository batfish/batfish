package org.batfish.question.initialization;

import static org.batfish.question.initialization.InitIssuesAnswerer.COL_FILELINES;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_ISSUE;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_ISSUE_TYPE;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_LINE_TEXT;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_NODES;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_PARSER_CONTEXT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import javax.annotation.Nullable;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.junit.Test;

/** Tests for {@link InitIssuesAnswerer}. */
public class InitIssuesAnswererTest {

  @Test
  public void testAnswererNoIssues() {
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase(null, null));
    TableAnswerElement answer = answerer.answer();

    // Make sure config with no warnings or errors has no rows
    assertThat(answer.getRows(), equalTo(new Rows()));
  }

  @Test
  public void testAnswererConvertError() {
    String node = "nodeError";
    String message = "Exception message";

    ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
    ccae.setWarnings(ImmutableSortedMap.of(node, new Warnings()));
    ccae.getErrorDetails().putIfAbsent(node, new ErrorDetails(message));
    // Answerer using TestBatfish that should produce a single convert error
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase(null, ccae));
    TableAnswerElement answer = answerer.answer();

    // Make sure we see convert error row in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODES,
                        ImmutableList.of(node),
                        COL_FILELINES,
                        null,
                        COL_ISSUE_TYPE,
                        IssueType.ConvertError.toString(),
                        COL_ISSUE,
                        message,
                        COL_LINE_TEXT,
                        null,
                        COL_PARSER_CONTEXT,
                        null))));
  }

  @Test
  public void testAnswererConvertWarn() {
    String node = "nodeWarn";
    String redFlag = "red flag text";
    String unimplemented = "unimplemented warning text";

    ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
    Warnings warnings = new Warnings(true, true, true);
    warnings.redFlag(redFlag);
    warnings.unimplemented(unimplemented);
    ccae.setWarnings(ImmutableSortedMap.of(node, warnings));
    // Answerer using TestBatfish that should produce two convert warnings (redflag and
    // unimplemented)
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase(null, ccae));
    TableAnswerElement answer = answerer.answer();

    // Make sure we see both the unimplemented and red flag warning rows in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODES,
                        ImmutableList.of(node),
                        COL_FILELINES,
                        null,
                        COL_ISSUE_TYPE,
                        IssueType.ConvertWarningRedFlag.toString(),
                        COL_ISSUE,
                        String.format("%s", redFlag),
                        COL_LINE_TEXT,
                        null,
                        COL_PARSER_CONTEXT,
                        null))
                .add(
                    Row.of(
                        COL_NODES,
                        ImmutableList.of(node),
                        COL_FILELINES,
                        null,
                        COL_ISSUE_TYPE,
                        IssueType.ConvertWarningUnimplemented.toString(),
                        COL_ISSUE,
                        String.format("%s", unimplemented),
                        COL_LINE_TEXT,
                        null,
                        COL_PARSER_CONTEXT,
                        null))));
  }

  @Test
  public void testAnswererParseError() {
    String file = "configs/nodeError.cfg";
    Integer line = 9876;
    String content = "content";
    String context = "context";
    String message = "Exception message";

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    pvcae.setWarnings(ImmutableSortedMap.of(file, new Warnings()));
    pvcae
        .getErrorDetails()
        .put(file, new ErrorDetails(message, new ParseExceptionContext(content, line, context)));
    // Answerer using TestBatfish that should produce a single parse error with parse exception
    // context
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase(pvcae, null));
    TableAnswerElement answer = answerer.answer();

    // Make sure we see parse error row with line number in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODES,
                        null,
                        COL_FILELINES,
                        ImmutableList.of(new FileLines(file, ImmutableSortedSet.of(line))),
                        COL_ISSUE_TYPE,
                        IssueType.ParseError.toString(),
                        COL_ISSUE,
                        message,
                        COL_LINE_TEXT,
                        content,
                        COL_PARSER_CONTEXT,
                        context))));
  }

  @Test
  public void testAnswererParseErrorNoContext() {
    String file = "configs/nodeError.cfg";
    String message = "Exception message";

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    pvcae.setWarnings(ImmutableSortedMap.of(file, new Warnings()));
    pvcae.getErrorDetails().put(file, new ErrorDetails(message));
    // Answerer using TestBatfish that should produce a single parse error without parse exception
    // context
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase(pvcae, null));
    TableAnswerElement answer = answerer.answer();

    // Make sure we see parse error row (without line number) in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODES,
                        null,
                        COL_FILELINES,
                        ImmutableList.of(new FileLines(file, ImmutableSortedSet.of())),
                        COL_ISSUE_TYPE,
                        IssueType.ParseError.toString(),
                        COL_ISSUE,
                        message,
                        COL_LINE_TEXT,
                        null,
                        COL_PARSER_CONTEXT,
                        null))));
  }

  @Test
  public void testAnswererParseWarn() {
    String node = "nodeWarn";
    String text = "line text";
    String context = "parser context";
    String comment = "comment";
    int line = 86420;

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Warnings warnings = new Warnings(true, true, true);
    warnings.getParseWarnings().add(new ParseWarning(line, text, context, comment));
    pvcae.setWarnings(ImmutableSortedMap.of(node, warnings));
    // Answerer using TestBatfish that should produce a single parse warning
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase(pvcae, null));
    TableAnswerElement answer = answerer.answer();

    // Make sure we see parse warning row in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODES,
                        ImmutableList.of(node),
                        COL_FILELINES,
                        ImmutableList.of(new FileLines(node, ImmutableSortedSet.of(line))),
                        COL_ISSUE_TYPE,
                        IssueType.ParseWarning.toString(),
                        COL_ISSUE,
                        comment,
                        COL_LINE_TEXT,
                        text,
                        COL_PARSER_CONTEXT,
                        context))));
  }

  private static class TestBatfishBase extends IBatfishTestAdapter {
    private ParseVendorConfigurationAnswerElement _pvcae;
    private ConvertConfigurationAnswerElement _ccae;

    TestBatfishBase(
        @Nullable ParseVendorConfigurationAnswerElement pvcae,
        @Nullable ConvertConfigurationAnswerElement ccae) {
      if (pvcae == null) {
        _pvcae = new ParseVendorConfigurationAnswerElement();
        _pvcae.setWarnings(ImmutableSortedMap.of("nodeOkay", new Warnings()));
      } else {
        _pvcae = pvcae;
      }
      if (ccae == null) {
        _ccae = new ConvertConfigurationAnswerElement();
        _ccae.setWarnings(ImmutableSortedMap.of("nodeOkay", new Warnings()));
      } else {
        _ccae = ccae;
      }
    }

    @Override
    public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement() {
      return _pvcae;
    }

    @Override
    public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse() {
      return _ccae;
    }
  }
}
