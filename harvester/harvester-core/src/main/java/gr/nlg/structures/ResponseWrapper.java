package gr.nlg.structures;

import java.util.ArrayList;

/**
 * Created by Midas on 2/15/2017.
 */
public class ResponseWrapper {
    private ArrayList<ArchiveUrl> items ;

    public ResponseWrapper() {
        items = new ArrayList<ArchiveUrl>();
    }
    public void add(ArchiveUrl item){
        this.items.add(item);
    }
    public ResponseWrapper(ArrayList<ArchiveUrl> items) {
        this.items = items;
    }

    public ArrayList<ArchiveUrl> getItems() {
        return items;
    }

    public void setItems(ArrayList<ArchiveUrl> items) {
        this.items = items;
    }
}
