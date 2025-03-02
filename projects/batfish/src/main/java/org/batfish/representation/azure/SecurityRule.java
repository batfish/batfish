package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityRule extends Resource implements Serializable {

    private final Properties _properties;

    public ExprAclLine getAclLine(){
        HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();

        // if there are prefixes, then we ignore single prefix field
        {
            if (_properties.getSourceAddressPrefixes().isEmpty())
                headerSpaceBuilder.addSrcIp(_properties.getSourceAddressPrefix().toIpSpace());

            if (_properties.getDestinationAddressPrefixes().isEmpty())
                headerSpaceBuilder.addDstIp(_properties.getDestinationAddressPrefix().toIpSpace());
        }

        // handle prefixes if there are
        {
            for (Prefix srcPrefix : _properties.getSourceAddressPrefixes())
                headerSpaceBuilder.addSrcIp(srcPrefix.toIpSpace());


            for (Prefix destPrefix : _properties.getDestinationAddressPrefixes())
                headerSpaceBuilder.addDstIp(destPrefix.toIpSpace());
        }

        headerSpaceBuilder.setIpProtocols(_properties.getProtocol());

        // handle port range(s) only if layer 4's protocol is TCP or UDP
        if((_properties.getProtocol().equals(IpProtocol.TCP)) ||
                _properties.getProtocol().equals(IpProtocol.UDP)){

            // if there are multiple port range, then we ignore single port range field

            if(_properties.getSourcePortRanges().isEmpty())
                headerSpaceBuilder.setSrcPorts(_properties.getSourcePortRange());
            else headerSpaceBuilder.setSrcPorts(_properties.getSourcePortRanges());

            if(_properties.getDestinationPortRanges().isEmpty())
                headerSpaceBuilder.setDstPorts(_properties.getDestinationPortRange());
            else headerSpaceBuilder.setDstPorts(_properties.getDestinationPortRanges());
        }
        else if (_properties.getProtocol().equals(IpProtocol.ICMP)){
            //todo: handle icmp type
            throw new BatfishException("Unsupported protocol: " + _properties.getProtocol());
        }


        LineAction action = _properties.getAccess().equals("Allow") ? LineAction.PERMIT  : LineAction.DENY;

        HeaderSpace headerSpace = headerSpaceBuilder.build();
        return ExprAclLine.builder()
                .setName(getName())
                .setMatchCondition(new MatchHeaderSpace(headerSpace))
                .setAction(action)
                .build();

    }

    @JsonCreator
    public SecurityRule(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties
    )
    {
        super(name, id, type);
        _properties = properties;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable{

        private final IpProtocol _protocol;
        private final SubRange _sourcePortRange;
        private final SubRange _destinationPortRange;
        private final List<SubRange> _sourcePortRanges;
        private final List<SubRange> _destinationPortRanges;
        private final Prefix _sourceAddressPrefix;
        private final Prefix _destinationAddressPrefix;
        private final List<Prefix> _sourceAddressPrefixes;
        private final List<Prefix> _destinationAddressPrefixes;
        private final String _access;
        private final int _priority;
        private final String _direction;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PORT) String sourcePortRange,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PORT) String destinationPortRange,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PORTS) List<String> sourcePortRanges,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PORTS) List<String> destinationPortRanges,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PREFIX) String sourceAddressPrefix,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PREFIX) String destinationAddressPrefix,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PREFIXES) List<String> sourceAddressPrefixes,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PREFIXES) List<String> destinationAddressPrefixes,
                @JsonProperty(value = AzureEntities.JSON_KEY_NSG_PROTOCOL, required = true) String protocol,
                @JsonProperty(value = AzureEntities.JSON_KEY_NSG_ACCESS, required = true) String access,
                @JsonProperty(value = AzureEntities.JSON_KEY_NSG_PRIORITY, required = true) int priority,
                @JsonProperty(value = AzureEntities.JSON_KEY_NSG_DIRECTION, required = true) String direction
        ){
            _protocol = getProtocol(protocol);
            _sourcePortRange = getSubRange(sourcePortRange);
            _destinationPortRange = getSubRange(destinationPortRange);
            _sourceAddressPrefix = getPrefix(sourceAddressPrefix);
            _destinationAddressPrefix = getPrefix(destinationAddressPrefix);
            _access = access;
            _priority = priority;
            _direction = direction;

            _sourceAddressPrefixes = sourceAddressPrefixes.stream()
                    .map(Properties::getPrefix)
                    .collect(Collectors.toCollection(ArrayList::new));

            _destinationAddressPrefixes = destinationAddressPrefixes.stream()
                    .map(Properties::getPrefix)
                    .collect(Collectors.toCollection(ArrayList::new));

            _sourcePortRanges = sourcePortRanges.stream()
                    .map(Properties::getSubRange)
                    .collect(Collectors.toCollection(ArrayList::new));

            _destinationPortRanges= destinationPortRanges.stream()
                    .map(Properties::getSubRange)
                    .collect(Collectors.toCollection(ArrayList::new));

        }

        private static IpProtocol getProtocol(String protocol) {
            if(protocol.equals("*"))
                return IpProtocol.ANY_0_HOP_PROTOCOL;
            return IpProtocol.fromString(protocol);
        }

        private static Prefix getPrefix(String prefix){
            if(prefix == null || prefix.equals("*"))
                return Prefix.ZERO;

            if(prefix.equals("Internet"))
                return Prefix.ZERO;

            return Prefix.parse(prefix);
        }

        private static SubRange getSubRange(String subRange){
            if(subRange == null || subRange.equals("*")) {
                return new SubRange(0,65535);
            }

            try {
                return new SubRange(Integer.parseInt(subRange));
            } catch (NumberFormatException e) {
                return new SubRange(subRange);
            }
        }

        public IpProtocol getProtocol() {
            return _protocol;
        }

        public SubRange getSourcePortRange() {
            return _sourcePortRange;
        }

        public SubRange getDestinationPortRange() {
            return _destinationPortRange;
        }

        public Prefix getSourceAddressPrefix() {
            return _sourceAddressPrefix;
        }

        public List<Prefix> getSourceAddressPrefixes() {
            return _sourceAddressPrefixes;
        }

        public Prefix getDestinationAddressPrefix() {
            return _destinationAddressPrefix;
        }

        public List<Prefix> getDestinationAddressPrefixes() {
            return _destinationAddressPrefixes;
        }

        public String getAccess() {
            return _access;
        }

        public int getPriority() {
            return _priority;
        }

        public String getDirection() {
            return _direction;
        }

        public List<SubRange> getSourcePortRanges() {
            return _sourcePortRanges;
        }

        public List<SubRange> getDestinationPortRanges() {
            return _destinationPortRanges;
        }
    }
}