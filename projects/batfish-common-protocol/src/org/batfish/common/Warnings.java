package org.batfish.common;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = Warnings.Serializer.class)
@JsonDeserialize(using = Warnings.Deserializer.class)
public class Warnings implements Serializable {

   public static class Deserializer extends JsonDeserializer<Warnings> {

      @Override
      public Warnings deserialize(JsonParser parser, DeserializationContext ctx)
            throws IOException, JsonProcessingException {
         JsonNode node = parser.getCodec().readTree(parser);
         Warnings warnings = new Warnings();
         if (node.has(PEDANTIC_VAR)) {
            JsonNode warningsNode = node.get(PEDANTIC_VAR);
            fillWarningList(warnings._pedanticWarnings, warningsNode);
         }
         if (node.has(RED_FLAGS_VAR)) {
            JsonNode warningsNode = node.get(RED_FLAGS_VAR);
            fillWarningList(warnings._redFlagWarnings, warningsNode);
         }
         if (node.has(UNIMPLEMENTED_VAR)) {
            JsonNode warningsNode = node.get(UNIMPLEMENTED_VAR);
            fillWarningList(warnings._unimplementedWarnings, warningsNode);
         }
         return warnings;
      }

      private void fillWarningList(List<Warning> warnings, JsonNode node) {
         for (Iterator<Entry<String, JsonNode>> iter = node.fields(); iter
               .hasNext();) {
            Entry<String, JsonNode> e = iter.next();
            String msg = e.getValue().asText();
            int colonIndex = msg.indexOf(":");
            String tag = msg.substring(0, colonIndex);
            String text = msg.substring(colonIndex + 1, msg.length());
            Warning warning = new Warning(text, tag);
            warnings.add(warning);
         }
      }

   }

   public static class Serializer extends JsonSerializer<Warnings> {

      @Override
      public void serialize(Warnings value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
         jgen.writeStartObject();
         if (!value._pedanticWarnings.isEmpty()) {
            jgen.writeFieldName(PEDANTIC_VAR);
            jgen.writeStartObject();
            for (int i = 0; i < value._pedanticWarnings.size(); i++) {
               Warning taggedWarning = value._pedanticWarnings.get(i);
               String text = taggedWarning.getFirst();
               String tag = taggedWarning.getSecond();
               String msg = tag + ": " + text;
               jgen.writeFieldName(Integer.toString(i + 1));
               jgen.writeString(msg);
            }
            jgen.writeEndObject();
         }
         if (!value._redFlagWarnings.isEmpty()) {
            jgen.writeFieldName(RED_FLAGS_VAR);
            jgen.writeStartObject();
            for (int i = 0; i < value._redFlagWarnings.size(); i++) {
               Warning taggedWarning = value._redFlagWarnings.get(i);
               String text = taggedWarning.getFirst();
               String tag = taggedWarning.getSecond();
               String msg = tag + ": " + text;
               jgen.writeFieldName(Integer.toString(i + 1));
               jgen.writeString(msg);
            }
            jgen.writeEndObject();
         }
         if (!value._unimplementedWarnings.isEmpty()) {
            jgen.writeFieldName(UNIMPLEMENTED_VAR);
            jgen.writeStartObject();
            for (int i = 0; i < value._unimplementedWarnings.size(); i++) {
               Warning taggedWarning = value._unimplementedWarnings.get(i);
               String text = taggedWarning.getFirst();
               String tag = taggedWarning.getSecond();
               String msg = tag + ": " + text;
               jgen.writeFieldName(Integer.toString(i + 1));
               jgen.writeString(msg);
            }
            jgen.writeEndObject();
         }
         jgen.writeEndObject();
      }

   }

   private static final String PEDANTIC_VAR = "Pedantic complaints";

   private static final String RED_FLAGS_VAR = "Red flags";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String UNIMPLEMENTED_VAR = "Unimplemented features";

   protected final List<Warning> _pedanticWarnings;

   protected final List<Warning> _redFlagWarnings;

   protected final List<Warning> _unimplementedWarnings;

   public Warnings() {
      _pedanticWarnings = new ArrayList<Warning>();
      _redFlagWarnings = new ArrayList<Warning>();
      _unimplementedWarnings = new ArrayList<Warning>();

   }

   public List<Warning> getPedanticWarnings() {
      return _pedanticWarnings;
   }

   public List<Warning> getRedFlagWarnings() {
      return _redFlagWarnings;
   }

   public List<Warning> getUnimplementedWarnings() {
      return _unimplementedWarnings;
   }

   @JsonIgnore
   public boolean isEmpty() {
      return _pedanticWarnings.isEmpty() && _redFlagWarnings.isEmpty()
            && _unimplementedWarnings.isEmpty();
   }

}
