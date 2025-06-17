package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import netscape.javascript.JSObject;
import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;

public class PassengerHomeController {

    // UI Components
    @FXML private VBox pickupContainer, destinationContainer;
    @FXML private TextField pickupField, destinationField;
    @FXML private ListView<String> pickupSuggestions, destinationSuggestions;
    @FXML private Label welcomeLabel, timeLabel, fareLabel;
    @FXML private WebView mapView;
    @FXML private Button requestRideButton, exitButton, minButton;
    @FXML private ImageView exitIcon, minIcon;
    @FXML private HBox titleBar;

    // Services
    private WebEngine webEngine;
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    
    // Autocomplete
    private ObservableList<String> pickupSuggestionsList = FXCollections.observableArrayList();
    private ObservableList<String> destinationSuggestionsList = FXCollections.observableArrayList();
    private final PauseTransition pause = new PauseTransition(Duration.millis(300));
    private String username;

    @FXML
    private void initialize() {
        setupMap();
        setupAutocomplete();
        setupControls();
    }

    private void setupMap() {
        // First ensure WebView is properly sized and configured
        mapView.setContextMenuEnabled(false); // Disable right-click menu
        webEngine = mapView.getEngine();
        
        // Enable debugging
        webEngine.setOnAlert(event -> System.out.println("WebView Alert: " + event.getData()));
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldExc, newExc) -> {
            if (newExc != null) {
                System.err.println("WebView Error: " + newExc.getMessage());
                showError("Map loading failed. Please check your internet connection.");
            }
        });
        
        // Enable JavaScript and set user agent
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        
        // Load the map content
        Platform.runLater(() -> {
            try {
                String mapHtml = getMapHTML();
                webEngine.loadContent(mapHtml);
                
                // Wait for page to load before setting up bridge
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            JSObject window = (JSObject) webEngine.executeScript("window");
                            window.setMember("javaApp", new JSBridge());
                            System.out.println("JavaScript bridge initialized successfully");
                        } catch (Exception e) {
                            System.err.println("Failed to initialize JS bridge: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load map content: " + e.getMessage());
            }
        });
    }

    private String getMapHTML() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <title>Ride Map</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
                integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
                crossorigin=""/>
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    height: 100%;
                    width: 100%;
                    overflow: hidden;
                }
                #map {
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    right: 0;
                }
                .user-location {
                    background: #4285F4;
                    border-radius: 50%;
                    width: 16px;
                    height: 16px;
                    border: 2px solid white;
                    box-shadow: 0 0 5px rgba(0,0,0,0.3);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
                integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
                crossorigin=""></script>
            <script>
                // Initialize map with default view
                var map = L.map('map', {
                    preferCanvas: true  // Better performance for JavaFX
                }).setView([14.5995, 120.9842], 13);
                
                // Add tile layer with proper attribution
                var osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                    maxZoom: 19,
                    noWrap: true
                }).addTo(map);
                
                // Debug tile loading
                osmLayer.on('tileload', function(e) {
                    console.log('Tile loaded: ' + e.tile.src);
                });
                
                osmLayer.on('tileerror', function(e) {
                    console.error('Tile error: ' + e.tile.src);
                });
                
                // Variables for markers
                var userMarker, startMarker, endMarker, routeLine;
                
                // Function to locate user
                function locateUser() {
                    if (navigator.geolocation) {
                        console.log("Attempting to get geolocation...");
                        navigator.geolocation.watchPosition(
                            function(position) {
                                console.log("Got position: ", position.coords);
                                var userLatLng = L.latLng(position.coords.latitude, position.coords.longitude);
                                updateUserLocation(userLatLng);
                            },
                            function(error) {
                                console.error("Geolocation error:", error);
                            },
                            {
                                enableHighAccuracy: true,
                                maximumAge: 10000,
                                timeout: 5000
                            }
                        );
                    } else {
                        console.log("Geolocation is not supported by this browser.");
                    }
                }
                
                function updateUserLocation(latLng) {
                    if (userMarker) {
                        userMarker.setLatLng(latLng);
                    } else {
                        userMarker = L.marker(latLng, {
                            icon: L.divIcon({
                                className: 'user-location',
                                iconSize: [20, 20]
                            })
                        }).addTo(map).bindPopup("You are here");
                    }
                    
                    if (window.javaApp) {
                        window.javaApp.setPickupText("Current Location");
                    }
                }
                
                // Function to show route between points
                window.showRoute = function(startCoords, endCoords) {
                    console.log("Showing route from", startCoords, "to", endCoords);
                    
                    // Clear existing markers and route
                    if (startMarker) map.removeLayer(startMarker);
                    if (endMarker) map.removeLayer(endMarker);
                    if (routeLine) map.removeLayer(routeLine);
                    
                    // Parse coordinates
                    var startLatLng = L.latLng(startCoords[0], startCoords[1]);
                    var endLatLng = L.latLng(endCoords[0], endCoords[1]);
                    
                    // Add markers
                    startMarker = L.marker(startLatLng).addTo(map).bindPopup("Pickup Location");
                    endMarker = L.marker(endLatLng).addTo(map).bindPopup("Destination");
                    
                    // Add route line
                    routeLine = L.polyline([startLatLng, endLatLng], {
                        color: 'blue',
                        weight: 3,
                        opacity: 0.7,
                        smoothFactor: 1
                    }).addTo(map);
                    
                    // Fit bounds to show all points
                    var bounds = L.latLngBounds([startLatLng, endLatLng]);
                    if (userMarker) {
                        bounds.extend(userMarker.getLatLng());
                    }
                    map.fitBounds(bounds, {padding: [50, 50]});
                    
                    // Calculate estimates
                    var distance = startLatLng.distanceTo(endLatLng);
                    var timeEstimate = Math.max(5, Math.min(120, Math.round(distance / 500))); // 500 meters per minute
                    var fareEstimate = Math.max(40, Math.round(distance / 200)); // ~₱1 per 200 meters
                    
                    // Update JavaFX UI
                    if (window.javaApp && window.javaApp.updateRouteInfo) {
                        window.javaApp.updateRouteInfo(
                            "~" + timeEstimate + " mins",
                            "₱" + fareEstimate
                        );
                    }
                };
                
                // Initialize
                locateUser();
                
                // Signal to Java that map is ready
                if (window.javaApp && window.javaApp.mapReady) {
                    window.javaApp.mapReady();
                }
                
                console.log("Map initialization complete");
            </script>
        </body>
        </html>
        """;
    }

    private void setupAutocomplete() {
        // Bind suggestions to ListViews
        pickupSuggestions.setItems(pickupSuggestionsList);
        destinationSuggestions.setItems(destinationSuggestionsList);
        
        // Configure pickup field
        pickupField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 2) {
                pause.setOnFinished(event -> fetchSuggestions(newVal, true));
                pause.playFromStart();
            } else {
                clearSuggestions(true);
            }
        });

        // Configure destination field
        destinationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 2) {
                pause.setOnFinished(event -> fetchSuggestions(newVal, false));
                pause.playFromStart();
            } else {
                clearSuggestions(false);
            }
        });

        // Set selection handlers
        pickupSuggestions.setOnMouseClicked(e -> selectSuggestion(true));
        destinationSuggestions.setOnMouseClicked(e -> selectSuggestion(false));

        // Hide suggestions when focus lost
        pickupField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) hideSuggestions(true);
        });
        destinationField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) hideSuggestions(false);
        });
    }

    private void setupControls() {
        requestRideButton.setOnAction(e -> handleRideRequest());
        
        try {
            exitIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/x.png")));
            minIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/-.png")));
        } catch (Exception e) {
            System.err.println("Error loading window controls: " + e.getMessage());
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            WindowControls.enableDrag(stage, titleBar);
        });
    }

    private void fetchSuggestions(String query, boolean isPickup) {
        new Thread(() -> {
            try {
                HttpUrl url = HttpUrl.get(NOMINATIM_URL).newBuilder()
                    .addQueryParameter("q", query)
                    .addQueryParameter("format", "json")
                    .addQueryParameter("limit", "5")
                    .build();

                Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "JavaFXAppExample")
                    .build();

                try (Response resp = httpClient.newCall(req).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        JsonArray results = JsonParser.parseString(resp.body().string()).getAsJsonArray();
                        updateSuggestions(results, isPickup);
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Address lookup service unavailable"));
            }
        }).start();
    }

    private void updateSuggestions(JsonArray results, boolean isPickup) {
        ObservableList<String> suggestions = FXCollections.observableArrayList();
        for (JsonElement element : results) {
            suggestions.add(element.getAsJsonObject().get("display_name").getAsString());
        }

        Platform.runLater(() -> {
            if (isPickup) {
                pickupSuggestionsList.setAll(suggestions);
                showSuggestions(true);
            } else {
                destinationSuggestionsList.setAll(suggestions);
                showSuggestions(false);
            }
        });
    }

    private void selectSuggestion(boolean isPickup) {
        ListView<String> suggestions = isPickup ? pickupSuggestions : destinationSuggestions;
        TextField field = isPickup ? pickupField : destinationField;
        
        String selected = suggestions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            field.setText(selected);
            clearSuggestions(isPickup);
            field.requestFocus();
        }
    }

    private void showSuggestions(boolean isPickup) {
        ListView<String> suggestions = isPickup ? pickupSuggestions : destinationSuggestions;
        if (!suggestions.getItems().isEmpty()) {
            suggestions.setVisible(true);
            suggestions.setManaged(true);
            suggestions.setPrefHeight(Math.min(150, suggestions.getItems().size() * 30));
        }
    }

    private void hideSuggestions(boolean isPickup) {
        ListView<String> suggestions = isPickup ? pickupSuggestions : destinationSuggestions;
        suggestions.setVisible(false);
        suggestions.setManaged(false);
        suggestions.setPrefHeight(0);
    }

    private void clearSuggestions(boolean isPickup) {
        if (isPickup) {
            pickupSuggestionsList.clear();
        } else {
            destinationSuggestionsList.clear();
        }
        hideSuggestions(isPickup);
    }

    public void handleRideRequest() {
        String from = pickupField.getText().trim();
        String to = destinationField.getText().trim();
        
        if (from.isEmpty() || to.isEmpty()) {
            showError("Please enter both locations");
            return;
        }
        
        requestRideButton.setDisable(true);
        new Thread(() -> {
            try {
                String[] fromCoords = geocode(from);
                String[] toCoords = geocode(to);
                
                if (fromCoords == null || toCoords == null) {
                    Platform.runLater(() -> showError("Could not locate addresses"));
                    return;
                }
                
                Platform.runLater(() -> {
                    webEngine.executeScript(String.format(
                        "showRoute(['%s','%s'], ['%s','%s'])", 
                        fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]
                    ));
                });
            } finally {
                Platform.runLater(() -> requestRideButton.setDisable(false));
            }
        }).start();
    }

    private String[] geocode(String address) {
        try {
            HttpUrl url = HttpUrl.get(NOMINATIM_URL).newBuilder()
                .addQueryParameter("q", address)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1")
                .build();

            Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", "JavaFXAppExample")
                .build();

            try (Response resp = httpClient.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JsonArray results = JsonParser.parseString(resp.body().string()).getAsJsonArray();
                    if (results.size() > 0) {
                        JsonObject location = results.get(0).getAsJsonObject();
                        return new String[]{
                            location.get("lat").getAsString(),
                            location.get("lon").getAsString()
                        };
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Geocoding error: " + e.getMessage());
        }
        return null;
    }

    public class JSBridge {
        public void updateRouteInfo(String time, String fare) {
            Platform.runLater(() -> {
                timeLabel.setText("Time: " + time);
                fareLabel.setText("Fare: " + fare);
            });
        }
        
        public void setPickupText(String text) {
            Platform.runLater(() -> pickupField.setText(text));
        }
        
        public void mapReady() {
            Platform.runLater(() -> {
                System.out.println("Map is fully loaded and ready");
            });
        }
    }

    public void setUsername(String username) {
        this.username = username;
        Platform.runLater(() -> welcomeLabel.setText("Welcome, " + username + "!"));
    }

    @FXML
    private void handleExit() {
        WindowControls.exit();
    }

    @FXML
    private void handleMinimize() {
        WindowControls.minimize((Stage) minButton.getScene().getWindow());
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setHeaderText(null);
            alert.show();
        });
    }
}