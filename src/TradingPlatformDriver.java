import java.util.Random;

public class TradingPlatformDriver {
    public static void main(String[] args) {
        Asset a1 = new Asset("test");
        System.out.println(a1.getPrice());
        a1.increasePrice(230);
        System.out.println(a1.getPrice());
        a1.refreshPrice();
        System.out.println(a1.getPrice());
        Random rand = new Random();
        System.out.println(rand.nextDouble());


    }
}
