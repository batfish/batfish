package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ConvertConfigurationAnswerElement}
 */
public class ConvertConfigurationAnswerElementTest {

   ConvertConfigurationAnswerElement element;

   @Before
   public void setUp() {
      element = new ConvertConfigurationAnswerElement();
   }

   @Test
   public void checkEmptyErrors() {
      assertThat(element.getErrors().size(), is(0));
   }

   @Test
   public void checkNonEmptyErrors() {
      BatfishException exception = new BatfishException("sample exception");
      element.getErrors().put("error", new BatfishStackTrace(exception));
      assertThat(element.getErrors().size(), is(1));
   }

   @Test
   public void testGetErrors() {
      BatfishException exception = new BatfishException("sample exception");
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      element.getErrors().put("error", stackTrace);
      assertThat(element.getErrors().get("error"), is(stackTrace));
   }

   @Test
   public void testSetErrors() {
      BatfishException exception = new BatfishException("sample exception");
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      SortedMap<String, BatfishStackTrace> errors = new TreeMap<>();
      errors.put("error", stackTrace);
      element.setErrors(errors);
      assertThat(element.getErrors().get("error"), is(stackTrace));
   }

   @Test
   public void checkDefaultTimestamp() {
      assertThat(element.getFinishTimestamp(), is(nullValue()));
   }

   @Test
   public void testSetTimestamp() {
      Date date = new Date();
      element.setFinishTimestamp(date);
      assertThat(element.getFinishTimestamp(), is(notNullValue()));
   }

   @Test
   public void testGetTimestamp() {
      Date date = new Date();
      element.setFinishTimestamp(date);
      assertThat(element.getFinishTimestamp(), is(date));
   }

   @Test
   public void testPrettyPrint() {
      BatfishException exception = new BatfishException("sample exception");
      BatfishStackTrace stackTrace = new BatfishStackTrace(exception);
      element.getErrors().put("sampleError", stackTrace);
      StringBuilder expected = new StringBuilder();
      expected.append("Results from converting vendor configurations\n");
      expected.append("\n  sampleError[Conversion errors]\n");
      for (String line : element.getErrors().get("sampleError").getLineMap()) {
         expected.append("    " + line + "\n");
      }
      assertThat(element.prettyPrint(), equalTo(expected.toString()));
   }

}