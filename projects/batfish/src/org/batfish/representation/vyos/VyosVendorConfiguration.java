package org.batfish.representation.vyos;

import java.util.Set;

import org.batfish.main.ConfigurationFormat;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.VendorConversionException;

public class VyosVendorConfiguration extends VyosConfiguration implements
      VendorConfiguration {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Configuration _c;

   private ConfigurationFormat _format;

   private Set<String> _unimplementedFeatures;

   private Warnings _w;

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public Warnings getWarnings() {
      return _w;
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _format = format;
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      _c = new Configuration(_hostname);
      _c.setVendor(_format);
      return _c;
   }

}
