package org.batfish.question.initialization;

import static org.batfish.question.initialization.InitIssuesAnswerer.COL_COMMENT;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_FILE;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_ISSUE_TYPE;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_NODE;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_PARSER_CONTEXT;
import static org.batfish.question.initialization.InitIssuesAnswerer.COL_STACK_TRACE;
import static org.batfish.question.initialization.InitIssuesAnswerer.trimStackTrace;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.initialization.InitIssuesAnswerer.IssueType;
import org.junit.Test;

/** Tests for {@link InitIssuesAnswerer}. */
public class InitIssuesAnswererTest {
  private static String EXCEPTION_CAUSED_BY = "Caused by:";
  private static String EXCEPTION_ENCLOSING = "Enclosing exception";
  private static String EXCEPTION_INNER = "Inner exception";

  @Test
  public void testTrimStackTrace() {
    BatfishStackTrace stackTraceFull =
        new BatfishStackTrace(
            ImmutableList.of(EXCEPTION_ENCLOSING, EXCEPTION_CAUSED_BY, EXCEPTION_INNER));
    BatfishStackTrace stackTraceMissingCausedBy =
        new BatfishStackTrace(ImmutableList.of(EXCEPTION_ENCLOSING, EXCEPTION_INNER));

    // Make sure only the enclosing line (before caused by) is stripped off the resulting text
    String trimmedFull = trimStackTrace(stackTraceFull);
    assertThat(trimmedFull, containsString(EXCEPTION_CAUSED_BY));
    assertThat(trimmedFull, containsString(EXCEPTION_INNER));
    assertThat(trimmedFull, not(containsString(EXCEPTION_ENCLOSING)));

    // For the trace missing a caused by line, make sure nothing is removed
    String trimmedNoCausedBy = trimStackTrace(stackTraceMissingCausedBy);
    assertThat(trimmedNoCausedBy, containsString(EXCEPTION_ENCLOSING));
    assertThat(trimmedNoCausedBy, containsString(EXCEPTION_INNER));
  }

  @Test
  public void testAnswererNoIssues() {
    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishBase());
    TableAnswerElement answer = answerer.answer();

    // Make sure config with no warnings or errors has no rows
    assertThat(answer.getRows(), equalTo(new Rows()));
  }

  @Test
  public void testAnswererConvertError() {
    BatfishStackTrace stackTrace =
        new BatfishStackTrace(
            ImmutableList.of(EXCEPTION_ENCLOSING, EXCEPTION_CAUSED_BY, EXCEPTION_INNER));
    String node = "nodeError";

    class TestBatfishConvertError extends TestBatfishBase {
      @Override
      public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse() {
        ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
        ccae.setWarnings(ImmutableSortedMap.of(node, new Warnings()));
        ccae.getErrors().putIfAbsent(node, stackTrace);
        return ccae;
      }
    }

    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishConvertError());
    TableAnswerElement answer = answerer.answer();

    // Make sure we see convert error row in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODE,
                        node,
                        COL_FILE,
                        null,
                        COL_ISSUE_TYPE,
                        IssueType.ConvertError.toString(),
                        COL_STACK_TRACE,
                        trimStackTrace(stackTrace),
                        COL_COMMENT,
                        null,
                        COL_PARSER_CONTEXT,
                        null))));
  }

  @Test
  public void testAnswererConvertWarn() {
    String node = "nodeWarn";
    String redFlag = "red flag text";
    String unimplemented = "unimplemented warning text";

    class TestBatfishConvertWarn extends TestBatfishBase {
      @Override
      public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse() {
        ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
        Warnings warnings = new Warnings(true, true, true);
        warnings.redFlag(redFlag);
        warnings.unimplemented(unimplemented);
        ccae.setWarnings(ImmutableSortedMap.of(node, warnings));
        return ccae;
      }
    }

    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishConvertWarn());
    TableAnswerElement answer = answerer.answer();

    // Make sure we see both the unimplemented and red flag warning rows in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODE,
                        node,
                        COL_ISSUE_TYPE,
                        IssueType.ConvertWarningRedFlag.toString(),
                        COL_COMMENT,
                        String.format("%s", redFlag),
                        COL_FILE,
                        null,
                        COL_PARSER_CONTEXT,
                        null,
                        COL_STACK_TRACE,
                        null))
                .add(
                    Row.of(
                        COL_NODE,
                        node,
                        COL_ISSUE_TYPE,
                        IssueType.ConvertWarningUnimplemented.toString(),
                        COL_COMMENT,
                        String.format("%s", unimplemented),
                        COL_FILE,
                        null,
                        COL_PARSER_CONTEXT,
                        null,
                        COL_STACK_TRACE,
                        null))));
  }

  @Test
  public void testAnswererParseError() {
    BatfishStackTrace stackTrace =
        new BatfishStackTrace(
            ImmutableList.of(EXCEPTION_ENCLOSING, EXCEPTION_CAUSED_BY, EXCEPTION_INNER));
    String file = "configs/nodeError.cfg";

    class TestBatfishParseError extends TestBatfishBase {
      @Override
      public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement() {
        ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
        pvcae.setWarnings(ImmutableSortedMap.of(file, new Warnings()));
        pvcae.getErrors().putIfAbsent(file, stackTrace);
        return pvcae;
      }
    }

    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishParseError());
    TableAnswerElement answer = answerer.answer();

    // Make sure we see parse error row in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODE,
                        null,
                        COL_FILE,
                        file,
                        COL_ISSUE_TYPE,
                        IssueType.ParseError.toString(),
                        COL_STACK_TRACE,
                        trimStackTrace(stackTrace),
                        COL_COMMENT,
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

    class TestBatfishConvertWarn extends TestBatfishBase {
      @Override
      public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement() {
        ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
        Warnings warnings = new Warnings(true, true, true);
        warnings.getParseWarnings().add(new ParseWarning(line, text, context, comment));
        pvcae.setWarnings(ImmutableSortedMap.of(node, warnings));
        return pvcae;
      }
    }

    InitIssuesAnswerer answerer =
        new InitIssuesAnswerer(new InitIssuesQuestion(), new TestBatfishConvertWarn());
    TableAnswerElement answer = answerer.answer();

    // Make sure we see parse warning row in answer
    assertThat(
        answer.getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        COL_NODE,
                        node,
                        COL_ISSUE_TYPE,
                        IssueType.ParseWarning.toString(),
                        COL_PARSER_CONTEXT,
                        context,
                        COL_COMMENT,
                        String.format("%s (line %d: %s)", comment, line, text),
                        COL_FILE,
                        null,
                        COL_STACK_TRACE,
                        null))));
  }

  private static class TestBatfishBase extends IBatfishTestAdapter {
    @Override
    public ParseVendorConfigurationAnswerElement loadParseVendorConfigurationAnswerElement() {
      ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
      pvcae.setWarnings(ImmutableSortedMap.of("nodeOkay", new Warnings()));
      return pvcae;
    }

    @Override
    public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElementOrReparse() {
      ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
      ccae.setWarnings(ImmutableSortedMap.of("nodeOkay", new Warnings()));
      return ccae;
    }
  }
}
