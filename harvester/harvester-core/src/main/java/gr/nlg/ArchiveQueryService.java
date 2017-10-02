/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.nlg;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ListIterator;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;


import gr.nlg.structures.ArchiveUrl;
import gr.nlg.structures.ResponseWrapper;

/**
 *
 * @author pmeladianos
 */
public class ArchiveQueryService {

    DateTimeFormatter formatter=  DateTimeFormatter.ISO_INSTANT;

    public ResponseWrapper getUrls(String input,String dateFromRaw,String dateToRaw) {
        //DATE FORMATS ISO_INSTANT
        ZonedDateTime dateFrom;
        ZonedDateTime dateTo;
        ZoneId zone= ZoneId.of("UTC");
        //YY-MM-DD
        if(!dateFromRaw.equals("none")) {
            LocalDateTime temp = LocalDateTime.of(Integer.valueOf(dateFromRaw.split("-")[0]), Integer.valueOf(dateFromRaw.split("-")[1]), Integer.valueOf(dateFromRaw.split("-")[2]), 0, 0);
            dateFrom=ZonedDateTime.of(temp,zone);
        }
        else {
            LocalDateTime temp = LocalDateTime.of(1999, Month.JANUARY, 1, 0, 0);
            dateFrom=ZonedDateTime.of(temp,zone);
        }
        if(!dateToRaw.equals("none")) {
            LocalDateTime temp = LocalDateTime.of(Integer.valueOf(dateToRaw.split("-")[0]), Integer.valueOf(dateToRaw.split("-")[1]), Integer.valueOf(dateToRaw.split("-")[2]), 0, 0);
            dateTo=ZonedDateTime.of(temp,zone);
        }
        else {
            LocalDateTime temp = LocalDateTime.now();
            dateTo=ZonedDateTime.of(temp,zone);
        }
        String dateRange="["+dateFrom.format(formatter)+" TO "+dateTo.format(formatter)+"]";

        SolrQuery solrQuery;
        solrQuery = new SolrQuery();

        String query;
        if (input.contains("http") || input.contains("www")){
            query = getUrlQuery(input, dateRange);
        }else{
            query = getQuery(input,dateRange);
            if (!query.equals("")) {
                solrQuery.setHighlight(true).setHighlightSnippets(1).setHighlightSimplePost("</mark></strong>").setHighlightSimplePre("<strong><mark>"); //set other params as needed
                solrQuery.setParam("hl.fl", "content_t");
                solrQuery.setParam("hl.requireFieldMatch", "true");
            }
        }

        solrQuery.setQuery(query);

        solrQuery.setRows(150);
        String urlString = "http://185.78.220.36:8983/solr/nlg_archive";
        SolrClient server = new HttpSolrClient.Builder(urlString).build();
        QueryResponse response = null;
        try {
            response = server.query(solrQuery);

        } catch (Exception e) {
            System.out.println(e);
        }
        SolrDocumentList rs = response.getResults();
        long numFound = rs.getNumFound();
        int numResultsDisplay = (int) numFound;
        ResponseWrapper responseWrapper=new ResponseWrapper();
                
        ListIterator<SolrDocument> iter = rs.listIterator();

        while (iter.hasNext()) {
            SolrDocument doc = iter.next();
            String url = doc.get("url_s").toString();
            String wayback = doc.get("wayback_s").toString();
            Date dDate = (Date) doc.get("date_dt");
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dDate);
            String title = doc.get("title_t").toString();
            String content;
            try{
                content = response.getHighlighting().get(doc.get("id").toString()).get("content_t").get(0);
            } catch (NullPointerException e){
                content = doc.get("content_t").toString();
            }
            responseWrapper.add(new ArchiveUrl( url,  date,  title,  content,wayback));
        }
        //Gson gson = new Gson();
        //String APIresponse = gson.toJson(responseWrapper);
        return responseWrapper;
    }

    private static String getQuery(String keywords,String dateRange) {
        String query = "";
        keywords.replaceAll(","," ");
        String query_0 = queryBuilder(keywords, "title_t");

        String query_1 = queryBuilder(keywords, "content_t");
        query = query_0 + " OR (" + query_1+")^10"+" AND date_dt:"+dateRange;
        return query;
    }

    private static String getUrlQuery(String url, String dateRange) {
        String query = "url_s:" + url.replace(":", "\\:");
        query += " AND date_dt:"+dateRange;
        return query;
    }

    private static String queryBuilder(String s, String field) {
        String res = "";
        res = field + ":" + "'" + s + "'~1000";
        return res;

    }

}
