package org.batfish.common.util;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BatfishObjectMapper extends ObjectMapper {

   private static class Factory extends JsonFactory {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      protected JsonGenerator _createGenerator(Writer out, IOContext ctxt)
            throws IOException {
         return super._createGenerator(out, ctxt)
               .setPrettyPrinter(new PrettyPrinter());
      }
   }

   private static class PrettyPrinter extends DefaultPrettyPrinter {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public PrettyPrinter() {
         _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
      }
   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public BatfishObjectMapper() {
      this(true);
   }

   public BatfishObjectMapper(boolean indent) {
      super(indent ? new Factory() : new JsonFactory());
      if (indent) {
         enable(SerializationFeature.INDENT_OUTPUT);
      }
      enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      setSerializationInclusion(Include.NON_EMPTY);
   }

   public BatfishObjectMapper(ClassLoader cl) {
      this();
      TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(cl);
      setTypeFactory(tf);
   }

}
