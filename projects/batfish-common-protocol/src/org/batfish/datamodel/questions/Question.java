package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class Question implements IQuestion {

   public static class InstanceData {

      public static class Variable {

         public static enum Type {
            BOOLEAN("boolean", false),
            COMPARATOR("comparator", true),
            DOUBLE("double", false),
            FLOAT("float", false),
            INTEGER("integer", false),
            IP("ip", true),
            IP_PROTOCOL("ipProtocol", true),
            IP_WILDCARD("ipWildcard", true),
            JAVA_REGEX("javaRegex", true),
            JSON_PATH("jsonPath", true),
            JSON_PATH_REGEX("jsonPathRegex", true),
            LONG("long", false),
            PREFIX("prefix", true),
            PREFIX_RANGE("prefixRange", true),
            PROTOCOL("protocol", true),
            STRING("string", true),
            SUBRANGE("subrange", true);

            private static final Map<String, Type> MAP = initMap();

            @JsonCreator
            public static Type fromString(String name) {
               Type value = MAP.get(name.toLowerCase());
               if (value == null) {
                  throw new BatfishException("No " + Type.class.getSimpleName()
                        + " with name: '" + name + "'");
               }
               return value;
            }

            private static synchronized Map<String, Type> initMap() {
               Map<String, Type> map = new HashMap<>();
               for (Type value : Type.values()) {
                  String name = value._name.toLowerCase();
                  map.put(name, value);
               }
               return Collections.unmodifiableMap(map);
            }

            private final String _name;

            private final boolean _stringType;

            private Type(String name, boolean stringType) {
               _name = name;
               _stringType = stringType;
            }

            @JsonValue
            public String getName() {
               return _name;
            }

            public boolean getStringType() {
               return _stringType;
            }

         }

         private static final String ALLOWED_VALUES_VAR = "allowedValues";

         private static final String DESCRIPTION_VAR = "description";

         private static final String MIN_ELEMENTS_VAR = "minElements";

         private static final String MIN_LENGTH_VAR = "minLength";

         private static final String OPTIONAL_VAR = "optional";

         private static final String TYPE_VAR = "type";

         private static final String VALUE_VAR = "value";

         private SortedSet<String> _allowedValues;

         private String _description;

         private Integer _minElements;

         private Integer _minLength;

         private boolean _optional;

         private Type _type;

         private JsonNode _value;

         public Variable() {
            _allowedValues = new TreeSet<>();
         }

         @JsonProperty(ALLOWED_VALUES_VAR)
         public SortedSet<String> getAllowedValues() {
            return _allowedValues;
         }

         @JsonProperty(DESCRIPTION_VAR)
         public String getDescription() {
            return _description;
         }

         @JsonProperty(MIN_ELEMENTS_VAR)
         public Integer getMinElements() {
            return _minElements;
         }

         @JsonProperty(MIN_LENGTH_VAR)
         public Integer getMinLength() {
            return _minLength;
         }

         @JsonProperty(OPTIONAL_VAR)
         public boolean getOptional() {
            return _optional;
         }

         @JsonProperty(TYPE_VAR)
         public Type getType() {
            return _type;
         }

         @JsonProperty(VALUE_VAR)
         public JsonNode getValue() {
            return _value;
         }

         @JsonProperty(ALLOWED_VALUES_VAR)
         public void setAllowedValues(SortedSet<String> allowedValues) {
            _allowedValues = allowedValues;
         }

         @JsonProperty(DESCRIPTION_VAR)
         public void setDescription(String description) {
            _description = description;
         }

         @JsonProperty(MIN_ELEMENTS_VAR)
         public void setMinElements(Integer minElements) {
            _minElements = minElements;
         }

         @JsonProperty(MIN_LENGTH_VAR)
         public void setMinLength(Integer minLength) {
            _minLength = minLength;
         }

         @JsonProperty(OPTIONAL_VAR)
         public void setOptional(boolean optional) {
            _optional = optional;
         }

         @JsonProperty(TYPE_VAR)
         public void setType(Type type) {
            _type = type;
         }

         @JsonProperty(VALUE_VAR)
         public void setValue(JsonNode value) {
            _value = value;
         }

      }

      private static final String DESCRIPTION_VAR = "description";

      private static final String INSTANCE_NAME_VAR = "instanceName";

      private static final String LONG_DESCRIPTION_VAR = "longDescription";

      private static final String TAGS_VAR = "tags";

      private static final String VARIABLES_VAR = "variables";

      private String _description;

      private String _instanceName;

      private String _longDescription;

      private SortedSet<String> _tags;

      private SortedMap<String, Variable> _variables;

      public InstanceData() {
         _tags = new TreeSet<>();
         _variables = new TreeMap<>();
      }

      @JsonProperty(DESCRIPTION_VAR)
      public String getDescription() {
         return _description;
      }

      @JsonProperty(INSTANCE_NAME_VAR)
      public String getInstanceName() {
         return _instanceName;
      }

      @JsonProperty(LONG_DESCRIPTION_VAR)
      public String getLongDescription() {
         return _longDescription;
      }

      @JsonProperty(TAGS_VAR)
      public SortedSet<String> getTags() {
         return _tags;
      }

      @JsonProperty(VARIABLES_VAR)
      public SortedMap<String, Variable> getVariables() {
         return _variables;
      }

      @JsonProperty(DESCRIPTION_VAR)
      public void setDescription(String description) {
         _description = description;
      }

      @JsonProperty(INSTANCE_NAME_VAR)
      public void setInstanceName(String instanceName) {
         _instanceName = instanceName;
      }

      @JsonProperty(LONG_DESCRIPTION_VAR)
      public void setLongDescription(String longDescription) {
         _longDescription = longDescription;
      }

      @JsonProperty(TAGS_VAR)
      public void setTags(SortedSet<String> tags) {
         _tags = tags;
      }

      @JsonProperty(VARIABLES_VAR)
      public void setVariables(SortedMap<String, Variable> variables) {
         _variables = variables;
      }

   }

   private static final String DIFF_VAR = "differential";

   public static final String INNER_QUESTION_VAR = "innerQuestion";

   public static final String INSTANCE_VAR = "instance";

   private boolean _differential;

   private InstanceData _instance;

   public Question() {
      _differential = false;
   }

   @JsonIgnore
   public abstract boolean getDataPlane();

   @JsonProperty(DIFF_VAR)
   public boolean getDifferential() {
      return _differential;
   }

   @JsonProperty(INSTANCE_VAR)
   public InstanceData getInstance() {
      return _instance;
   }

   @JsonIgnore
   public abstract String getName();

   @JsonIgnore
   public abstract boolean getTraffic();

   protected boolean isBaseParamKey(String paramKey) {
      switch (paramKey) {
      case DIFF_VAR:
         return true;
      default:
         return false;
      }
   }

   // by default, pretty printing is Json
   // override this function in derived classes to do something more meaningful
   public String prettyPrint() {
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         return mapper.writeValueAsString(this);
      }
      catch (JsonProcessingException e) {
         throw new BatfishException("Failed to pretty-print question", e);
      }
   }

   protected String prettyPrintBase() {
      String retString = "";
      // for brevity, print only if the values are non-default
      if (_differential) {
         retString += String.format("differential=%s", _differential);
      }
      if (retString == "") {
         return "";
      }
      else {
         return retString + " | ";
      }
   }

   @JsonProperty(DIFF_VAR)
   public void setDifferential(boolean differential) {
      _differential = differential;
   }

   @JsonProperty(INSTANCE_VAR)
   public void setInstance(InstanceData instance) {
      _instance = instance;
   }

   @JsonIgnore
   public void setJsonParameters(JSONObject parameters) {
      Iterator<?> paramKeys = parameters.keys();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         if (!isBaseParamKey(paramKey)) {
            continue;
         }

         try {
            switch (paramKey) {
            case DIFF_VAR:
               setDifferential(parameters.getBoolean(paramKey));
               break;
            case INSTANCE_VAR:
               setInstance(new ObjectMapper().<InstanceData> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<InstanceData>() {
                     }));
               break;
            default:
               throw new BatfishException("Unhandled base param key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
         catch (JSONException | IOException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }

   }

   @Override
   public String toFullJsonString() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      mapper.setSerializationInclusion(Include.ALWAYS);
      return mapper.writeValueAsString(this);
   }

   @Override
   public String toJsonString() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

}
