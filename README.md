# ShoppingCartsDHT

This class contains the source files for both HW2 and HW3. As I was unable to showcase
the my completed efforts for HW2, I wanted to include them here for your records. I apologize again for not
completing my HW2 on time. I got swept away with the Digital Ag Hackathon. Nonetheless, the primary
files for HW3 are found in the src/main/java/ folder.
* DHT_server_cosmos.java
* CustomerEntity.java
* Client_Cosmos.java
* Main.java

## How to Run Code
``` 
mvn clean install
mvn package
mvn compile exec:java -Dexec.mainClass=Main
```

## Testing
The testing cases I implemented for HW2 was testing 10 customers buying 10 items, updating one, and checking out. I then reused
the code for my similar testing case in HW3, where I tested 10 customers buying 15 items, updating a few of them, and checking out.
I also ran my program for various values of C, particularly focusing on 1 to 3 concurrent customers. My second test focuses on testing
if clients can buy, update the same items, and list the cart correctly at the same time. By changing the booleans test1 and test2, one can
easily isolate testing. We can verify the Cosmos DB table handled concurrent customers will by examining the printouts.