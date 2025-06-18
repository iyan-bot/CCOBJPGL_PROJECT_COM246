package com.example;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class HomeController {
    // UI Elements
    @FXML private ImageView currentSongArt;
    @FXML private Label currentSongTitle;
    @FXML private Label currentSongArtist;
    @FXML private Slider progressSlider;
    @FXML private Button playButton;
    @FXML private ImageView playPauseIcon;
    
    // Media Player
    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;
    private boolean isPlaying = false;
    
    // Images
    private Image playImage;
    private Image pauseImage;
    private Image backImage;
    private Image skipImage;
    private Image defaultAlbumArt;

    @FXML
    public void initialize() {
        try {
            // Load images
            playImage = new Image(getClass().getResource("play.png").toString());
            pauseImage = new Image(getClass().getResource("pause.png").toString());
            backImage = new Image(getClass().getResource("back.png").toString());
            skipImage = new Image(getClass().getResource("skip.png").toString());
            defaultAlbumArt = new Image(getClass().getResource("weeknd.jpg").toString());
            
            // Setup player
            setupPlayerControls();
            initializePlaylist();
        } catch (Exception e) {
            System.err.println("Initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPlayerControls() {
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });
    }

    private void initializePlaylist() {
        // Add your MP3 file (wsong1.mp3 should be in src/main/resources/music/)
        URL songUrl = getClass().getResource("wsong1.mp3");
        if (songUrl != null) {
            playlist.add(new Song("Your Song", "Artist", defaultAlbumArt, songUrl.toString()));
            updateNowPlayingUI(playlist.get(0)); // Initialize with first song
        } else {
            System.err.println("Error: Could not find wsong1.mp3 in resources/music/");
        }
    }

    private void updateNowPlayingUI(Song song) {
        currentSongArt.setImage(song.getAlbumArt());
        currentSongTitle.setText(song.getTitle());
        currentSongArtist.setText(song.getArtist());
    }

    // Player Actions
    @FXML
    private void togglePlay() {
        if (playlist.isEmpty()) return;
        
        if (mediaPlayer == null) {
            playSong(currentTrackIndex);
            return;
        }
        
        if (isPlaying) {
            mediaPlayer.pause();
            playPauseIcon.setImage(playImage);
        } else {
            mediaPlayer.play();
            playPauseIcon.setImage(pauseImage);
        }
        isPlaying = !isPlaying;
    }

    @FXML
    private void nextTrack() {
        if (playlist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        playSong(currentTrackIndex);
    }

    @FXML
    private void previousTrack() {
        if (playlist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
        playSong(currentTrackIndex);
    }

    private void playSong(int index) {
        try {
            Song song = playlist.get(index);
            updateNowPlayingUI(song);
            
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            
            Media media = new Media(song.getMediaUrl());
            mediaPlayer = new MediaPlayer(media);
            setupMediaPlayerListeners();
            mediaPlayer.play();
            isPlaying = true;
            playPauseIcon.setImage(pauseImage);
        } catch (Exception e) {
            System.err.println("Error playing song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupMediaPlayerListeners() {
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.toSeconds());
            }
        });
        
        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        });
        
        mediaPlayer.setOnEndOfMedia(this::nextTrack);
    }

    // Song data class
    private static class Song {
        private final String title;
        private final String artist;
        private final Image albumArt;
        private final String mediaUrl;

        public Song(String title, String artist, Image albumArt, String mediaUrl) {
            this.title = title;
            this.artist = artist;
            this.albumArt = albumArt;
            this.mediaUrl = mediaUrl;
        }

        // Getters
        public String getTitle() { return title; }
        public String getArtist() { return artist; }
        public Image getAlbumArt() { return albumArt; }
        public String getMediaUrl() { return mediaUrl; }
    }
}