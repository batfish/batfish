include "datamodel.thrift"

namespace java org.batfish.storage

service Storage {

   //Throws if analysis does not exist on disk in the container
   datamodel.Analysis getAnalysis(1:string containerName, 2:string analysisName) throws (1:datamodel.BatfishException e);

   //Throws if the analysis by this name already exists in the container specified
   datamodel.Analysis createAnalysis(1:string containerName, 2:string analysisName) throws (1:datamodel.BatfishException e);

   //Throws if the analysis does not exist in the given container
   datamodel.Analysis updateAnalysis(1:string containerName, 2:datamodel.Analysis analysis) throws (1:datamodel.BatfishException e);

   //if the analysis is not found in the container, creates a new analysis otherwise updates it
   datamodel.Analysis saveOrUpdateAnalysis(1:string containerName, 2:datamodel.Analysis analysis);

   //returns false when delete is called on a non-empty analysis with force=false
   bool deleteAnalysis(1:string containerName, 2:string analysisName, 2:bool force)

}



