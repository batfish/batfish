package org.batfish.z3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.batfish.main.BatfishException;

import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class NodJob implements Callable<NodJobResult> {

   private Path _dataPlanePath;
   private Path _queryPath;
   private Path _outputPath;

   private String readFile(File file) {
      String text = null;
      try {
         text = FileUtils.readFileToString(file);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to read file: " + file.toString(),
               e);
      }
      return text;
   }

   public NodJob(Path dataPlanePath, Path queryPath) {
      _dataPlanePath = dataPlanePath;
      _queryPath = queryPath;
   }

   public Path getOutputPath() {
      return _outputPath;
   }

   @Override
   public NodJobResult call() throws Exception {
      _outputPath = Paths.get(_queryPath.toString() + ".out");
      String concatenatedQuery = null;
      try {
         String dataPlane = readFile(_dataPlanePath.toFile());
         String queryText = readFile(_queryPath.toFile());
         concatenatedQuery = dataPlane + queryText;
      }
      catch (BatfishException e) {
         return NodJobResult.FAILURE;
      }

      Map<String, String> config = new HashMap<String, String>();
      config.put("fixedpoint.engine", "datalog");
      config.put("fixedpoint.datalog.default_relation", "doc");
      config.put("fixedpoint.print.answer", "true");
      try {
         Context ctx = new Context(config);
         ctx.parseSMTLIB2String(concatenatedQuery, null, null, null, null);
      }
      catch (Z3Exception e) {
         return NodJobResult.FAILURE;
      }
      return NodJobResult.SUCCESS;
   }

}
