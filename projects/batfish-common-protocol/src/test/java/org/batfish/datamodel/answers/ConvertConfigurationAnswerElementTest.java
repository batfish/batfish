package org.batfish.datamodel.answers;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link ConvertConfigurationAnswerElement} */
public class ConvertConfigurationAnswerElementTest {

  ConvertConfigurationAnswerElement _element;

  @Test
  public void checkEmptyErrors() {
    assertThat(_element.getErrors().size(), is(0));
  }

  @Test
  public void checkNonEmptyErrors() {
    BatfishException exception = new BatfishException("sample exception");
    _element.getErrors().put("error", new BatfishStackTrace(exception));
    assertThat(_element.getErrors().size(), is(1));
  }

  @Before
  public void setUp() {
    _element = new ConvertConfigurationAnswerElement();
  }

  @Test
  public void testConvertStatus() {
    assertThat(_element.getConvertStatusProp(), anEmptyMap());
    _element.getConvertStatus().put("node", ConvertStatus.PASSED);
    assertThat(_element.getConvertStatusProp(), hasEntry("node", ConvertStatus.PASSED));
  }

  @Test
  public void testConvertStatusFromFailed() {
    Set<String> set = new TreeSet<>();
    _element.setFailed(set);
    _element.setConvertStatus(null);

    assertThat(_element.getConvertStatusProp(), anEmptyMap());

    // Confirm object containing failed-set, not convert-status-map still returns a correct map
    set.add("node");
    assertThat(_element.getConvertStatusProp(), hasEntry("node", ConvertStatus.FAILED));
  }

  @Test
  public void testGetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    _element.getErrors().put("error", stackTrace);
    assertThat(_element.getErrors().get("error"), is(stackTrace));
  }

  @Test
  public void testPrettyPrint() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    _element.getErrors().put("sampleError", stackTrace);
    StringBuilder expected = new StringBuilder();
    expected.append("Results from converting vendor configurations\n");
    expected.append("\n  sampleError[Conversion errors]\n");
    for (String line : _element.getErrors().get("sampleError").getLineMap()) {
      expected.append("    " + line + "\n");
    }
    assertThat(_element.prettyPrint(), equalTo(expected.toString()));
  }

  @Test
  public void testSetErrors() {
    BatfishException exception = new BatfishException("sample exception");
    BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
    SortedMap<String, BatfishStackTrace> errors = new TreeMap<>();
    errors.put("error", stackTrace);
    _element.setErrors(errors);
    assertThat(_element.getErrors().get("error"), is(stackTrace));
  }
}
