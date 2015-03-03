package org.batfish.coordinator;

import java.util.Date;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class Coordinator {

   private CloudQueue _queueAssignedWork;
   private CloudQueue _queueUnassignedWork;
   private CloudQueue _queueCompletedWork;   
   private String _storageConnectionString;
   
   public Coordinator(Settings settings) {
      
      _storageConnectionString = String.format("DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s",
                 settings.getStorageProtocol(), settings.getStorageAccountName(), settings.getStorageAccountKey());

      _queueAssignedWork = getQueue(settings.getQueueAssignedWork());
      _queueCompletedWork = getQueue(settings.getQueueCompletedWork());
      _queueUnassignedWork = getQueue(settings.getQueueUnassignedWork());
            
   }

   private CloudQueue getQueue(String queueName) {
      try {
         // Retrieve storage account from connection-string.
         CloudStorageAccount storageAccount = CloudStorageAccount
               .parse(_storageConnectionString);

         // Create the queue client.
         CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

         // Retrieve a reference to a queue.
         CloudQueue queue = queueClient.getQueueReference(queueName);

         // Create the queue if it doesn't already exist.
         queue.createIfNotExists();

         return queue;
      }
      catch (Exception e) {
         // Output the stack trace.
         e.printStackTrace();
      }

      return null;
   }
   
   private long getQueueLength(CloudQueue queue) {
      try {
         // Download the approximate message count from the server.
         queue.downloadAttributes();

         // Retrieve the newly cached approximate message count.
         return queue.getApproximateMessageCount();
      }
      catch (Exception e) {
         e.printStackTrace();
         return -1;
      }

   }
   
   public String getWorkStatus() {
    
      String retString = "";
      
      retString += "Length of unassigned work queue = " + getQueueLength(_queueUnassignedWork) + "\n";
      retString += "Length of assigned work queue = " + getQueueLength(_queueAssignedWork) + "\n";
      retString += "Length of completed work queue = " + getQueueLength(_queueCompletedWork);
      
      return retString;
   }
   
   public void run() {
      try {
      Thread.sleep(5 * 60 * 1000);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}