package org.batfish.datamodel;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationDiff implements AnswerElement {

   private static final String AS_PATH_ACCESS_LIST_DIFF_VAR = "asPathAccessListDiff";
   private AsPathAccessListDiff _asPathAccessListDiff;
   
   private static final String COMMUNITY_LIST_DIFF_VAR = "comunityListDiff";
   private CommunityListDiff _communityListDiff;
   
   private static final String IP_ACCESS_LIST_DIFF_VAR = "ipAccessListDiff";
   private IpAccessListDiff _ipAccessListListDiff;
   
   @JsonCreator()
   public ConfigurationDiff()
   {
      
   }
   
   public ConfigurationDiff(Configuration a, Configuration b)
   {
      _asPathAccessListDiff = new AsPathAccessListDiff(a.getAsPathAccessLists(), b.getAsPathAccessLists());
      _communityListDiff = new CommunityListDiff(a.getCommunityLists(), b.getCommunityLists());
      _ipAccessListListDiff = new IpAccessListDiff(a.getIpAccessLists(), b.getIpAccessLists());
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO Auto-generated method stub
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   
   @JsonProperty(AS_PATH_ACCESS_LIST_DIFF_VAR)
   public AsPathAccessListDiff get_asPathAccessListDiff() {
      return _asPathAccessListDiff;
   }
   
   public void set_asPathAccessListDiff(AsPathAccessListDiff d) {
      _asPathAccessListDiff = d;  
   }
   
   /**
    * @return the _communityListDiff
    */
   @JsonProperty(COMMUNITY_LIST_DIFF_VAR)
   public CommunityListDiff get_communityListDiff() {
      return _communityListDiff;
   }

   /**
    * @param _communityListDiff the _communityListDiff to set
    */
   public void set_communityListDiff(CommunityListDiff _communityListDiff) {
      this._communityListDiff = _communityListDiff;
   }

   /**
    * @return the _ipAccessListListDiff
    */
   @JsonProperty(IP_ACCESS_LIST_DIFF_VAR)
   public IpAccessListDiff get_ipAccessListListDiff() {
      return _ipAccessListListDiff;
   }

   /**
    * @param _ipAccessListListDiff the _ipAccessListListDiff to set
    */
   public void set_ipAccessListListDiff(IpAccessListDiff _ipAccessListListDiff) {
      this._ipAccessListListDiff = _ipAccessListListDiff;
   }
}
