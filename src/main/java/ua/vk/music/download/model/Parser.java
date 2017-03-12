package ua.vk.music.download.model;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ua.vk.music.download.object.Audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    public Map<String, String> getFormParameters(HttpEntity entity){
        Map<String, String> param = new HashMap<>();

        Document document = null;
        if(entity != null){
            try(InputStream in = entity.getContent()){
                document = Jsoup.parse(IOUtils.toString(in));
            } catch (IOException e) {
            }
        }

        Elements form = document.getElementsByTag("form");

        param.put("action", form.attr("action"));

        Elements elements = document.getElementsByAttributeValue("type", "hidden");

        for(Element element : elements){
            param.put(element.attr("name"), element.attr("value"));
        }

        return param;
    }

    private String fixWindowsFileName(String pathname) {
        String[] forbiddenSymbols = new String[]{"<", ">", ":", "\"", "/", "\\", "|", "?", "*"};
        String result = pathname;
        for (String forbiddenSymbol : forbiddenSymbols) {
            result = StringUtils.replace(result, forbiddenSymbol, "");
        }
        return StringEscapeUtils.unescapeXml(result);
    }

    public List<Audio> audioParser(String response){
        List<Audio> audioList = new ArrayList<>();

        if(response == null || response.isEmpty()) return audioList;

        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = null;
        try {
            jsonResponse = (JSONObject) parser.parse(response);
            JSONArray mp3list = (JSONArray) jsonResponse.get("response");

            for (int i=1; i<mp3list.size(); i++) {
                JSONObject mp3 = (JSONObject) mp3list.get(i);
                if(!mp3.get("url").toString().isEmpty()) {
                    audioList.add(new Audio(fixWindowsFileName(mp3.get("artist").toString()),
                            fixWindowsFileName(mp3.get("title").toString()),
                            mp3.get("duration").toString(),
                            mp3.get("url").toString()));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return audioList;
    }

    public List<Audio> wallParser(String response){
        org.json.JSONArray arrayResponse = new org.json.JSONObject(response).getJSONArray("response");
        List<Audio> audioList = new ArrayList<>();
        for(int responseCount = 1; responseCount < arrayResponse.length(); responseCount++){
            org.json.JSONArray arrayAttachments = arrayResponse.getJSONObject(responseCount).getJSONArray("attachments");
            for(int attachments = 0; attachments < arrayAttachments.length(); attachments++){
                org.json.JSONObject attachment = arrayAttachments.getJSONObject(attachments);
                try {
                    if (attachment.has("audio") && attachment.getJSONObject("audio").has("url")) {
                        audioList.add(new Audio(fixWindowsFileName(attachment.getJSONObject("audio").getString("artist")),
                                fixWindowsFileName(attachment.getJSONObject("audio").getString("title")),
                                attachment.getJSONObject("audio").getInt("duration") + "",
                                attachment.getJSONObject("audio").getString("url")));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return audioList;
    }
}
