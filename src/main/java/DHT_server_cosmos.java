import com.google.common.collect.HashBasedTable;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;
import com.microsoft.azure.storage.table.TableQuery.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.json.simple.JSONObject;
import javafx.util.Pair;
import org.json.simple.parser.JSONParser;


public class DHT_server_cosmos{
    // Define the connection-string with your values.

    public static final String connectionString =
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=yvonne-chan;" +
                    "AccountKey=FYETu5nVinPFUqqFxb6nFvxClV2JgEDHGgH3i5AC4VkBPM4rCXYpWGy1ucOwVDWWvPSwokWSply7lAotMDw5EA==;" +
                    "TableEndpoint=https://yvonne-chan.table.cosmos.azure.com:443/;";


    private Hashtable<String, Long> partitionKeys;
    private Long partitionNum;

    public DHT_server_cosmos(){
        partitionKeys = new Hashtable<>();
        partitionNum = 0L;
    }


    public void createTable(){
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create the table if it doesn't exist.
            String tableName = "shopping-carts";
            CloudTable cloudTable = tableClient.getTableReference(tableName);
            cloudTable.createIfNotExists();
        } catch (Exception e)   {
            // Output the stack trace.
            e.printStackTrace();
        }
    }


    public JSONObject getShoppingCart(String customerId) {
        JSONParser parser = new JSONParser();
        JSONObject currentShoppingCart = new JSONObject();

        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("shopping-carts");

            String shard = String.valueOf(partitionKeys.get(customerId));
            // Retrieve the entity
            TableOperation retrieveEntity =
                    TableOperation.retrieve(shard, customerId, CustomerEntity.class);

            // Submit the operation to the table service and get the specific entity.
            CustomerEntity specificEntity =
                    cloudTable.execute(retrieveEntity).getResultAsType();

            // Output the entity.
            if (specificEntity != null)
            {
                byte[] currentCart = specificEntity.getCart();
                String byteString = new String(currentCart);
                currentShoppingCart = (JSONObject) parser.parse(byteString);
            }

        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }

        return currentShoppingCart;
    }

    public void put(String customerId, JSONObject newItem) {
        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("shopping-carts");

            String shard = String.valueOf(partitionKeys.get(customerId));
            // Retrieve the entity
            TableOperation retrieveEntity =
                    TableOperation.retrieve(shard, customerId, CustomerEntity.class);

            // Submit the operation to the table service and get the specific entity.
            CustomerEntity specificEntity =
                    cloudTable.execute(retrieveEntity).getResultAsType();

            JSONParser parser = new JSONParser();
            byte[] currentCart = specificEntity.getCart();
            String byteString = new String(currentCart);
            JSONObject shoppingCart = new JSONObject();
            if (byteString.equals("")){
                shoppingCart.put(newItem.get("item number"), newItem);

            }else {
                JSONObject cart = (JSONObject) parser.parse(byteString);
                long itemNumber = (long) newItem.get("item number");
                shoppingCart = addItemtoCart(cart, newItem, itemNumber);
            }

            byte[] newCart = shoppingCart.toString().getBytes("UTF-8");

            specificEntity.setCart(newCart);

            // Create an operation to replace the entity.
            TableOperation replaceEntity = TableOperation.insertOrReplace(specificEntity);

            // Submit the operation to the table service.
            cloudTable.execute(replaceEntity);
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public boolean customerExists(String customerId){
        byte[] currentCart = null;
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("shopping-carts");

            String shard = String.valueOf(partitionKeys.get(customerId));
            // Retrieve the entity
            TableOperation retrieveEntity =
                    TableOperation.retrieve(shard, customerId, CustomerEntity.class);

            // Submit the operation to the table service and get the specific entity.
            CustomerEntity specificEntity =
                    cloudTable.execute(retrieveEntity).getResultAsType();

            currentCart = specificEntity.getCart();
        }catch (Exception e)
        {
            System.out.println("Customer does not exist!");
        }
        return currentCart != null;
    }

    public void addCustomerToTable(String customerId) {
        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("shopping-carts");

            // Create a new customer entity.
            CustomerEntity customer1 = new CustomerEntity(customerId, String.valueOf(partitionNum));
            partitionKeys.put(customerId, partitionNum);
            partitionNum++;
            byte[] emptyCart = "".toString().getBytes("UTF-8");
            customer1.setCart(emptyCart);

            // Create an operation to add the new customer to the people table.
            TableOperation insertCustomer1 = TableOperation.insertOrReplace(customer1);

            // Submit the operation to the table service.
            cloudTable.execute(insertCustomer1);
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    public void update(String customerId, int change, long itemNumber) {
        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("shopping-carts");

            String shard = String.valueOf(partitionKeys.get(customerId));
            // Retrieve the entity
            TableOperation retrieveEntity =
                    TableOperation.retrieve(shard, customerId, CustomerEntity.class);

            // Submit the operation to the table service and get the specific entity.
            CustomerEntity specificEntity =
                    cloudTable.execute(retrieveEntity).getResultAsType();

            JSONParser parser = new JSONParser();
            byte[] currentCart = specificEntity.getCart();
            String byteString = new String(currentCart);
            JSONObject currentShoppingCart = (JSONObject) parser.parse(byteString);

            byte[] newCart;

            JSONObject updated = updateExistingCart(currentShoppingCart, change, itemNumber);
            newCart = updated.toString().getBytes("UTF-8");

            specificEntity.setCart(newCart);

            // Create an operation to replace the entity.
            TableOperation replaceEntity = TableOperation.replace(specificEntity);

            // Submit the operation to the table service.
            cloudTable.execute(replaceEntity);
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

    /* asssumes iteam alrready exists in shopping cart. Removes is change amounts to 0. */
    private JSONObject updateExistingCart(JSONObject shoppingCart, int change, long itemNumber){

        Set<String> s = shoppingCart.keySet();
        Iterator<String> keys = s.iterator();

        while(keys.hasNext()) {
            String key = keys.next();
            if (shoppingCart.get(key) instanceof JSONObject) {
//                System.out.println("updating units of " + String.valueOf(itemNumber));
                if (Long.valueOf(key).equals(Long.valueOf(itemNumber))) {
                    JSONObject currentItem = (JSONObject) shoppingCart.get(key);
                    Integer currentUnits = ((Long)currentItem.get("units added")).intValue();
                    Integer newUnits = change + currentUnits;
                    if (newUnits == 0) {
//                        System.out.println("Updating to 0, removing shopping cart");
                        shoppingCart.remove(key);
                        break;
                    }else{
                        currentItem.put("units added", newUnits);
                    }
                }
            }
        }

        return shoppingCart;
    }

    /* Updates JSON Object with new item. If item exists in the shopping cart, add change to number of units. If item does not
     * exist, add item to the shopping cart list.  */
    private JSONObject addItemtoCart(JSONObject shoppingCart, JSONObject item, long itemNum){

        Set<String> s = shoppingCart.keySet();
        Iterator<String> keys = s.iterator();

        boolean itemExists = false;

        while(keys.hasNext()) {
            String key = keys.next();
            if (shoppingCart.get(key) instanceof JSONObject) {
                System.out.println("adding item to cart");
                if (Long.valueOf(key).equals(Long.valueOf(itemNum))) {
                    itemExists = true;
                    JSONObject currentItem = (JSONObject) shoppingCart.get(key);
                    Integer currentUnits = ((Long)currentItem.get("units added")).intValue();
                    Integer addedUnits = ((Long)item.get("units added")).intValue();
                    currentItem.put("units added", addedUnits + currentUnits);
                }
            }
        }
        if (!itemExists){
            shoppingCart.put(itemNum, item);
        }

        return shoppingCart;
    }

    public void remove(String customerId) {
        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(connectionString);

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("shopping-carts");

            String shard = String.valueOf(partitionKeys.get(customerId));
            // Retrieve the entity
            TableOperation retrieveEntity =
                    TableOperation.retrieve(shard, customerId, CustomerEntity.class);

            // Submit the operation to the table service and get the specific entity.
            CustomerEntity specificEntity =
                    cloudTable.execute(retrieveEntity).getResultAsType();

            // Create an operation to delete the entity.
            TableOperation deleteEntity = TableOperation.delete(specificEntity);

            // Submit the delete operation to the table service.
            cloudTable.execute(deleteEntity);
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }
    }


}
