import static java.lang.Math.abs;

public class Order {
    private final OrderType type;
    private final int quantity;
    private final double price;
    private final double finalPrice;
    public User user;
    public Asset asset;

    public Order(OrderType type, Asset asset, int quantity, double price, User user) {
        this.type = type;
        this.asset = asset;
        this.quantity = abs(quantity);
        this.price = abs(price);
        this.finalPrice = quantity * price;
        this.user = user;
    }

    public OrderType getType() {
        return type;
    }

    public Asset getAsset() {
        return asset;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public User getUser() { return user; }

    public void updateUser(User src) {
        this.user = src;
    }

    @Override
    public String toString() {
        return "Order{" +
                "type=" + type +
                ", quantity=" + quantity +
                ", price=" + price +
                ", finalPrice=" + finalPrice +
                ", user=" + user.getName() +
                ", asset=" + asset.getName() +
                '}';
    }
}
