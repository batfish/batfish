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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityRule extends Resource implements Serializable {

    private final @Nonnull Properties _properties;

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
            @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties
    )
    {
        super(name, id, type);
        checkArgument(properties != null, "properties must be provided");
        _properties = properties;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable{

        private final @Nonnull IpProtocol _protocol;
        private final @Nonnull SubRange _sourcePortRange;
        private final @Nonnull SubRange _destinationPortRange;
        private final @Nonnull List<SubRange> _sourcePortRanges;
        private final @Nonnull List<SubRange> _destinationPortRanges;
        private final @Nonnull Prefix _sourceAddressPrefix;
        private final @Nonnull Prefix _destinationAddressPrefix;
        private final @Nonnull List<Prefix> _sourceAddressPrefixes;
        private final @Nonnull List<Prefix> _destinationAddressPrefixes;
        private final @Nonnull String _access;
        private final int _priority;
        private final @Nonnull String _direction;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PORT) @Nullable String sourcePortRange,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PORT) @Nullable String destinationPortRange,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PORTS) @Nullable List<String> sourcePortRanges,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PORTS) @Nullable List<String> destinationPortRanges,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PREFIX) @Nullable String sourceAddressPrefix,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PREFIX) @Nullable String destinationAddressPrefix,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_SRC_PREFIXES) @Nullable List<String> sourceAddressPrefixes,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DST_PREFIXES) @Nullable List<String> destinationAddressPrefixes,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_PROTOCOL) @Nullable String protocol,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_ACCESS) @Nullable String access,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_PRIORITY) @Nullable Integer priority,
                @JsonProperty(AzureEntities.JSON_KEY_NSG_DIRECTION) @Nullable String direction
        ){
            checkArgument(protocol != null, "protocol must be provided");
            checkArgument(access != null, "access must be provided");
            checkArgument(priority != null, "priority must be provided");
            checkArgument(direction != null, "direction must be provided");
            if (sourcePortRanges == null) sourcePortRanges = new ArrayList<>();
            if (destinationPortRanges == null) destinationPortRanges = new ArrayList<>();
            if (sourceAddressPrefixes == null) sourceAddressPrefixes = new ArrayList<>();
            if (destinationAddressPrefixes == null) destinationAddressPrefixes = new ArrayList<>();

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