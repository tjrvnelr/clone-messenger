package com.javamessenger.android;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private EditText hostField;
    private EditText identifierField;
    private EditText passwordField;
    private TextView messageView;
    private boolean registerMode;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        showLogin();
    }

    private void showLogin() {
        registerMode = false;
        showAuthForm("Log in", "Login", "Don't have an account? Sign up");
    }

    private void showRegister() {
        registerMode = true;
        showAuthForm("Create account", "Register", "Already have an account? Log in");
    }

    private void showAuthForm(String title, String action, String alternate) {
        LinearLayout root = formRoot();
        TextView heading = label(title, 24);
        root.addView(heading);

        hostField = input("Server address", false);
        hostField.setText(getPreferences(MODE_PRIVATE).getString("server_host", "10.0.2.2"));
        root.addView(hostField);

        identifierField = input(registerMode ? "Username" : "Username or email", false);
        root.addView(identifierField);

        EditText emailField = null;
        if (registerMode) {
            emailField = input("Email", false);
            root.addView(emailField);
        }

        passwordField = input("Password", true);
        root.addView(passwordField);

        EditText confirmField = null;
        if (registerMode) {
            confirmField = input("Confirm password", true);
            root.addView(confirmField);
        }

        Button actionButton = new Button(this);
        actionButton.setText(action);
        root.addView(actionButton);

        messageView = label("", 14);
        messageView.setTextColor(0xffc62828);
        root.addView(messageView);

        Button alternateButton = new Button(this);
        alternateButton.setText(alternate);
        root.addView(alternateButton);

        EditText finalEmailField = emailField;
        EditText finalConfirmField = confirmField;
        actionButton.setOnClickListener(v -> {
            if (registerMode) {
                register(finalEmailField.getText().toString(), finalConfirmField.getText().toString());
            } else {
                login();
            }
        });
        alternateButton.setOnClickListener(v -> {
            if (registerMode) {
                showLogin();
            } else {
                showRegister();
            }
        });
        setContentView(root);
    }

    private void login() {
        String identifier = identifierField.getText().toString().trim();
        String password = passwordField.getText().toString();
        if (identifier.isEmpty() || password.isEmpty()) {
            messageView.setText("Enter your username/email and password.");
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("identifier", identifier);
            data.put("password", password);
            sendAuth("LOGIN", data);
        } catch (JSONException e) {
            messageView.setText("Could not prepare the login request.");
        }
    }

    private void register(String email, String confirm) {
        String username = identifierField.getText().toString().trim();
        String password = passwordField.getText().toString();
        if (username.isEmpty() || email.trim().isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageView.setText("Please fill in every field.");
            return;
        }
        if (!password.equals(confirm)) {
            messageView.setText("Passwords do not match.");
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("email", email.trim());
            data.put("password", password);
            sendAuth("REGISTER", data);
        } catch (JSONException e) {
            messageView.setText("Could not prepare the registration request.");
        }
    }

    private void sendAuth(String type, JSONObject data) {
        String host = hostField.getText().toString().trim();
        if (host.isEmpty()) {
            messageView.setText("Enter the computer's server address.");
            return;
        }
        getPreferences(MODE_PRIVATE).edit().putString("server_host", host).apply();
        messageView.setText("Connecting...");
        executor.execute(() -> {
            try {
                connect(host);
                JSONObject packet = new JSONObject();
                packet.put("type", type);
                packet.put("data", data);
                writer.println(packet);
                String response = reader.readLine();
                if (response == null) {
                    throw new IOException("The server closed the connection.");
                }
                JSONObject result = new JSONObject(response);
                runOnUiThread(() -> handleResponse(result));
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> messageView.setText(
                        "Can't reach the server. Check the address, firewall, and server."));
            }
        });
    }

    private void handleResponse(JSONObject packet) {
        String type = packet.optString("type");
        JSONObject data = packet.optJSONObject("data");
        if ("LOGIN_SUCCESS".equals(type)) {
            String username = data != null && data.optJSONObject("user") != null
                    ? data.optJSONObject("user").optString("username", "user") : "user";
            showMessenger(username);
        } else if ("REGISTER_SUCCESS".equals(type)) {
            Toast.makeText(this, "Account created. You can now log in.", Toast.LENGTH_LONG).show();
            showLogin();
        } else {
            messageView.setText(data == null ? "The server rejected the request."
                    : data.optString("message", "The server rejected the request."));
        }
    }

    private void connect(String host) throws IOException {
        closeSocket();
        socket = new Socket(host, 5555);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    private void showMessenger(String username) {
        LinearLayout root = formRoot();
        root.addView(label("Java Messenger", 26));
        root.addView(label("Signed in as " + username, 18));
        root.addView(label("Messaging features will use the same server connection.", 16));
        Button logout = new Button(this);
        logout.setText("Log out");
        root.addView(logout);
        logout.setOnClickListener(v -> {
            executor.execute(() -> {
                try {
                    if (writer != null) {
                        JSONObject packet = new JSONObject();
                        packet.put("type", "LOGOUT");
                        packet.put("data", new JSONObject());
                        writer.println(packet);
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(this,
                            "Could not send logout request.", Toast.LENGTH_SHORT).show());
                } finally {
                    closeSocket();
                    runOnUiThread(this::showLogin);
                }
            });
        });
        setContentView(root);
    }

    private LinearLayout formRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 48, 40, 24);
        return root;
    }

    private TextView label(String text, float size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setPadding(0, 10, 0, 10);
        return view;
    }

    private EditText input(String hint, boolean password) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setSingleLine(true);
        if (password) {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        field.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return field;
    }

    private void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        } finally {
            socket = null;
        }
    }

    @Override
    protected void onDestroy() {
        closeSocket();
        executor.shutdownNow();
        super.onDestroy();
    }
}
