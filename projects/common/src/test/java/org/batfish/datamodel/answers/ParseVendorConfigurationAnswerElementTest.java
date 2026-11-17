package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link ParseVendorConfigurationAnswerElement} */
class ParseVendorConfigurationAnswerElementTest {

  ParseVendorConfigurationAnswerElement _element;

  @Test
  void checkEmptyErrors() {
    assertThat(_element.getErrors().size(), is(0));
  }

  @Test
  void checkNonEmptyErrors() {
    BatfishException exception = new BatfishException("sample exception");
    _element.getErrors().put("error", new BatfishStackTrace(exception));
    assertThat(_element.getErrors().size(), is(1));
  }

  @BeforeEach
  void setUp() {
    _element = new ParseVendorConfigurationAnswerElement();
  }

  @Test
  void testGetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    _element.getErrors().put("error", stackTrace);
    assertThat(_element.getErrors().get("error"), is(stackTrace));
  }

  @Test
  void testSetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    SortedMap<String, BatfishStackTrace> errors = new TreeMap<>();
    errors.put("error", stackTrace);
    _element.setErrors(errors);
    assertThat(_element.getErrors().get("error"), is(stackTrace));
  }
}
