package org.batfish.common;

import java.util.SortedMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thrown as a fatal exception. When caught, Batfish should perform any
 * necessary cleanup and terminate gracefully with a non-zero exit status. A
 * BatfishException should always contain a detail message.
 */
public class BatfishException extends RuntimeException implements AnswerElement {

   public static class BatfishStackTrace implements AnswerElement {

      private static final String LINE_MAP_VAR = "contents";

      private final SortedMap<Integer, String> _lineMap;

      public BatfishStackTrace(BatfishException exception) {
         String stackTrace = ExceptionUtils.getFullStackTrace(exception)
               .replace("\t", "   ");
         _lineMap = CommonUtil.toLineMap(stackTrace);
      }

      @JsonCreator
      public BatfishStackTrace(
            @JsonProperty(LINE_MAP_VAR) SortedMap<Integer, String> lineMap) {
         _lineMap = lineMap;
      }

      @JsonProperty(LINE_MAP_VAR)
      public SortedMap<Integer, String> getLineMap() {
         return _lineMap;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
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

   @JsonValue
   public BatfishStackTrace getBatfishStackTrace() {
      return new BatfishStackTrace(this);
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}