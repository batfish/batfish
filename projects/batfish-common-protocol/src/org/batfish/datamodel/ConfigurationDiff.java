package org.batfish.datamodel;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationDiff implements AnswerElement {

   private static final String AS_PATH_ACCESS_LISTS_DIFF_VAR = "asPathAccessListsDiff";
   private static final String COMMUNITY_LISTS_DIFF_VAR = "comunityListsDiff";
   private static final String IP_ACCESS_LIST_DSIFF_VAR = "ipAccessListstDiff";
   private AsPathAccessListsDiff _asPathAccessListsDiff;
   private CommunityListsDiff _communityListsDiff;
   private InterfaceListsDiff _interfaceListsDiff;
   private IpAccessListsDiff _ipAccessListsDiff;

   @JsonCreator()
   public ConfigurationDiff() {

   }

   public ConfigurationDiff(Configuration a, Configuration b) {
      _asPathAccessListsDiff = new AsPathAccessListsDiff(
            a.getAsPathAccessLists(), b.getAsPathAccessLists());
      _communityListsDiff = new CommunityListsDiff(a.getCommunityLists(),
            b.getCommunityLists());
      _interfaceListsDiff = new InterfaceListsDiff(a.getInterfaces(),
            b.getInterfaces());
      _ipAccessListsDiff = new IpAccessListsDiff(a.getIpAccessLists(),
            b.getIpAccessLists());
   }

   public InterfaceListsDiff getInterfaceListsDiff() {
      return _interfaceListsDiff;
   }

   public void setInterfaceListsDiff(InterfaceListsDiff interfaceListsDiff) {
      _interfaceListsDiff = interfaceListsDiff;
   }

   @JsonProperty(IP_ACCESS_LIST_DSIFF_VAR)
   public IpAccessListsDiff get_ipAccessListListDiff() {
      return _ipAccessListsDiff;
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_DIFF_VAR)
   public AsPathAccessListsDiff getAsPathAccessListDiff() {
      return _asPathAccessListsDiff;
   }

   @JsonProperty(COMMUNITY_LISTS_DIFF_VAR)
   public CommunityListsDiff getCommunityListDiff() {
      return _communityListsDiff;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void set_ipAccessListListDiff(IpAccessListsDiff _ipAccessListsDiff) {
      this._ipAccessListsDiff = _ipAccessListsDiff;
   }

   public void setAsPathAccessListDiff(AsPathAccessListsDiff d) {
      _asPathAccessListsDiff = d;
   }

   public void setCommunityListDiff(CommunityListsDiff _communityListsDiff) {
      this._communityListsDiff = _communityListsDiff;
   }
}
