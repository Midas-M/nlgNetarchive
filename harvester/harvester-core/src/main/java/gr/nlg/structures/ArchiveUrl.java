package gr.nlg.structures;

/**
 * Created by midas on 2/15/2017.
 */
public class ArchiveUrl {
    private String url;
    private String date;
    private String title;
    private String content;

    public ArchiveUrl(String url, String date, String title, String content) {
        this.url = url;
        this.date = date;
        this.title = title;
        this.content = content;
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
