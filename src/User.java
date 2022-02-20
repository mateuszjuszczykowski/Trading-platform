import exceptions.NotEnoughFundsException;
import exceptions.NotEnoughSharesException;
import exceptions.UnhandledOrderTypeException;
import exceptions.WrongShareNameException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class User implements Runnable{
    private final String name;
    private double funds;
    private HashMap<Asset, Integer> ownedAssets;
    // GUI elements
    JFrame userWindow;
    JLabel assetNameLabel;
    JLabel assetQuantityLabel;
    JLabel assetPriceLabel;
    JLabel userDataLabel;
    JTextField assetNameField;
    JTextField assetQuantityField;
    JTextField assetPriceField;
    JTextArea userDataArea;
    JScrollPane userDataScrollPane;
    JButton buyButton;
    JButton sellButton;


    public User(String name, double funds) {
        this.name = name;
        this.funds = funds;
        this.ownedAssets = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public double getFunds() {
        return funds;
    }

    public HashMap<Asset, Integer> getOwnedAssets() {
        return ownedAssets;
    }

    public void changeFunds(double diff) {
        funds += diff;
        updateInfo();
    }

    private void updateInfo() {
        userDataArea.setText("username: " + name + "\nbalance: " + funds + "\nowned assets:\n");
        for (Asset asset : ownedAssets.keySet()) {
            String assetStr = asset.getName() + "\t owned: " + ownedAssets.get(asset) + "\n";
            userDataArea.append(assetStr);
        }
    }

    public void updateOwnedAssets(Asset asset, int quantity) {
        for (Map.Entry<Asset, Integer> set: ownedAssets.entrySet()) {
            if(set.getKey().getName().equals(asset.getName())) {
                set.setValue(set.getValue() + quantity);
                if(set.getValue() <= 0)
                    ownedAssets.remove(set.getKey());
                updateInfo();
                return;
            }
        }
        ownedAssets.put(asset, quantity);
        updateInfo();
    }


    @Override
    public void run() {
        System.out.println("User " + name + " is now running on its own thread");
        setupGUI();
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder("User: " + name + "funds: " + funds + "\n");
        msg.append("owned assets: \n");
        for (Map.Entry<Asset, Integer> set: ownedAssets.entrySet()) {
            msg.append(set.getKey().getName()).append(": ").append(set.getValue());
        }
        return msg.toString();
    }

    private void setupGUI() {

        userWindow = new JFrame(name);
        assetNameField = new JTextField();
        assetQuantityField = new JTextField();
        assetPriceField = new JTextField();
        assetNameLabel = new JLabel("asset name:");
        assetQuantityLabel = new JLabel("quantity:");
        assetPriceLabel = new JLabel("price:");
        buyButton = new JButton("BUY");
        sellButton = new JButton("SELL");
        userDataLabel = new JLabel("user data:");
        userDataArea = new JTextArea();
        userDataScrollPane = new JScrollPane(userDataArea);
        userDataScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        userDataArea.setEditable(false);

        updateInfo();

        buyButton.addActionListener(e -> {
            try {
                Asset a = Exchange.instance.findAsset(assetNameField.getText());
                if (a == null) {
                    Exchange.instance.log("Asset with that name cannot be found.");
                    return;
                }
                Exchange.instance.startBuy(a,
                        Integer.parseInt(assetQuantityField.getText()),
                        Double.parseDouble(assetPriceField.getText()),
                        User.this);
            } catch (NotEnoughSharesException ex) {
                Exchange.instance.log(ex.getMessage());
            } catch (NumberFormatException ex) {
                System.out.println("order can't be placed, invalid number format");
            } catch (WrongShareNameException ex) {
                Exchange.instance.log("Wrong Share name");
            } catch (NotEnoughFundsException ex) {
                Exchange.instance.log("Not enough funds");
            }
        });
        sellButton.addActionListener(e -> {
            try {
                Asset a = Exchange.instance.findAsset(assetNameField.getText());
                if (a == null) {
                    Exchange.instance.log("Asset with that name cannot be found.");
                    return;
                }
                Exchange.instance.startSell(a,
                        Integer.parseInt(assetQuantityField.getText()),
                        Double.parseDouble(assetPriceField.getText()),
                        User.this);
            } catch (NotEnoughSharesException ex) {
                Exchange.instance.log("You tried to sell more shares than you have");
            } catch (NumberFormatException ex) {
                Exchange.instance.log("order can't be placed, invalid number format");
            } catch (WrongShareNameException ex) {
                Exchange.instance.log("Wrong share name");
            }
        });

        userWindow.add(userDataLabel);
        userWindow.add(userDataScrollPane);
        userWindow.add(assetNameLabel);
        userWindow.add(assetNameField);
        userWindow.add(assetQuantityLabel);
        userWindow.add(assetQuantityField);
        userWindow.add(assetPriceLabel);
        userWindow.add(assetPriceField);
        userWindow.add(buyButton);
        userWindow.add(sellButton);

        userWindow.setLayout(new GridLayout(5, 2));
        userWindow.setSize(600, 400);
        userWindow.setVisible(true);

    }
}
