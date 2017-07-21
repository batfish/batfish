package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
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
   public void checkDefaultTimestamp() {
      assertThat(element.getStartTimestamp(), is(nullValue()));
      assertThat(element.getFinishTimestamp(), is(nullValue()));
   }

   @Test
   public void testSetStartTimestamp() {
      Date date = new Date();
      element.setStartTimestamp(date);
      assertThat(element.getStartTimestamp(), is(notNullValue()));
   }

   @Test
   public void testSetFinishTimestamp() {
      Date date = new Date();
      element.setFinishTimestamp(date);
      assertThat(element.getFinishTimestamp(), is(notNullValue()));
   }

   @Test
   public void testGetStartTimestamp() {
      Date date = new Date();
      element.setStartTimestamp(date);
      assertThat(element.getStartTimestamp(), is(date));
   }

   @Test
   public void testGetFinishTimestamp() {
      Date date = new Date();
      element.setFinishTimestamp(date);
      assertThat(element.getFinishTimestamp(), is(date));
   }

   @Test
   public void testPrettyPrint() {
      Date startTimestamp = new Date();
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
      Date finishTimestamp = new Date();
      element.setStartTimestamp(startTimestamp);
      element.setFinishTimestamp(finishTimestamp);
      expected.append("  Start time: " + startTimestamp.toString() + "\n");
      expected.append("  Finish time: " + finishTimestamp.toString() + "\n");
      long timeCost = finishTimestamp.getTime() - startTimestamp.getTime();
      expected.append("  Time cost: " + timeCost + "ms\n");
      assertThat(element.prettyPrint(), equalTo(expected.toString()));
   }

}