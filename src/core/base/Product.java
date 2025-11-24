package core.base;

public class Product {
    private static int ID_COUNTER = 1;
    private final int id = ID_COUNTER++;
    private final String name;
    private final float basePrice;

    public Product(String name){
        this.name = name;
        this.basePrice = 1.0f;
    }

    public Product(String name, float basePrice){
        this.name = name;
        this.basePrice = basePrice;
    }


    public int getId(){ return id; }
    public String getName(){ return name; }
}
