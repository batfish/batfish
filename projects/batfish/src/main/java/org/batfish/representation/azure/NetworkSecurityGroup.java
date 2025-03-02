package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents an Azure Network Security Group (NSG)
 * <a href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/networksecuritygroups?pivots=deployment-language-arm-template">Resource link</a>
 * <p>
 * Partially implemented :
 * <li>
 *     do not support service tags except "Internet"
 * </li>
 *  </p>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkSecurityGroup extends Resource implements Serializable {

    private final @Nonnull Properties _properties;
    private final @Nonnull List<AclLine> _inboundAclLines;
    private final @Nonnull List<AclLine> _outboundAclLines;

    @JsonCreator
    public NetworkSecurityGroup(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties
    ) {
        super(name, id, type);
        checkArgument(properties != null, "properties must be provided");
        _properties = properties;

        _inboundAclLines = new ArrayList<>();
        _outboundAclLines = new ArrayList<>();
        generateAclLines();
    }

    /**
     * Apply to the specified interface, this nsg converted to an Acl.
     *         GenerateAclLines must be called before.
     * @param iface
     */
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

    /**
     * Generates _inboundAclLines and _outboundAclLines from this NSG
     */
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

        private final @Nonnull List<SecurityRule> _securityRules;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SECURITY_RULES) @Nullable List<SecurityRule> securityRules,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DEFAULT_SECURITY_RULES) @Nullable List<SecurityRule> defaultSecurityRules
        )
        {
            if (securityRules == null) securityRules = new ArrayList<>();
            if (defaultSecurityRules != null) securityRules.addAll(defaultSecurityRules);

            // sorting the rules according to priority so each security rule
            // can be added to an acl as Line (first line is executed before second etc..)
            securityRules.sort(
                    Comparator.comparingInt(securityRule -> securityRule.getProperties().getPriority()));
            _securityRules = securityRules;
        }

        public List<SecurityRule> getSecurityRules() {
            return _securityRules;
        }
    }
}
