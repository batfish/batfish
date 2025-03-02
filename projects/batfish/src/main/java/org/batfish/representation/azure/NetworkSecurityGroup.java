package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkSecurityGroup extends Resource implements Serializable {

    private final Properties _properties;
    private final List<AclLine> _inboundAclLines;
    private final List<AclLine> _outboundAclLines;

    @JsonCreator
    public NetworkSecurityGroup(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties) {
        super(name, id, type);
        _properties = properties;

        _inboundAclLines = new ArrayList<>();
        _outboundAclLines = new ArrayList<>();
        generateAclLines();
    }

    public void applyToInterface(Interface iface){

        Configuration configuration = iface.getOwner();

        if(!_inboundAclLines.isEmpty()) {
            IpAccessList inboundAcl = IpAccessList.builder()
                    .setLines(_inboundAclLines)
                    .setName(getCleanId() + "-inbound")
                    .setOwner(configuration)
                    .build();

            configuration.getIpAccessLists().put(inboundAcl.getName(), inboundAcl);

            iface.setInboundFilter(inboundAcl);
        }

        if(!_outboundAclLines.isEmpty()) {
            IpAccessList outboundAcl = IpAccessList.builder()
                    .setName(getCleanId() + "-outbound")
                    .setLines(_outboundAclLines)
                    .setOwner(configuration)
                    .build();

            configuration.getIpAccessLists().put(outboundAcl.getName(), outboundAcl);

            iface.setOutgoingFilter(outboundAcl);
        }
    }

    private void generateAclLines(){
        for(SecurityRule securityRule : _properties.getSecurityRules()){
            if(securityRule.getProperties().getDirection().equals("Inbound"))
                _inboundAclLines.add(securityRule.getAclLine());
            else if(securityRule.getProperties().getDirection().equals("Outbound"))
                _outboundAclLines.add(securityRule.getAclLine());
        }
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {
        private final List<SecurityRule> _securityRules;

        @JsonCreator
        public Properties(
                @JsonProperty("securityRules") List<SecurityRule> securityRules)
        {
            _securityRules = securityRules;

            // sorting the rules according to priority so it is easier for the next steps
            _securityRules.sort(
                    Comparator.comparingInt(securityRule -> securityRule.getProperties().getPriority()));
        }

        public List<SecurityRule> getSecurityRules() {
            return _securityRules;
        }
    }
}
