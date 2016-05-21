package org.batfish.datamodel.questions;

import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BgpAdvertisementsQuestion extends Question {

   private static final String EBGP_VAR = "ebgp";

   private static final String IBGP_VAR = "ibgp";

   private static final String NODE_REGEX_VAR = "nodeRegex";

   private static final String RECEIVED_VAR = "received";

   private static final String SENT_VAR = "sent";

   private boolean _ebgp;

   private boolean _ibgp;

   private String _nodeRegex;

   private boolean _received;

   private boolean _sent;

   public BgpAdvertisementsQuestion() {
      super(QuestionType.BGP_ADVERTISEMENTS);
      _ebgp = true;
      _ibgp = true;
      _nodeRegex = ".*";
      _received = true;
      _sent = true;
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @Override
   public boolean getDifferential() {
      return false;
   }

   @JsonProperty(EBGP_VAR)
   public boolean getEbgp() {
      return _ebgp;
   }

   @JsonProperty(IBGP_VAR)
   public boolean getIbgp() {
      return _ibgp;
   }

   @JsonProperty(NODE_REGEX_VAR)
   public String getNodeRegex() {
      return _nodeRegex;
   }

   @JsonProperty(RECEIVED_VAR)
   public boolean getReceived() {
      return _received;
   }

   @JsonProperty(SENT_VAR)
   public boolean getSent() {
      return _sent;
   }

   @Override
   public boolean getTraffic() {
      return false;
   }

   public void setEbgp(boolean ebgp) {
      _ebgp = ebgp;
   }

   public void setIbgp(boolean ibgp) {
      _ibgp = ibgp;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);
      Iterator<?> paramKeys = parameters.keys();
      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();
         try {
            switch (paramKey) {
            case EBGP_VAR:
               setEbgp(parameters.getBoolean(paramKey));
               break;
            case IBGP_VAR:
               setIbgp(parameters.getBoolean(paramKey));
               break;
            case NODE_REGEX_VAR:
               setNodeRegex(parameters.getString(paramKey));
               break;
            case RECEIVED_VAR:
               setReceived(parameters.getBoolean(paramKey));
               break;
            case SENT_VAR:
               setSent(parameters.getBoolean(paramKey));
               break;
            default:
               throw new BatfishException("Unknown key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
         catch (JSONException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }
   }

   public void setNodeRegex(String nodeRegex) {
      _nodeRegex = nodeRegex;
   }

   public void setReceived(boolean received) {
      _received = received;
   }

   public void setSent(boolean sent) {
      _sent = sent;
   }

}
