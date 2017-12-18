package gr.nlg.structures;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by midas on 2/15/2017.
 */
public class ArchiveUrl {
    public HashSet<String > topLevelDomains=new HashSet<String>(Arrays.asList("blogspot.gr", "gov.gr","com.gr","net.gr","blogspot.com"));
    private String url;
    private String date;
    private String title;
    private String content;
private String waybackurl;
private String domain;

    public ArchiveUrl(String url, String date, String title,String wayback) throws URISyntaxException {
        this.url = url;

        this.date = date;
        this.title = title;
        URI uri = null;
        url=url.substring(1);
        url=url.substring(0,url.length()-1);
        uri = new URI(url);
        String host = uri.getHost();
        String[] domainParts = host.split("\\.");

        this.domain = domainParts[domainParts.length-2]+"."+domainParts[domainParts.length-1];
        if(topLevelDomains.contains(this.domain))
            this.domain = domainParts[domainParts.length-3]+"."+domainParts[domainParts.length-2]+"."+domainParts[domainParts.length-1];
        this.waybackurl= wayback+"/*/"+this.domain;
    }

    public String getDomain() {
        return this.domain;
    }
    public String getwaybackurl() {
        return this.waybackurl;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
