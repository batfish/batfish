package org.batfish.datamodel.answers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link InitInfoAnswerElement}
 */
public class InitInfoAnswerElementTest {
   InitInfoAnswerElement element;

   @Before
   public void setUp() {
      element = new InitInfoAnswerElement();
   }

   @Test
   public void checkEmptyErrors() {
      assertThat(element.getErrors().size(), is(0));
   }

   @Test
   public void checkNonEmptyErrors() {
      element.getErrors().put("error", new HashSet<>());
      assertThat(element.getErrors().size(), is(1));
   }

   @Test
   public void checkNonEmptyErrorsSet() {
      BatfishException exception = new BatfishException("sample exception");
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      element.getErrors().put("error", new HashSet<>());
      element.getErrors().get("error").add(stackTrace);
      assertThat(element.getErrors().get("error").size(), is(1));
      assertTrue(element.getErrors().get("error").contains(stackTrace));
   }

   @Test
   public void testGetErrors() {
      BatfishException exception = new BatfishException("sample exception");
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      HashSet<BatfishStackTrace> errors = new HashSet<>();
      errors.add(stackTrace);
      element.getErrors().put("error", errors);
      assertThat(element.getErrors().get("error"), is(errors));
   }

   @Test
   public void testSetErrors() {
      BatfishException exception = new BatfishException("sample exception");
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      HashSet<BatfishStackTrace> errors = new HashSet<>();
      errors.add(stackTrace);
      SortedMap<String, Set<BatfishStackTrace>> error = new TreeMap<>();
      error.put("error", errors);
      element.setErrors(error);
      assertThat(element.getErrors().get("error"), is(errors));
   }

   @Test
   public void testPrettyPrint() {
      String errorMessage = "message is: parser: SampleParser: line 50, sample error\n";
      BatfishException exception = new BatfishException(errorMessage);
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      Set<BatfishStackTrace> errors = new HashSet<>();
      errors.add(stackTrace);
      element.getErrors().put("sampleError", errors);
      StringBuilder expected = new StringBuilder();
      expected.append("PARSING SUMMARY\n");
      expected.append("DETAILED ERRORS\n");
      for (BatfishStackTrace trace : element.getErrors().get("sampleError")) {
         expected.append("  Failed to parse sampleError:\n");
         for (String line : trace.getLineMap()) {
            expected.append("    " + line + "\n");
         }
      }
      expected.append("STATISTICS\n");
      expected.append("  Parsing results:\n");
      assertThat(element.prettyPrint(), equalTo(expected.toString()));
   }

}