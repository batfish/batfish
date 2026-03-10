package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
