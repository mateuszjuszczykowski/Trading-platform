import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Asset {
    private final String name;
    private double price;
    private int avaiableQuantity;

    private final double maxPrice = 10000;
    private final double minPrice = 1;

    public Asset(String name) {
        this.name = name;

        Random rand = new Random();
        double minStartPrice = 1;
        double maxStartPrice = 1000;
        this.price = round(minStartPrice + (maxStartPrice - minStartPrice) * rand.nextDouble());

        int minSharesNum = 1000;
        int maxSharesNum = 5000;
        this.avaiableQuantity = rand.nextInt( maxSharesNum - minSharesNum) + minSharesNum;


    }

    @Override
    public String toString() {
        return  "name='" + name + '\'' +
                ", price=" + price +
                ", avaiableQuantity=" + avaiableQuantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getAvailableQuantity() {
        return avaiableQuantity;
    }

    public void increasePrice(int noSold) {
        double newPrice = round(price + (double)(noSold/avaiableQuantity) * price);
        if(checkPrice(newPrice))
            price = newPrice;

    }

    public void decreasePrice(int noBought) {
        double newPrice = round(price - (double)(noBought/avaiableQuantity) * price);
        if(checkPrice(newPrice))
            price = newPrice;
    }

    public void changeQuantity(int diff) {
        this.avaiableQuantity += diff;
    }

    @Contract(pure = true)
    private @NotNull
    Boolean checkPrice(double newPrice) {
        // blocks price from falling or skyrocketing
        // no exception here, as it's user-independent
        return (newPrice > minPrice && newPrice < maxPrice);
    }

    private double round(double number) {
        return (double)Math.round(number * 100)/100;
    }

    public void refreshPrice() {
        if(checkPrice(price)) {
            Random rand = new Random();
            int[] arr = new int[]{-5, -4, -3, -2, 2, 3, 4, 5};
            int change = arr[rand.nextInt(arr.length)];
            double newPrice = round(price + change);
            if(checkPrice(newPrice))
                price = newPrice;
        }
    }

}
