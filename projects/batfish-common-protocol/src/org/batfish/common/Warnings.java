package org.batfish.common;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = Warnings.Serializer.class)
public class Warnings implements Serializable {

   public static class Serializer extends JsonSerializer<Warnings> {

      @Override
      public void serialize(Warnings value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
         jgen.writeStartObject();
         if (!value._pedanticWarnings.isEmpty()) {
            jgen.writeFieldName("Pedantic complaints");
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
            jgen.writeFieldName("Red flags");
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
            jgen.writeFieldName("Unimplemented features");
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

   /**
    *
    */
   private static final long serialVersionUID = 1L;

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
