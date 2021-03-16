import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		boolean test1 = true;
		boolean test2 = true;

		int C = 10; // number of concurrent clients
		Client_Cosmos[] customers = new Client_Cosmos[C];
		for (int i = 0; i < C; i++) {
			DHT_server_cosmos server = new DHT_server_cosmos();
			customers[i] = new Client_Cosmos(server);
		}

		int items = 15; //number of items a customer buys

		long[] customerIds = new long[C];
		long customerIdCounter = 123456789000L;
		for (int i = 0; i < C; i++){
			customerIdCounter++;
			customerIds[i] = customerIdCounter;
		}

		/* Testing C customers buying 15 items*/
		long itemNumberCounter = 000010000000L;

		if (test1) {
			for (int c = 0; c < C; c++) {
				Client_Cosmos testCustomer = customers[c];
				String customerId = String.valueOf(customerIds[c]);

				for (int i = 0; i < items; i++) {

					//randomly generated item
					int price = (int) (Math.random() * 10000);
					int units = (int) (Math.random() * 10);
					int buyTime = (int) (Math.random() * 100000000);
					String discount = (int) (Math.random() * 2) == 1 ? "BOGO" : "";
					String deadline = String.valueOf((int) (Math.random() * 100000000));

					testCustomer.buy(customerId, itemNumberCounter, price, units, buyTime, discount, deadline);
					itemNumberCounter++;
				}

				ArrayList<JSONObject> currentCart = testCustomer.listCart(customerId);
				for (int i = 0; i < currentCart.size(); i++) {
					System.out.println(currentCart.get(i).toString());
				}
				testCustomer.updateCount(customerId, 2, 000010000000L);
				ArrayList<JSONObject> updatedCart = testCustomer.listCart(customerId);
				for (int i = 0; i < updatedCart.size(); i++) {
					System.out.println(updatedCart.get(i).toString());
				}
				testCustomer.checkoutAndPay(customerId);

			}
		}

		if (test2) {

			System.out.println("------------------------TEST 2 ---------------------------------");

			/* Testing Concurrent Users accessing the same shopping cart*/
			DHT_server_cosmos server = new DHT_server_cosmos();
			Client_Cosmos client1 = new Client_Cosmos(server);
			Client_Cosmos client2 = new Client_Cosmos(server);
			Client_Cosmos client3 = new Client_Cosmos(server);

			String singleAccount = String.valueOf(customerIdCounter);

			long item1 = itemNumberCounter;
			itemNumberCounter++;
			long item2 = itemNumberCounter;
			itemNumberCounter++;
			long item3 = itemNumberCounter;
			itemNumberCounter++;

			//randomly generated item
			int price1 = (int) (Math.random() * 10000);
			int units1 = (int) (Math.random() * 10) + 1;
			int buyTime1 = (int) (Math.random() * 100000000);
			String discount1 = (int) (Math.random() * 2) == 1 ? "BOGO" : "";
			String deadline1 = String.valueOf((int) (Math.random() * 100000000));

			int price2 = (int) (Math.random() * 10000);
			int units2 = (int) (Math.random() * 10) + 1;
			int buyTime2 = (int) (Math.random() * 100000000);
			String discount2 = (int) (Math.random() * 2) == 1 ? "BOGO" : "";
			String deadline2 = String.valueOf((int) (Math.random() * 100000000));

			int price3 = (int) (Math.random() * 10000);
			int units3 = (int) (Math.random() * 10) + 1;
			int buyTime3 = (int) (Math.random() * 100000000);
			String discount3 = (int) (Math.random() * 2) == 1 ? "BOGO" : "";
			String deadline3 = String.valueOf((int) (Math.random() * 100000000));

			// clients buying different items
			client1.buy(singleAccount, item1, price1, units1, buyTime1, discount1, deadline1);
			client2.buy(singleAccount, item2, price2, units2, buyTime2, discount2, deadline2);
			client3.buy(singleAccount, item3, price3, units3, buyTime3, discount3, deadline3);

			ArrayList<JSONObject> currentCart = client3.listCart(singleAccount);
			for (int i = 0; i < currentCart.size(); i++) {
				System.out.println(currentCart.get(i).toString());
			}

			//they all update the same item -- should result in 3 total units of item1
			client1.updateCount(singleAccount, 1, item1);
			client2.updateCount(singleAccount, -1, item1);
			client3.updateCount(singleAccount, 2, item1);

			//one client lists cart while the other 2 update the same item
			currentCart = client1.listCart(singleAccount);
			for (int i = 0; i < currentCart.size(); i++) {
				System.out.println(currentCart.get(i).toString());
			}
			client2.updateCount(singleAccount, 3, item2);
			client3.updateCount(singleAccount, 2, item2);


			//one client updates to delete and then another client adds it back
			client1.updateCount(singleAccount, -1, item3);
			currentCart = client2.listCart(singleAccount);
			for (int i = 0; i < currentCart.size(); i++) {
				System.out.println(currentCart.get(i).toString());
			}
			client3.updateCount(singleAccount, 2, item3);

			//one last listing is to check change is correct
			currentCart = client3.listCart(singleAccount);
			for (int i = 0; i < currentCart.size(); i++) {
				System.out.println(currentCart.get(i).toString());
			}
		}

		/* Only Testing for Homework 2 */
//		int N = 8; // number of servers
//		DHT_server[] servers = new DHT_server[N];
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

	}


}
