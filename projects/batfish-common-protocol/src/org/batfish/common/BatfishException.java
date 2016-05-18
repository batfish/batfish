package org.batfish.common;

import java.io.IOException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

/**
 * Thrown as a fatal exception. When caught, Batfish should perform any
 * necessary cleanup and terminate gracefully with a non-zero exit status. A
 * BatfishException should always contain a detail message.
 */
@JsonSerialize(using = BatfishException.BatfishExceptionSerializer.class)
public class BatfishException extends RuntimeException implements AnswerElement {

   public static class BatfishExceptionSerializer extends
         JsonSerializer<BatfishException> {

      @Override
      public void serialize(BatfishException value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
         String stackTrace = ExceptionUtils.getFullStackTrace(value);
         String[] lines = stackTrace.replace("\t", "   ").split("\n");
         for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            jgen.writeFieldName(Integer.toString(i));
            jgen.writeString(line);
         }
      }

      @Override
      public void serializeWithType(BatfishException value, JsonGenerator gen,
            SerializerProvider provider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException {

         typeSer.writeTypePrefixForObject(value, gen);
         serialize(value, gen, provider); // call customized serialize method
         typeSer.writeTypeSuffixForObject(value, gen);
      }

   }

   private static final long serialVersionUID = 1L;

   /**
    * Constructs a BatfishException with a detail message
    *
    * @param msg
    *           The detail message
    */
   public BatfishException(String msg) {
      super(msg);
   }

   /**
    * Constructs a BatfishException with a detail message and a cause
    *
    * @param msg
    *           The detail message
    * @param cause
    *           The cause of this exception
    */
   public BatfishException(String msg, Throwable cause) {
      super(msg, cause);
   }

}