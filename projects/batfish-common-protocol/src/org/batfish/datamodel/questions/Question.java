package org.batfish.datamodel.questions;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class Question {

   private final Set<NodeInterfacePair> _interfaceBlacklist;

   private final NodeSet _nodeBlacklist;

   private final QuestionType _type;

   public Question(QuestionType type) {
      _type = type;
      _nodeBlacklist = new NodeSet();
      _interfaceBlacklist = new TreeSet<NodeInterfacePair>();
   }

   @JsonIgnore
   public abstract boolean getDataPlane();

   @JsonIgnore
   public boolean getDiffActive() {
      return !getDifferential()
            && (!_nodeBlacklist.isEmpty() || !_interfaceBlacklist.isEmpty());
   }

   @JsonIgnore
   public abstract boolean getDifferential();

   public Set<NodeInterfacePair> getInterfaceBlacklist() {
      return _interfaceBlacklist;
   }

   public NodeSet getNodeBlacklist() {
      return _nodeBlacklist;
   }

   @JsonIgnore
   public abstract boolean getTraffic();

   @JsonIgnore
   public QuestionType getType() {
      return _type;
   }

   public void setJsonParameters(JSONObject parameters) {

   }

}
