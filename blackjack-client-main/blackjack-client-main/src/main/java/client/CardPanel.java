package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;

public class CardPanel extends JPanel 
{
    private static final long serialVersionUID = 1L;

    private JButton hitButton;
    private JButton standButton; 
    private JLabel balanceLabel;
    private JLabel valueLabel;

    private List<Card> dealerCards = new ArrayList<>();
    private List<Card> playerCards = new ArrayList<>();
    private Map<Card, ImageIcon> cardImages;
    //private Random random;

    public CardPanel(JButton hitButton, JButton standButton, JLabel balanceLabel, JLabel valueLabel, Map<Card, ImageIcon> cardImages)
    {
        this.hitButton = hitButton;
        this.standButton = standButton;
        this.cardImages = cardImages;
        this.balanceLabel = balanceLabel;
        this.valueLabel = valueLabel;

        // null layout manager is absolute positioning
        setLayout(null);
        setBackground(Color.GREEN.darker());

        //loadCards();

        // add a hit and stand button
        // the actual click handler is defined in the BlackjackGUI class
        hitButton.setBounds(350, 540, 100, 60);
        add(hitButton);
        
        standButton.setBounds(550, 540, 100, 60);
        add(standButton);
        
        balanceLabel.setBounds(200, 20, 200, 80);
        add(balanceLabel);

        valueLabel.setBounds(700, 20, 200, 80);
        add(valueLabel);

        hitButton.setVisible(false);
        standButton.setVisible(false);
        balanceLabel.setVisible(false);
        valueLabel.setVisible(false);
    }

    public void clearCards() {
        dealerCards.clear();
        playerCards.clear();
    }

    public void addDealerCard(Card card) {
        dealerCards.add(card);
    }

    public void addPlayerCard(Card card) {
        playerCards.add(card);
    }

    public void hideButtonsandLabels() {
        hitButton.setVisible(false);
        standButton.setVisible(false);
        balanceLabel.setVisible(false);
        valueLabel.setVisible(false);
    }

    public void showButtonsandLabels() {
        hitButton.setVisible(true);
        standButton.setVisible(true);
        balanceLabel.setVisible(true);
        valueLabel.setVisible(true);
    }

    public void DisableHitAndStandButtons() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
    }

    public void EnableHitAndStandButtons() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
    }

    public void resetPlayerValue() {
        valueLabel.setText("Player's value: ");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 100;
        int y = 100;
        // dealer cards        
        if (dealerCards.size() == 1) {
            // Draw the first card
            Card card = dealerCards.get(0);
            ImageIcon cardImage = cardImages.get(card);
            if (cardImage != null) {
                System.out.println("Drawing dealer card: " + card);
                g.drawImage(cardImage.getImage(), x, y, null);
                x += cardImage.getIconWidth() + 10; 
            }
            // Face down the second card
            ImageIcon backCard = new ImageIcon(getClass().getResource("/assets/" + "Back.png"));
            g.drawImage(backCard.getImage(), x, y, null);
            x += backCard.getIconWidth() + 10;
        } else {
            // Flip the hidden card and show all dealder's card
            x = 100;
            y = 100;
            for (Card card : dealerCards) {
                ImageIcon cardImage = cardImages.get(card);
                if (cardImage != null) {
                    System.out.println("Drawing dealer card: " + card);
                    g.drawImage(cardImage.getImage(), x, y, null);
                    x += cardImage.getIconWidth() + 10; 
                }
            }
        }

        // player cards
        x = 100; 
        y = 400;
        for (Card card : playerCards) {
            System.out.println("Drawing player card: " + card);
            // Draw player cards
            ImageIcon cardImage = cardImages.get(card);
            if (cardImage != null) {
                g.drawImage(cardImage.getImage(), x, y, null);
                x += cardImage.getIconWidth() + 10; 
            }
        }
    }        
    
}
