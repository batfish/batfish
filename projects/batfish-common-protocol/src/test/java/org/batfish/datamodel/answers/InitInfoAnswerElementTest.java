package org.batfish.datamodel.answers;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
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
    _element.getErrors().put("error", new ArrayList<>());
    _element.getErrors().get("error").add(exception);
    assertThat(_element.getErrors().get("error").size(), is(1));
    assertThat(_element.getErrors().get("error"), contains(exception));
  }

  @Before
  public void setUp() {
    _element = new InitInfoAnswerElement();
  }

  @Test
  public void testGetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    List<BatfishException> errors = new ArrayList<>();
    errors.add(exception);
    _element.getErrors().put("error", errors);
    assertThat(_element.getErrors().get("error"), is(errors));
  }

  @Test
  public void testPrettyPrint() {
    String errorMessage = "message is: parser: SampleParser: line 50, sample error\n";
    BatfishException exception = new BatfishException(errorMessage);
    List<BatfishException> errors = new ArrayList<>();
    errors.add(exception);
    _element.getErrors().put("sampleError", errors);
    StringBuilder expected = new StringBuilder();
    expected.append("PARSING SUMMARY\n");
    expected.append("DETAILED ERRORS\n");
    for (BatfishException e : _element.getErrors().get("sampleError")) {
      expected.append("  Failed to parse sampleError:\n");
      for (String line : e.getBatfishStackTrace().getLineMap()) {
        expected.append("    " + line + "\n");
      }
    }
    expected.append("STATISTICS\n");
    expected.append("  Parsing results:\n");
    assertThat(_element.prettyPrint(), equalTo(expected.toString()));
  }

  @Test
  public void testSetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    List<BatfishException> errors = new ArrayList<>();
    errors.add(exception);
    SortedMap<String, List<BatfishException>> error = new TreeMap<>();
    error.put("error", errors);
    _element.setErrors(error);
    assertThat(_element.getErrors().get("error"), is(errors));
  }
}
