package core;

import java.time.LocalDate;
import java.util.*;

public class ProductEntry {
    private final int productID;
    private final LocalDate sellDate;
    private final List<Float> sellPrices;

    public ProductEntry(Product p, LocalDate d){
        this.sellDate = d;
        this.productID = p.getId();
        this.sellPrices = new ArrayList<>();
    }

    public ProductEntry(Product p, float sellPrice, LocalDate d){
        this.sellDate = d;
        this.productID = p.getId();
        this.sellPrices = new ArrayList<>();
        this.sellPrices.add(sellPrice);
    }

    public ProductEntry(ProductEntry pe){
        this.productID = pe.productID;
        this.sellDate = pe.getSellDate();
        this.sellPrices = new ArrayList<>(pe.getSellPrices());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntry that = (ProductEntry) o;
        return productID == that.productID && Objects.equals(sellDate, that.sellDate);
    }

    // TODO acabar isto
    protected ProductEntry clone() {
        return new ProductEntry(this);
    }

    public int getQuantitySold(){ return sellPrices.size(); }

    public int getProductID(){ return productID; }
    public LocalDate getSellDate(){ return sellDate; }

    public float getHighestPrice(){ return Collections.max(sellPrices); }

    public float getMedianPrice(){ return (float) (sellPrices.stream().mapToDouble(Float::doubleValue).sum() / getQuantitySold()); }
    public List<Float> getSellPrices(){ return sellPrices; }
    public void addSellPrice(float newPrice){ sellPrices.add(newPrice); }
}
