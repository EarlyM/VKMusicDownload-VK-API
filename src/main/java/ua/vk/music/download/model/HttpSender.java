package ua.vk.music.download.model;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import ua.vk.music.download.object.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpSender {

    private static HttpSender httpSender;

    private HttpClient client = HttpClients.createDefault();
    private HttpPost post;
    private HttpResponse response;
    private RequestConfig config;
    private URIBuilder builder;

    private HttpSender(){
        this.client = HttpClients.createDefault();
        this.config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
    }

    public static HttpSender getInstance(){
        if(httpSender == null){
            httpSender = new HttpSender();
        }
        return httpSender;
    }

    public void authorize() throws Exception{
        URI uri = getLoginPageURI();
        post = new HttpPost(uri);
        post.setConfig(config);
        response = client.execute(post);
        post.abort();

        Map<String, String> param = new Parser().getFormParameters(response.getEntity());

        post = new HttpPost(param.get("action"));
        UrlEncodedFormEntity formEntity = fillForm(param);
        post.setEntity(formEntity);
        post.setConfig(config);
        response = client.execute(post);

        String token = null;

        while (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY){
            token = response.getFirstHeader("Location").getValue();
            post = new HttpPost(token);
            post.setConfig(config);
            response = client.execute(post);
            post.abort();
        }

        if(token != null){
            token = token.substring(token.indexOf("=")+1, token.indexOf("&"));
        }

        User.getInstance().setToken(token);

    }

    public String searchAudio(String id) throws IOException, URISyntaxException {
        builder = new URIBuilder();
        builder.setScheme("https").setHost("api.vk.com").setPath("/method/audio.search")
                .setParameter("q",id)
                .setParameter("auto_complete","1")
                .setParameter("lyrics", "0")
                .setParameter("performer_only", "0")
                .setParameter("sort", "2")
                .setParameter("search_own", "0")
                .setParameter("offset", "0")
                .setParameter("count", "100")
                .setParameter("access_token", User.getInstance().getToken());
        return send();
    }

    public String searchAudioOnWall(String id) throws IOException, URISyntaxException {
        builder = new URIBuilder();
        builder.setScheme("https").setHost("api.vk.com").setPath("/method/wall.get")
                .setParameter("owner_id", id)
                .setParameter("domain", "")
                .setParameter("offset", "0")
                .setParameter("count", "100")
                .setParameter("filter", "owner")
                .setParameter("extended", "0")
                .setParameter("fields", "0")
                .setParameter("access_token", User.getInstance().getToken());

        return send();
    }

    public String searchUserAudio(String id) throws IOException, URISyntaxException {
        builder = new URIBuilder();
        builder.setScheme("https").setHost("api.vk.com").setPath("/method/audio.get")
                .setParameter("oid", id)
                .setParameter("need_user", "0")
                .setParameter("count", "2000")
                .setParameter("offset", "0")
                .setParameter("access_token", User.getInstance().getToken());

        return send();
    }

    private URI getLoginPageURI() throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("oauth.vk.com").setPath("/authorize")
                .setParameter("client_id", "5909285")
                .setParameter("redirect_uri", "https://oauth.vk.com/blank.html")
                .setParameter("display", "page")
                .setParameter("scope", "audio")
                .setParameter("response_type", "token").
                setParameter("v", "5.62");
        return builder.build();
    }

    private UrlEncodedFormEntity fillForm(Map<String, String> param) throws UnsupportedEncodingException {
        List<NameValuePair> postForm = new ArrayList<>();

        postForm.add(new BasicNameValuePair("_origin", param.get("_origin")));
        postForm.add(new BasicNameValuePair("ip_h", param.get("ip_h")));
        postForm.add(new BasicNameValuePair("lg_h", param.get("lg_h")));
        postForm.add(new BasicNameValuePair("to", param.get("to")));
        postForm.add(new BasicNameValuePair("expire", "0"));
        postForm.add(new BasicNameValuePair("email", User.getInstance().getName()));
        postForm.add(new BasicNameValuePair("pass", User.getInstance().getPassword()));

        return new UrlEncodedFormEntity(postForm,"UTF-8");
    }

    private String send() throws URISyntaxException, IOException {
        HttpGet httpGet = new HttpGet(builder.build());
        HttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String temp = "";
        if(entity != null){
            try(InputStream in = entity.getContent()){
                temp = IOUtils.toString(in);
            }
        }
        System.out.println(temp);
        return temp;
    }
}
