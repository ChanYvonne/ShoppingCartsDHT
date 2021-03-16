import com.microsoft.azure.storage.table.TableServiceEntity;

public class CustomerEntity extends TableServiceEntity {
    public CustomerEntity(String customerId, String shard) {
        this.partitionKey = shard;
        this.rowKey = customerId;
    }

    public CustomerEntity() { }

    byte[] shoppingCartValue;

    public byte[] getCart() {
        return this.shoppingCartValue;
    }

    public void setCart(byte[] newShoppingCart){
        this.shoppingCartValue = newShoppingCart;
    }
}