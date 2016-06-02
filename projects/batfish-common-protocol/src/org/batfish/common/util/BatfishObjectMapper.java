package org.batfish.common.util;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BatfishObjectMapper extends ObjectMapper {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public BatfishObjectMapper() {
      enable(SerializationFeature.INDENT_OUTPUT);
      enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
   }

}
