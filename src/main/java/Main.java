import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		int N = 8; // number of servers
		DHT_server[] servers = new DHT_server[N];

		int C = 10; // number of concurrent clients
		Client_Cosmos[] customers = new Client_Cosmos[C];
		for (int i = 0; i < C; i++) {
			customers[i] = new Client_Cosmos();
		}

		int items = 15; //number of items a customer buys

		long[] customerIds = new long[C];
		long customerIdCounter = 123456789000L;
		for (int i = 0; i < C; i++){
			customerIdCounter++;
			customerIds[i] = customerIdCounter;
		}

		/* Testing 1 customer buying 15 items*/
		Client_Cosmos customer1 = customers[0];
		String customerId = String.valueOf(customerIds[0]);
		long itemNumberCounter = 000010000000L;
		for (int i = 0; i < items; i++) {

			//randomly generated item
			int price = (int)(Math.random() * 10000);
			int units = (int)(Math.random() * 10);
			int buyTime = (int)(Math.random() * 100000000);
			String discount = (int)(Math.random() * 2) == 1 ? "BOGO" : "";
			String deadline = String.valueOf((int)(Math.random() * 100000000));

			customer1.buy(customerId, itemNumberCounter, price, units, buyTime, discount, deadline);
			itemNumberCounter++;
		}

		/* Only Testing for Homework 2 */
//		Shopping[] customers = new Shopping[C];
//
//		int items = 10; //number of items a customer buys
//
//		long customerIdCounter = 123456789000L;
//		for (int i = 0; i < C; i++){
//			customerIdCounter++;
//			customers[i] = new Shopping(customerIdCounter);
//		}
//
//		//test one customer buying 10 items and checking out
//		Shopping customer1 = customers[0];
//		long itemNumberCounter = 000010000000L;
//		for (int i = 0; i < items; i++) {
//
//			//randomly generated item
//			int price = (int)(Math.random() * 10000);
//			int units = (int)(Math.random() * 10);
//			int buyTime = (int)(Math.random() * 100000000);
//			String discount = (int)(Math.random() * 2) == 1 ? "BOGO" : "";
//			String deadline = String.valueOf((int)(Math.random() * 100000000));
//
//			customer1.buy(itemNumberCounter, price, units, buyTime, discount, deadline);
//			itemNumberCounter++;
//		}
//
		ArrayList<JSONObject> currentCart = customer1.listCart(customerId);
		for (int i = 0; i < currentCart.size(); i ++){
			System.out.println(currentCart.get(i).toString());
		}
		customer1.updateCount(customerId, 2, 000010000000L);
		ArrayList<JSONObject> updatedCart = customer1.listCart(customerId);
		for (int i = 0; i < updatedCart.size(); i ++){
			System.out.println(updatedCart.get(i).toString());
		}
		customer1.checkoutAndPay(customerId);

	}


}
