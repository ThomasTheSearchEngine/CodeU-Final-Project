package sample;
import java.io.*;
import java.util.*;

import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


/**
 * Created by ericachia on 8/9/16.
 */
public class SerializableIndex {

        private Map<String, Set<TermCounter>> index = new HashMap<String, Set<TermCounter>>();
        private Set<String> urls = new HashSet<>();
        private int urlCounts;



    public void deserialize() {
        HashMap<String, Set<TermCounter>> map = null;
        try {
            FileInputStream fis = new FileInputStream("hashmap.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            urlCounts = ois.readInt();
            index = (HashMap) ois.readObject();
            ois.close();
            fis.close();
            System.out.println("Size of map is: " + index.size());
            System.out.println("Deserialized HashMap..");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

    }
        /**
         * Adds a TermCounter to the set associated with `term`.
         *
         * @param term
         * @param tc
         */
        public void add(String term, TermCounter tc) {
            Set<TermCounter> set = get(term);

            // if we're seeing a term for the first time, make a new Set
            if (set == null) {
                set = new HashSet<TermCounter>();
                index.put(term, set);
            }
            // otherwise we can modify an existing Set
            set.add(tc);
        }

        /**
         * Looks up a search term and returns a set of TermCounters.
         *
         * @param term
         * @return
         */
        public Set<TermCounter> get(String term) {
            return index.get(term);
        }

        public boolean isIndexed(String url) {
            return urls.contains(url);
        }

        /**
         * Add a page to the index.
         *
         * @param url         URL of the page.
         * @param paragraphs  Collection of elements that should be indexed.
         */
        public boolean indexPage(String url, Elements paragraphs) {
            System.out.println("Indexing : " + url);
            TermCounter answer = new TermCounter(url);
            // make a TermCounter and count the terms in the paragraphs
            // TODO: fill this in
            answer.processElements(paragraphs);
            // for each term in the TermCounter, add the TermCounter to the index
            // TODO: fill this in
            for(String key:answer.keySet()) {
                add(key, answer);
            }
            urls.add(url);
            return urls.size() < 500;
        }

        public Set<String> getURLs(String term) {
            Set<TermCounter> tc = get(term);
            Set<String> urls = new HashSet<>();
            for(TermCounter termC:tc) {
                urls.add(termC.getLabel());
            }
            return urls;
        }

        public Double getCount(String term, String url) {
            Set<TermCounter> tc = get(term);
            for(TermCounter termC:tc) {
                if(termC.getLabel().equals(url)) {
                    return termC.get(term);
                }
            }
            return 0.0;
        }

        public Map<String, Double> getCountsFaster(String term) {
            Map<String, Double> map = new HashMap<String, Double>();
            Set<String> urls = getURLs(term);

            Double idf = 0.0;
            Integer urlsSize = urls.size();

            if ( urlsSize != 0 ) {
                idf = Math.log( (double) urlCounts / urlsSize);
            }

            for (String url: urls) {
                Double count = getCount(term, url) * idf;
                map.put(url, count);
            }

            return map;
        }

        /**
         * Prints the contents of the index.
         */
        public void printIndex() {
            // loop through the search terms
            for (String term: keySet()) {

                // for each term, print the pages where it appears
                Set<TermCounter> tcs = get(term);
                for (TermCounter tc: tcs) {
                    Double count = tc.get(term);
                    System.out.println("    " + tc.getLabel() + " " + count);
                }
            }
        }

        /**
         * Returns the set of terms that have been indexed.
         *
         * @return
         */
        public Set<String> keySet() {
            return index.keySet();
        }

        public void serialize() {
            try
            {
                FileOutputStream fos =
                        new FileOutputStream("hashmap.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeInt(urls.size());
                oos.writeObject(index);
                oos.close();
                fos.close();
                System.out.printf("Serialized HashMap data is saved in hashmap.ser");
            }catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }

        /**
         * @param args
         * @throws IOException
         */
        public static void main(String[] args) throws IOException {

            WikiFetcher wf = new WikiFetcher();
            SerializableIndex indexer = new SerializableIndex();

            String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
            Elements paragraphs = wf.fetchWikipedia(url);
            indexer.indexPage(url, paragraphs);

            url = "https://en.wikipedia.org/wiki/Programming_language";
            paragraphs = wf.fetchWikipedia(url);
            indexer.indexPage(url, paragraphs);

            indexer.printIndex();
        }


    }
