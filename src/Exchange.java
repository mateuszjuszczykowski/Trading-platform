import exceptions.NotEnoughFundsException;
import exceptions.NotEnoughSharesException;
import exceptions.WrongShareNameException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.Scanner;

public class Exchange {
    //try singleton approach
    public static Exchange instance;
    //check order list
    //refresh prices
    public ArrayList<Asset> assets;
    public ArrayList<Order> orders;
    private final String FILENAME = "assets.txt";
    private Boolean isRunning = true;

    // GUI elements
    private final int PADDING = 50;
    private final int WIDTH = 1200;
    private final int HEIGHT = 600;

    private JFrame exchangeWindow;
    private JTextArea assetsArea;
    private JScrollPane assetScrollPane;
    private JTextArea ordersArea;
    private JScrollPane ordersScrollPane;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JButton launchUserButton;
    private JTextField userFundsField;
    private JLabel userFundsLabel;

    public Exchange() {
        instance = this;
        assets = new ArrayList<Asset>();
        orders = new ArrayList<Order>();
    }

    public static void main(String[] args) {
        Exchange exchange = new Exchange();
        exchange.start();

        Asset as = new Asset("test");
        System.out.println(as);
    }

    public Asset findAsset(String name) throws WrongShareNameException {
        for (Asset as: assets) {
            if (as.getName().equals(name))
                return as;
        }
        return null;
        //throw new WrongShareNameException("That asset name does not exist");
    }

    public void start() {
        loadAssets();
        setupGUI();


        while (isRunning) {
            try {
                printAssets();
                printOrders();
                checkOrders();
                refreshPrices();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                isRunning = false;
            }
        }
    }

    private void printAssets() {
        String tempMsg = "Assets: \n";
        for (Asset asset: assets)
            tempMsg += asset.toString() + "\n";
        assetsArea.setText(tempMsg);
    }

    public void log(String msg) {
        System.out.println(msg + "\n");
    }

    private void printOrders() {
        String tempMsg = "Orders:\n";
        for (Order order : orders) {
            tempMsg += order.toString() + "\n";
        }
        ordersArea.setText(tempMsg);
    }

    private void checkOrders() {
        try {
            for (Order order: orders) {
                switch (order.getType()) {
                    case BUY:
                        if (order.getPrice() >= order.asset.getPrice() &&
                                order.getQuantity() <= order.asset.getAvailableQuantity()) {
                            endBuy(order);
                        }
                        break;
                    case SELL:
                        if (order.getPrice() <= order.asset.getPrice())
                            endSell(order);
                        break;
                    default:
                        break;
                }
            }
        }catch (ConcurrentModificationException e) {
            Exchange.instance.log("No more orders to execute for now");
        }
    }

    public void startBuy(Asset asset, int quantity, double price, User user)
            throws NotEnoughSharesException, NotEnoughFundsException {
        // creates order
        if(quantity > asset.getAvailableQuantity())
            throw new NotEnoughSharesException("User requested to many shared to buy");
        if(price*quantity > user.getFunds()) {
            throw new NotEnoughFundsException("User doesn't have enough money");
        }
        Exchange.instance.orders.add(new Order(OrderType.BUY, asset, quantity, price, user));
    }

    public void startSell(Asset asset, int quantity, double price, User user)
            throws NotEnoughSharesException {
        if(quantity > user.getOwnedAssets().get(asset))
            throw new NotEnoughSharesException("User doesn't have enough shares to sell");
        Exchange.instance.orders.add(new Order(OrderType.SELL, asset, quantity, price, user));
    }

    public void endBuy(Order order) {
        if(order.user.getFunds() - order.getFinalPrice() >= 0 &&
            order.asset.getAvailableQuantity() >= order.getQuantity())
        {
            order.user.changeFunds(-order.getFinalPrice());
            order.user.updateOwnedAssets(order.asset, order.getQuantity());
            order.asset.increasePrice(order.getQuantity());
            order.asset.changeQuantity(-order.getQuantity());
            Exchange.instance.orders.remove(order);
            Exchange.instance.log("Successfully bought: " + order.toString());
        }
    }

