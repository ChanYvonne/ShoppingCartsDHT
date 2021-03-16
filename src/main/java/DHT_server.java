import java.util.Hashtable;
import javafx.util.Pair;

public class DHT_server {

    private Hashtable<String, VersionValuePair> shoppingCarts; //should be named customerShoppingCarts

    public DHT_server(){
        shoppingCarts = new Hashtable<String,  VersionValuePair>();
    }

    public int put(String key, byte[] value, Integer version) {
        if (value.length == 0) {
            shoppingCarts.remove(key);
            return -1;
        }
        if (shoppingCarts.get(key) != null) {
            if (shoppingCarts.get(key).getVersion() != (version - 1)) { //check to ensure new version is most up to date
                return -1;
            } else {
                VersionValuePair newValue = new VersionValuePair(value, version);
                if (!newValue.equals(shoppingCarts.get(key))) {
                    shoppingCarts.put(key, newValue);
                }
                return 0;
            }
        }else {
            VersionValuePair newValue = new VersionValuePair(value, 1);
            shoppingCarts.put(key, newValue);
            return 0;
        }


    }

    public Pair<byte[], Integer> get(String key){
        if (shoppingCarts.get(key) == null){
            return null;
        }else {
            return shoppingCarts.get(key).makeTuple();
        }
    }

    public void remove(String key) {
        shoppingCarts.remove(key);
    }

    private class VersionValuePair {
        private byte[] value;
        private Integer version;

        public VersionValuePair(byte[] x, Integer y) {
            this.value = x;
            this.version = y;
        }

        public Pair<byte[], Integer> makeTuple() {
            return new Pair<byte[], Integer>(value, version);
        }

        public Integer getVersion(){
            return version;
        }

        @Override
        public int hashCode() {
            int hashFirst = value != null ? value.hashCode() : 0;
            int hashSecond = version != null ? version.hashCode() : 0;

            return (hashFirst + hashSecond) * hashSecond + hashFirst;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VersionValuePair other = (VersionValuePair) obj;
            if (value != other.value)
                return false;
            if (version != other.version)
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "(" + value.toString() + ", " + version.toString() + ")";
        }
    }

}
