import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Set;
import org.json.simple.JSONObject;
import javafx.util.Pair;
import org.json.simple.parser.JSONParser;

public class Stub {

    private DHT_server server;

    public Stub(){
        server = new DHT_server();
    }

    public JSONObject getCart(long customerId){
        Pair<JSONObject, Integer> value = getCartVersion(customerId);
        return value.getKey();

    }

    public int getVersion(long customerId){
        Pair<JSONObject, Integer> value = getCartVersion(customerId);
        return value.getValue();
    }

    public void removeCustomer(long customerId) {
        String key = lookUpServer(customerId);
        server.remove(key);
    }


    /** Puts the newItem into the shopping cart and accounts for when the the item already exists. Returns -1 if
     * the version was not the next one in the sequence. Returns 0 if cart was updated successfully.  */
    public int put(long customerId, JSONObject newItem, int version) {
        int result = -1;
        JSONObject newCart = new JSONObject();

        //get current value in DHT
        Pair<JSONObject, Integer> valueVersion = getCartVersion(customerId);
        JSONObject currentShoppingCart = valueVersion.getKey();

        try {

            if (currentShoppingCart != null) { // if customer exists in the DHT yet, update existing cart
                newCart = addItemtoCart(currentShoppingCart, newItem, (Long)newItem.get("item number")); // add +1 to units to item corresponding if shopping cart
            }else { // if customer doesn't exist in the DHT yet, create new cart with item
                newCart.put(newItem.get("item number"), newItem); // item number : item
            }

            //put new shoppingCart value in DHT
            byte[] byteValue = newCart.toString().getBytes("UTF-8");
            result = server.put(lookUpServer(customerId), byteValue, Integer.valueOf(version));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return result;
    }

    private Pair<JSONObject, Integer> getCartVersion(long customerId){
        String key = lookUpServer(customerId);
        Pair<byte[], Integer> valueVersion = server.get(key);
        Pair<JSONObject, Integer> value = new Pair<JSONObject, Integer>(null, -1);
        try {
            if (valueVersion != null) {
                String byteString = new String(valueVersion.getKey());
                JSONParser parser = new JSONParser();
                JSONObject currentShoppingCart = (JSONObject) parser.parse(byteString); //shopping cart

                Integer version = Integer.valueOf(valueVersion.getValue());
                value = new Pair<JSONObject, Integer>(currentShoppingCart, version);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return value;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    //method to hash and determine which server to store info, call the server, and return corresponding key
    private String lookUpServer(long customerId) {
        String pathname = "/csc/" + String.valueOf(customerId);
        String lookUpKey;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pathname.getBytes("UTF-8"));
            lookUpKey = bytesToHex(hash);
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        return lookUpKey;
    }

    /** Updates existing cart with existing item with itemNumber with a change in the units. If the number of new units
     * after adding change is 0, it removes the item from the shopping cart corresponding to customerId. Returns -1 if
     * the version was not the next one in the sequence. Returns 0 if cart was updated successfully. Assumes item already exists
     * in the cart */
    public int updateExistingCart(long customerId, int change, long itemNumber, int version){
        JSONObject shoppingCart = getCart(customerId);
        int result = -1;
        Set<String> s = shoppingCart.keySet();
        Iterator<String> keys = s.iterator();

        while(keys.hasNext()) {
            String key = keys.next();
            if (shoppingCart.get(key) instanceof JSONObject) {
                System.out.println("updating units of " + String.valueOf(itemNumber));
                if (Long.valueOf(key).equals(Long.valueOf(itemNumber))) {
                    JSONObject currentItem = (JSONObject) shoppingCart.get(key);
                    Integer currentUnits = ((Long)currentItem.get("units added")).intValue();
                    Integer newUnits = change + currentUnits;
                    if (newUnits == 0) {
                        System.out.println("Updating to 0, removing shopping cart");
                        shoppingCart.remove(key);
                        break;
                    }else{
                        currentItem.put("units added", newUnits);
                    }
                }
            }
        }
        try {
            //put new shoppingCart value in DHT
            byte[] byteValue = shoppingCart.toString().getBytes("UTF-8");
            result = server.put(lookUpServer(customerId), byteValue, Integer.valueOf(version));
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    /* Updates JSON Object with new item. If item exists in the shopping cart, add change to number of units. If item does not
    * exist, add item to the shopping cart list.  */
    private JSONObject addItemtoCart(JSONObject shoppingCart,  JSONObject item, long itemNum){

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
}

