package org.batfish.graphviz;

import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.common.datamodel.Prefix;
import org.batfish.job.BatfishJobResult;

public final class GraphvizResult extends BatfishJobResult<Map<String, byte[]>> {

   private final byte[] _graphBytes;

   private final String _graphFile;

   private final byte[] _htmlBytes;

   private final String _htmlFile;

   private final Prefix _prefix;

   private final byte[] _svgBytes;

   private final String _svgFile;

   public GraphvizResult(long elapsedTime, Prefix prefix, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _graphBytes = null;
      _graphFile = null;
      _htmlBytes = null;
      _htmlFile = null;
      _prefix = prefix;
      _svgBytes = null;
      _svgFile = null;
   }

   public GraphvizResult(long elapsedTime, String graphFile, byte[] graphBytes,
         String svgFile, byte[] svgBytes, String htmlFile, byte[] htmlBytes,
         Prefix prefix) {
      super(elapsedTime);
      _graphBytes = graphBytes;
      _graphFile = graphFile;
      _htmlBytes = htmlBytes;
      _htmlFile = htmlFile;
      _prefix = prefix;
      _svgBytes = svgBytes;
      _svgFile = svgFile;
   }

   @Override
   public void applyTo(Map<String, byte[]> output, BatfishLogger logger) {
      output.put(_graphFile, _graphBytes);
      output.put(_svgFile, _svgBytes);
      output.put(_htmlFile, _htmlBytes);
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
   }

   @Override
   public String toString() {
      return "<Computed graph for prefix: " + _prefix.toString() + ">";
   }

}
