package org.batfish.datamodel;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationDiff implements AnswerElement {

   private static final String AS_PATH_ACCESS_LISTS_DIFF_VAR = "asPathAccessListsDiff";
   private AsPathAccessListsDiff _asPathAccessListsDiff;
   
   private static final String COMMUNITY_LISTS_DIFF_VAR = "comunityListsDiff";
   private CommunityListsDiff _communityListsDiff;
   
   private static final String IP_ACCESS_LIST_DSIFF_VAR = "ipAccessListstDiff";
   private IpAccessListsDiff _ipAccessListsDiff;
   
   @JsonCreator()
   public ConfigurationDiff()
   {
      
   }
   
   public ConfigurationDiff(Configuration a, Configuration b)
   {
      _asPathAccessListsDiff = new AsPathAccessListsDiff(a.getAsPathAccessLists(), b.getAsPathAccessLists());
      _communityListsDiff = new CommunityListsDiff(a.getCommunityLists(), b.getCommunityLists());
      _ipAccessListsDiff = new IpAccessListsDiff(a.getIpAccessLists(), b.getIpAccessLists());
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO Auto-generated method stub
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   
   @JsonProperty(AS_PATH_ACCESS_LISTS_DIFF_VAR)
   public AsPathAccessListsDiff get_asPathAccessListDiff() {
      return _asPathAccessListsDiff;
   }
   
   public void set_asPathAccessListDiff(AsPathAccessListsDiff d) {
      _asPathAccessListsDiff = d;  
   }
   
   /**
    * @return the _communityListDiff
    */
   @JsonProperty(COMMUNITY_LISTS_DIFF_VAR)
   public CommunityListsDiff get_communityListDiff() {
      return _communityListsDiff;
   }

   /**
    * @param _communityListDiff the _communityListDiff to set
    */
   public void set_communityListDiff(CommunityListsDiff _communityListsDiff) {
      this._communityListsDiff = _communityListsDiff;
   }

   /**
    * @return the _ipAccessListListDiff
    */
   @JsonProperty(IP_ACCESS_LIST_DSIFF_VAR)
   public IpAccessListsDiff get_ipAccessListListDiff() {
      return _ipAccessListsDiff;
   }

   /**
    * @param _ipAccessListListDiff the _ipAccessListListDiff to set
    */
   public void set_ipAccessListListDiff(IpAccessListsDiff _ipAccessListsDiff) {
      this._ipAccessListsDiff = _ipAccessListsDiff;
   }
}
