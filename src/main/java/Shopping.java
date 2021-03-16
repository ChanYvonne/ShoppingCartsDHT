import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

public class Shopping {

    private Stub shoppingCart;
    private long customerId;

    public Shopping(long custId){
        customerId = custId;
        shoppingCart = new Stub();
    }

    /**  Each time a web client clicks “buy” (on a book, or a pair of shoes, or a bag of Japanese Nori...), the web
    application would construct an object describing the purpose (item number, price offered, number of units purchased,
     time of click, customer id) and store the object into the DHT. If an item was on a special sale, the object could
     also include some form of description of the discount offered and the deadline for completing the purchase to
     obtain that discount. */
    public void buy(long itemNum, int priceOffered, int unitsAdded, int buyTime, String dis, String dead){
        //create new item object
        JSONObject item = new JSONObject();
        item.put("item number", itemNum);
        item.put("price offered", priceOffered);
        item.put("units added", unitsAdded);
        item.put("time of click", buyTime);
        item.put("customer Id", customerId);
        item.put("discount type", dis);
        item.put("discount deadline", dead);

        boolean success = false;

        while (!success) {
            int currentVersion = shoppingCart.getVersion(customerId);
            int newVersion = currentVersion + 1;
            success = shoppingCart.put(customerId, item, newVersion) == 0;
        }
    }

    /** If the user clicks to “see my shopping cart” the application must list the current contents. For example,
    if a shopping cart contains a book, shoes and a bag of Nori, the list would show those three items – it can’t miss
    any or show duplicates! – and also the current total checkout cost.

    This returns an ArrayList of an ArrayList of Strings. It represents the cart in the following format:

    [{
        “price offered”: 1000,
        “units added”: 2,
        “time of click”: “20210113 23:30:52”,
        “customer id”: 123456789000,
        “discount type”: “BOGO”,
        “discount deadline”: “20210201 23:30:52”,
        "item number": 000010013731,
      },
      ...
      { "total cart cost": 11000}
      ]

    The last JSONObject will contain the total checkout cost in cents.
    */
    public ArrayList<JSONObject> listCart(){
        JSONObject cart = shoppingCart.getCart(customerId);
        Set<String> s = cart.keySet();
        Iterator<String> keys = s.iterator();

        ArrayList<JSONObject> listedCart = new ArrayList<JSONObject>();
        int totalCost = 0;
        System.out.println("listing Cart");

        while(keys.hasNext()) {
            String key = keys.next();
            if (cart.get(key) instanceof JSONObject) {
                JSONObject currentItem = (JSONObject) cart.get(key);

                //calculate cost
                Integer price = ((Long)currentItem.get("price offered")).intValue();
                Integer units = ((Long)currentItem.get("units added")).intValue();
                Integer itemCost = price * units;
                totalCost += itemCost;

                //adding item to cart list
                currentItem.put("item number", key);
                listedCart.add(currentItem);

            }
        }

        JSONObject totalCostJson = new JSONObject();
        totalCostJson.put("total car cost", totalCost);
        listedCart.add(totalCostJson);

        return listedCart;
    }

    /** Next to each item is a clickable button to “update number of units / delete”. This allows the user to change how
    many units of the same kind of item they are ordering. A change that results in 0 units of an item will be used to
    remove some item entirely. Change can be positive or negative. */
    public void updateCount(int change, long itemNumber){
        boolean success = false;
        while (!success) {
            int currentVersion = shoppingCart.getVersion(customerId);
            int newVersion = currentVersion + 1;
            success = shoppingCart.updateExistingCart(customerId, change, itemNumber, newVersion) == 0;
        }

    }

    /** If the user clicks to “checkout and pay”, the purchase will finalize, and this also removes items from the DHT. We
    * assume some external service will handle finalizing the purchase. */
    public void checkoutAndPay(){
        shoppingCart.removeCustomer(customerId);
    }
}
