package core;

import java.util.Date;

public class Product {
    private static int ID_COUNTER = 1;
    private final int id = ID_COUNTER++;
    private final String name;
    private float basePrice = 0.0f;

    public Product(String name){
        this.name = name;
    }


    public int getId(){ return id; }
    public String getName(){ return name; }

    public float getPrice(){ return basePrice; }
    public void setPrice(float price){ this.basePrice = price; }
}
