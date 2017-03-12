package ua.vk.music.download.model;

import org.apache.commons.io.FileUtils;
import ua.vk.music.download.object.Audio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AudioDownloader {

    private static final String DIRECTORY = "../../../music";

    public void downloadAudio(Audio audio) throws IOException {
        String patchName = DIRECTORY + audio.getArtist() + " - " + audio.getTitle() + ".mp3";
        File destination = new File(patchName);
        if (!destination.exists()) {
            FileUtils.copyURLToFile(new URL(audio.getUrl()), destination);
        }
    }

    public void downloadAllAudio(List<Audio> audioList) throws IOException{
        for(Audio audio : audioList){
            downloadAudio(audio);
        }
    }
}
