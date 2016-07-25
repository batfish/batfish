package org.batfish.datamodel.questions;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class Question {

   private static final String DIFF_VAR = "differential";
   private static final String INTERFACE_BLACKLIST_VAR = "interfaceBlacklist";
   private static final String NODE_BLACKLIST_VAR = "nodeBlacklist";

   private boolean _differential;
   private final Set<NodeInterfacePair> _interfaceBlacklist;
   private final NodeSet _nodeBlacklist;

   private final QuestionType _type;

   public Question(QuestionType type) {
      _type = type;
      _nodeBlacklist = new NodeSet();
      _interfaceBlacklist = new TreeSet<NodeInterfacePair>();
      _differential = false;
   }

   @JsonIgnore
   public abstract boolean getDataPlane();

   @JsonIgnore
   public boolean getDiffActive() {
      return !getDifferential()
            && (!_nodeBlacklist.isEmpty() || !_interfaceBlacklist.isEmpty());
   }

   @JsonProperty(DIFF_VAR)
   public boolean getDifferential() {
      return _differential;
   }

   @JsonProperty(INTERFACE_BLACKLIST_VAR)
   public Set<NodeInterfacePair> getInterfaceBlacklist() {
      return _interfaceBlacklist;
   }

   @JsonProperty(NODE_BLACKLIST_VAR)
   public NodeSet getNodeBlacklist() {
      return _nodeBlacklist;
   }

   @JsonIgnore
   public abstract boolean getTraffic();

   @JsonIgnore
   public QuestionType getType() {
      return _type;
   }

   protected boolean isBaseParamKey(String paramKey) {
      switch (paramKey) {
      case DIFF_VAR:
      case INTERFACE_BLACKLIST_VAR:
      case NODE_BLACKLIST_VAR:
         return true;
      default:
         return false;
      }
   }

   // by default, pretty printing is Json
   // override this function in derived classes to do something more meaningful
   public String prettyPrint() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
   
   protected String prettyPrintBase() {
      String retString = "";
      //for brevity, print only if the values are non-default
      if (_differential) {
         retString += String.format("differential=%s", _differential);
      }
      if (_interfaceBlacklist.size() != 0) {
         retString += String.format(" | interfaceBlacklist: %s", _interfaceBlacklist);
      }
      if (_nodeBlacklist.size() != 0) {
         retString += String.format(" | interfaceBlacklist: %s", _interfaceBlacklist);
      }
      if (retString == "")
         return "";
      else 
         return retString + " | ";
   }

   public void setDifferential(boolean differential) {
      _differential = differential;
   }

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
            // TODO: interface and node blacklists
            default:
               throw new BatfishException("Unhandled base param key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
         catch (JSONException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }

   }

   public String toJsonString() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
