import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class App extends JFrame {
    private JPanel usernamePanel, chatPanel;
    private JTextField usernameField, messageField;
    private JButton setUsernameButton, sendMessageButton;
    private JTextArea chatArea;

    private String username;
    private Connection connection;

    public App() {
        // Database connection setup
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatApp", "USERNAME",
                    "PASSWORD");
            createTableIfNotExists();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Dark theme setup
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("TextArea.background", Color.DARK_GRAY);
            UIManager.put("TextArea.foreground", Color.WHITE);
            UIManager.put("TextField.background", Color.DARK_GRAY);
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("Button.background", Color.GRAY);
            UIManager.put("Button.foreground", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Frame setup
        setTitle("JChat");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Username panel
        usernamePanel = new JPanel();
        usernameField = new JTextField(20);
        setUsernameButton = new JButton("Set Username");
        setUsernameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                showChatPanel();
            }
        });
        usernamePanel.add(new JLabel("Enter Username:"));
        usernamePanel.add(usernameField);
        usernamePanel.add(setUsernameButton);

        // Chat panel
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        messageField = new JTextField(20); // Initialize messageField
        sendMessageButton = new JButton("Send"); // Initialize sendMessageButton
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendMessageButton, BorderLayout.EAST);
        chatArea = new JTextArea(15, 30);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        chatPanel.setVisible(false);

        add(usernamePanel);
        add(chatPanel);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setVisible(true);

        // Periodically update the chat area
        new Thread(() -> {
            while (true) {
                try {
                    updateChatArea();
                    Thread.sleep(3000); // Update every 3 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void createTableIfNotExists() throws SQLException {
        Statement statement = connection.createStatement();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS messages (id INT AUTO_INCREMENT, username VARCHAR(50), message VARCHAR(255), PRIMARY KEY (id))";
        statement.executeUpdate(createTableQuery);
    }

    private void showChatPanel() {
        usernamePanel.setVisible(false);
        chatPanel.setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText();
        try {
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO messages (username, message) VALUES (?, ?)");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            messageField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateChatArea() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM messages");
            StringBuilder chatContent = new StringBuilder();
            while (resultSet.next()) {
                String savedUsername = resultSet.getString("username");
                String savedMessage = resultSet.getString("message");
                chatContent.append(savedUsername).append(": ").append(savedMessage).append("\n");
            }
            SwingUtilities.invokeLater(() -> chatArea.setText(chatContent.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());
    }
}
