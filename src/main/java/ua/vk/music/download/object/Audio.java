package ua.vk.music.download.object;

public class Audio {
    private String artist;
    private String title;
    private String duration;
    private String url;

    public Audio(String artist, String title, String duration, String url) {
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getUrl() {
        return url;
    }
}