    public void endSell(Order order) {
        Asset tempAsset = order.getAsset();
        if(order.user.getOwnedAssets().get(tempAsset) >= order.getQuantity()) {
            order.user.changeFunds(order.getFinalPrice());
            order.user.updateOwnedAssets(tempAsset, -order.getQuantity());
            order.asset.decreasePrice(order.getQuantity());
            order.asset.changeQuantity(order.getQuantity());
            Exchange.instance.orders.remove(order);
            Exchange.instance.log("Successfully sold: " + order.toString());
        }
    }

    private void loadAssets() {
        Scanner sc;
        Random rand = new Random();
        File src = new File(FILENAME);
        ArrayList<Asset> loadedAssets = new ArrayList<>();
        int MAX_SHARES_NUM = rand.nextInt(10-5) + 5; //(max - min) + min
        try {
            sc = new Scanner(src);
            while(sc.hasNextLine()) {
                loadedAssets.add(new Asset(sc.nextLine().trim()));
            }

            for (int i = 0; i < 15 -MAX_SHARES_NUM; i++)
                loadedAssets.remove(rand.nextInt(loadedAssets.size()));

            assets = loadedAssets;

        } catch(FileNotFoundException e) {
            System.out.println("file doesn't exist");
        }
    }

    private void refreshPrices() {
        for (Asset asset : assets)
            asset.refreshPrice();
    }

    private void setupGUI() {

        exchangeWindow = new JFrame("Exchange");

        //assets
        assetsArea = new JTextArea("");
        assetScrollPane = new JScrollPane(assetsArea);
        assetScrollPane.setBounds(PADDING, PADDING, (WIDTH - 2 * PADDING) / 2, (HEIGHT - 2 * PADDING));
        assetsArea.setEditable(false);
        exchangeWindow.add(assetScrollPane);

        //orders
        ordersArea = new JTextArea("");
        ordersArea.setEditable(false);
        ordersScrollPane = new JScrollPane(ordersArea);
        ordersScrollPane.setBounds(
                PADDING + ((WIDTH - 2 * PADDING) / 2),
                PADDING,
                (WIDTH - 2 * PADDING) / 2,
                (HEIGHT - 2 * PADDING));
        exchangeWindow.add(ordersScrollPane);

        // USER LOGIN AREA
        int tab = PADDING/2;
        usernameLabel = new JLabel("username:");
        usernameLabel.setBounds(PADDING / 2, 0, 100, 50);
        exchangeWindow.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(PADDING / 2 + 100, 25 / 2, 100, 25);
        exchangeWindow.add(usernameField);

        userFundsLabel = new JLabel("funds:");
        userFundsLabel.setBounds(PADDING / 2 + 250, 0, 100, 50);
        exchangeWindow.add(userFundsLabel);

        userFundsField = new JTextField();
        userFundsField.setBounds(PADDING / 2 + 350, 25 / 2, 100, 25);
        exchangeWindow.add(userFundsField);

        launchUserButton = new JButton("Login");
        launchUserButton.setBounds(PADDING / 2 + 500, 25 / 2, 100, 25);
        launchUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                log("Launching user with username " + usernameField.getText() + "\n"); //todo fight with incorrect input

                try {
                    Thread t = new Thread(new User(usernameField.getText(), Double.parseDouble(userFundsField.getText().trim())));
                    t.start();
                }catch (NumberFormatException e) {
                    log("User input invalid: it's not a number!");
                }
            }
        });
        exchangeWindow.add(launchUserButton);

        // FINAL WINDOW SETUP
        exchangeWindow.setSize(WIDTH, HEIGHT);
        exchangeWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        exchangeWindow.setLayout(null);
        exchangeWindow.setVisible(true);
    }

}
