package models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

@Entity
public class Wallet {
    @Id
    private long id;

    @Unique
    private String address;

    private String key;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Wallet(long id, String address, String key) {
        this.id = id;
        this.address = address;
        this.key = key;
    }

    public Wallet() {}
}
