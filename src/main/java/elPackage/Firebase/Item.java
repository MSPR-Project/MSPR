package elPackage.Firebase;

public class Item {

    public String id;
    public String available;
    public String idOwner;


    public Item(){

    }

    public Item(String id, String available, String idOwner){

        this.id = id;
        this.available = available;
        this.idOwner = idOwner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(String idOwner) {
        this.idOwner = idOwner;
    }
}
