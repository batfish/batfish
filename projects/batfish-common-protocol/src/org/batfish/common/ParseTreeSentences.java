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

@JsonSerialize(using = ParseTreeSentences.Serializer.class)
@JsonDeserialize(using = ParseTreeSentences.Deserializer.class)
public class ParseTreeSentences implements Serializable {

   public static class Deserializer
         extends JsonDeserializer<ParseTreeSentences> {

      @Override
      public ParseTreeSentences deserialize(JsonParser parser,
            DeserializationContext ctx)
            throws IOException, JsonProcessingException {
         JsonNode node = parser.getCodec().readTree(parser);
         ParseTreeSentences tree = new ParseTreeSentences();
         if (node.has(SENTENCES_VAR)) {
            JsonNode warningsNode = node.get(SENTENCES_VAR);
            fillSentenceList(tree._sentences, warningsNode);
         }
         return tree;
      }

      private void fillSentenceList(List<String> sentences, JsonNode node) {
         for (Iterator<Entry<String, JsonNode>> iter = node.fields(); iter
               .hasNext();) {
            Entry<String, JsonNode> e = iter.next();
            String msg = e.getValue().asText();
            int colonIndex = msg.indexOf(":");
            String text = msg.substring(colonIndex + 2, msg.length());
            sentences.add(text);
         }
      }

   }

   public static class Serializer extends JsonSerializer<ParseTreeSentences> {

      @Override
      public void serialize(ParseTreeSentences value, JsonGenerator jgen,
            SerializerProvider provider)
            throws IOException, JsonProcessingException {
         jgen.writeStartObject();
         if (!value._sentences.isEmpty()) {
            jgen.writeFieldName(SENTENCES_VAR);
            jgen.writeStartObject();
            for (int i = 0; i < value._sentences.size(); i++) {
               jgen.writeFieldName(Integer.toString(i + 1));
               jgen.writeString(value._sentences.get(i));
            }
            jgen.writeEndObject();
         }
         jgen.writeEndObject();
      }
   }

   private static final String SENTENCES_VAR = "Parse tree";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final List<String> _sentences;

   public ParseTreeSentences() {
      _sentences = new ArrayList<>();
   }

   public void appendToLastSentence(String appendStr) {
      if (_sentences.size() == 0) {
         _sentences.add(appendStr);
      }
      else {
         String finalStr = _sentences.get(_sentences.size() - 1) + appendStr;
         _sentences.remove(_sentences.size() - 1);
         _sentences.add(finalStr);
      }
   }

   public List<String> getSentences() {
      return _sentences;
   }

   @JsonIgnore
   public boolean isEmpty() {
      return _sentences.isEmpty();
   }
}
