package org.batfish.datamodel.answers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link InitInfoAnswerElement} */
public class InitInfoAnswerElementTest {
  InitInfoAnswerElement _element;

  @Test
  public void checkEmptyErrors() {
    assertThat(_element.getErrors().size(), is(0));
  }

  @Test
  public void checkNonEmptyErrors() {
    _element.getErrors().put("error", new ArrayList<>());
    assertThat(_element.getErrors().size(), is(1));
  }

  @Test
  public void checkNonEmptyErrorsSet() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    _element.getErrors().put("error", new ArrayList<>());
    _element.getErrors().get("error").add(stackTrace);
    assertThat(_element.getErrors().get("error").size(), is(1));
    assertTrue(_element.getErrors().get("error").contains(stackTrace));
  }

  @Before
  public void setUp() {
    _element = new InitInfoAnswerElement();
  }

  @Test
  public void testGetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    List<BatfishStackTrace> errors = new ArrayList<>();
    errors.add(stackTrace);
    _element.getErrors().put("error", errors);
    assertThat(_element.getErrors().get("error"), is(errors));
  }

  @Test
  public void testPrettyPrint() {
    String errorMessage = "message is: parser: SampleParser: line 50, sample error\n";
    BatfishException exception = new BatfishException(errorMessage);
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    List<BatfishStackTrace> errors = new ArrayList<>();
    errors.add(stackTrace);
    _element.getParseStatus().put("sampleError", ParseStatus.FAILED);
    _element.getErrors().put("sampleError", errors);
    StringBuilder expected = new StringBuilder();
    expected.append("PARSING SUMMARY\n");
    expected.append("  sampleError: failed to parse\n");
    expected.append("DETAILED ERRORS\n");
    for (BatfishStackTrace trace : _element.getErrors().get("sampleError")) {
      expected.append("  Failed to parse sampleError:\n");
      for (String line : trace.getLineMap()) {
        expected.append("    " + line + "\n");
      }
    }
    expected.append("STATISTICS\n");
    expected.append("  Parsing results:\n");
    expected.append("    Failed to parse: 1\n");
    assertThat(_element.prettyPrint(), equalTo(expected.toString()));
  }

  @Test
  public void testSetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    List<BatfishStackTrace> errors = new ArrayList<>();
    errors.add(stackTrace);
    SortedMap<String, List<BatfishStackTrace>> error = new TreeMap<>();
    error.put("error", errors);
    _element.setErrors(error);
    assertThat(_element.getErrors().get("error"), is(errors));
  }
}
