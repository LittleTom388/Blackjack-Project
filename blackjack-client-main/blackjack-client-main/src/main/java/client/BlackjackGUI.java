package client;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class BlackjackGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private JButton hitButton;
    private JButton standButton;
    private JLabel balanceLabel;
    private JLabel valueLabel;

    private String BASE_URL = "http://euclid.knox.edu:8080/api/blackjack";
    private String USERNAME = "npham";
    private String PASSWORD = "7645dbd";
    private ClientConnecter clientConnecter;

    private CardPanel cardPanel;
    private Map<Card, ImageIcon> cardImages;
    private UUID sessionId;
    private GameState state;
    private boolean hasReshuffled;
    private Map<String, SessionSummary> sessionMap;

    public BlackjackGUI() {
        setTitle("Blackjack Game");
        setSize(1000, 800);
        loadCards();
        // create and pass the buttons to the card panel
        // it will resize them and add them to the panel

        // Hit and Stand buttons
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");

        // Balance label
        balanceLabel = new JLabel("Balance: ");
        valueLabel = new JLabel("Player's value: ");
        cardPanel = new CardPanel(hitButton, standButton, balanceLabel, valueLabel, cardImages);
        setContentPane(cardPanel);
        hasReshuffled = false;
        addMenuBar();
        // now set the action listeners for the hit/stand buttons
        hitButton.addActionListener(e -> {
            try {
                // Hit the button
                if (state != null && !state.gameOver) {
                    state = clientConnecter.hit(state.sessionId);
                    System.out.println(state.reshuffled);
                    if (state.reshuffled) {
                        hasReshuffled = true;
                    }
                    String cardName = state.playerCards.get(state.playerCards.size() - 1);
                    JOptionPane.showMessageDialog(this, "Drawing " + cardName, "GAME MESSAGE",
                            JOptionPane.INFORMATION_MESSAGE);
                    // Show if cards are shuffled
                    if (hasReshuffled) {
                        JOptionPane.showMessageDialog(this,
                                "Cards reshuffled!" + state.cardsRemaining, "GAME MESSAGE",
                                JOptionPane.INFORMATION_MESSAGE);
                        hasReshuffled = false;
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Cards not reshuffled! --- Cards remaining: " + state.cardsRemaining, "GAME MESSAGE",
                                JOptionPane.INFORMATION_MESSAGE);
                        valueLabel.setText("Player's value: " + state.playerValue);
                        cardPanel.addPlayerCard(getCard(cardName));
                        repaint();
                    }
                }
                if (state.gameOver) {
                    cardPanel.DisableHitAndStandButtons();
                    for (int i = 1; i < state.dealerCards.size(); i++) {
                        String cardName2 = state.dealerCards.get(i);
                        cardPanel.addDealerCard(getCard(cardName2));
                    }
                    repaint();
                    // Show the outcome and ask if the player wants to play a new game
                    showGameOutcomeAndAskPlayer();
                }
            } catch (Exception ex) {

            }
        });
        standButton.addActionListener(e -> {
            try {
                state = clientConnecter.stand(state.sessionId);
                cardPanel.DisableHitAndStandButtons();
                if (state.reshuffled) {
                    hasReshuffled = true;
                }
                // Show if cards are shuffled
                if (hasReshuffled) {
                    JOptionPane.showMessageDialog(this,
                            "Cards reshuffled! --- Cards remaining: " + state.cardsRemaining, "GAME MESSAGE",
                            JOptionPane.INFORMATION_MESSAGE);
                    hasReshuffled = false;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Cards not reshuffled! --- Cards remaining: " + state.cardsRemaining, "GAME MESSAGE",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                for (int i = 1; i < state.dealerCards.size(); i++) {
                    String cardName = state.dealerCards.get(i);
                    cardPanel.addDealerCard(getCard(cardName));
                }
                repaint();
                showGameOutcomeAndAskPlayer();
            } catch (Exception ex) {

            }
        });

        // client connecter to make API calls on the server
        clientConnecter = new ClientConnecter(BASE_URL, USERNAME, PASSWORD);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");

        menuBar.add(fileMenu);
        addMenuItem(fileMenu, "Reconnect", () -> {
            System.out.println("Load clicked");
            try {
                int num = 1;
                List<SessionSummary> sessionSummaryList = clientConnecter.listSessions();
                sessionMap = new HashMap<>();
                List<String> sessionName = new ArrayList<>();
                for (SessionSummary session : sessionSummaryList) {
                    String s = num++ + ". Session ID: " + session.sessionId + ", Balance: " + session.balance;
                    sessionName.add(s);
                    sessionMap.putIfAbsent(s, session);
                }
                showListPopup("Choose Session", sessionName);
                cardPanel.hideButtonsandLabels();
                startNewGame();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        addMenuItem(fileMenu, "New Game", () -> {
            System.out.println("New Game clicked");
            try {
                state = clientConnecter.startGame();
                JOptionPane.showMessageDialog(this, "      A new Blackjack game has started!", "GAME MESSAGE",
                        JOptionPane.PLAIN_MESSAGE);
                cardPanel.hideButtonsandLabels();
                startNewGame();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error starting new game: " + e.getMessage(), "GAME MESSAGE",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    private void startNewGame() {
        // Clear cards
        cardPanel.clearCards();
        cardPanel.resetPlayerValue();
        balanceLabel.setText("Balance: " + state.balance);
        repaint();

        // Enter player's bet:
        int bet = 0;
        boolean isValidBet = false;
        while (!isValidBet) {
            String betString = JOptionPane.showInputDialog(this,
                    "Enter bet (must be multiple of 10): ", "Place Bet",
                    JOptionPane.QUESTION_MESSAGE);
            try {
                bet = Integer.parseInt(betString);
                if (bet > 0 && bet % 10 == 0) {
                    isValidBet = true;
                } else {
                    JOptionPane.showMessageDialog(this, "Your bet amount is not valid, try again !",
                            "Invalid bet", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Your bet amount is not valid, try again !",
                        "Invalid bet", JOptionPane.ERROR_MESSAGE);
            }

        }
        try {
            state = clientConnecter.placeBet(state.sessionId, bet);
        } catch (Exception e) {

        }

        // Show and enable buttons and labels
        valueLabel.setText("Player's value: " + state.playerValue);
        cardPanel.showButtonsandLabels();
        cardPanel.EnableHitAndStandButtons();

        // Deal the cards:
        // Player's cards:
        for (String cardName : state.playerCards) {
            cardPanel.addPlayerCard(getCard(cardName));
        }
        // Dealer's cards:
        cardPanel.addDealerCard(getCard(state.dealerCards.get(0)));
        repaint();

        // Check if player has blackjack
        if (state.gameOver && state.dealerValue < 21) {
            showGameOutcomeAndAskPlayer();
        }
    }

    private void clearCardPanel() {
        cardPanel.hideButtonsandLabels();
        cardPanel.clearCards();
        repaint();
    }

    private void showGameOutcomeAndAskPlayer() {
        try {
            JOptionPane.showMessageDialog(this, state.outcome + " (Dealer's value is " + state.dealerValue + ")",
                    "GAME MESSAGE", JOptionPane.INFORMATION_MESSAGE);
            int choice = JOptionPane.showConfirmDialog(this,
                    "(CURRENT BALANCE: "+state.balance +")"+" DO YOU WANT TO PLAY AGAIN ?", "GAME MESSAGE",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                state = clientConnecter.newGame(state.sessionId);
                startNewGame();
            } else {
                JOptionPane.showMessageDialog(this, "THANK YOU FOR PLAYING! ", "GAME MESSAGE",
                        JOptionPane.INFORMATION_MESSAGE);
                clearCardPanel();
                clientConnecter.finishGame(state.sessionId);
            }
        } catch (Exception e) {

        }
    }

    // convert "THREE OF HEARTS" from server to Card.THREE_OF_HEARTS
    private Card getCard(String cardName) {
        return Card.valueOf(cardName.toUpperCase().replace(' ', '_'));
    }

    private void addMenuItem(JMenu menu, String name, Runnable action) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(e -> action.run());
        menu.add(menuItem);
    }

    private void loadCards() {
        // Load card images and add them to the main panel
        // This is where you would implement the logic to load and display cards
        cardImages = new HashMap<>();
        for (Card card : Card.values()) {
            ImageIcon cardImage = new ImageIcon(getClass().getResource("/assets/" + card.getFilename()));
            cardImages.put(card, cardImage);
        }
    }

    public void showListPopup(String title, java.util.List<String> items) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JList<String> list = new JList<>(new DefaultListModel<>());
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
        for (String item : items) {
            model.addElement(item);
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // double click to select
                    String selected = list.getSelectedValue();
                    System.out.println("Selected: " + selected);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(BlackjackGUI.this, "Selected: " + selected, "Connect successfully",
                            JOptionPane.INFORMATION_MESSAGE);
                    SessionSummary session = sessionMap.get(selected);
                    System.out.println(session.sessionId);
                    try {
                        state = clientConnecter.resumeSession(session.sessionId);
                    } catch (Exception ex) {

                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        BlackjackGUI gui = new BlackjackGUI();
        gui.setVisible(true);
    }

}
