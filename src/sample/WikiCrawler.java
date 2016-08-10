package sample;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
    // keeps track of where we started
    private final String source;

    // the index where the results go
    private SerializableIndex index;

    // queue of URLs to be indexed
    private Queue<String> queue = new LinkedList<String>();

    // fetcher used to get pages from Wikipedia
    final static WikiFetcher wf = new WikiFetcher();

    /**
     * Constructor.
     *
     * @param source
     * @param index
     */
    public WikiCrawler(String source, SerializableIndex index) {
        this.source = source;
        this.index = index;
        queue.offer(source);
    }

    /**
     * Returns the number of URLs in the queue.
     *
     * @return
     */
    public int queueSize() {
        return queue.size();
    }

    /**
     * Gets a URL from the queue and indexes it.
     *
     * @return Number of pages indexed.
     * @throws IOException
     */
    public String crawl(boolean testing) throws IOException {
        String url = queue.poll();
        if(testing) {
            Elements paragraphs = wf.readWikipedia(url);
            index.indexPage(url, paragraphs);
            queueInternalLinks(paragraphs);
            return url;
        } else {
            if (index.isIndexed(url)) {
                return null;
            } else {
                Elements paragraphs = wf.readWikipedia(url);
                index.indexPage(url, paragraphs);
                queueInternalLinks(paragraphs);
                return url;
            }
        }
    }

    /**
     * Parses paragraphs and adds internal links to the queue.
     *
     * @param paragraphs
     */
    // NOTE: absence of access level modifier means package-level
    void queueInternalLinks(Elements paragraphs) {
        // FILL THIS IN!
        int counter = 0;
        while(counter < paragraphs.size()) {
            Element paragraph = paragraphs.get(counter);
            Iterable<Node> iter = new WikiNodeIterable(paragraph);
            for (Node node:iter) {
                if (node instanceof Element) {
                    String link = node.attr("href");
                    String answer = "https://en.wikipedia.org" + link;
                    if (link != "" && link.indexOf('i',3) == 4 && !link.contains("Wikipedia:") &&
                            !link.contains("Talk:") && !link.contains("#") ) {
                        queue.add(answer);
                        System.out.println(answer);
                    }
                }
            }
            counter++;
        }
    }

    public static void main(String[] args) throws IOException {

        // make a WikiCrawler
        //Jedis jedis = JedisMaker.make();
        SerializableIndex index = new SerializableIndex();
        SerializableDescriptions des = new SerializableDescriptions();
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        WikiCrawler wc = new WikiCrawler(source, index);

        // for testing purposes, load up the queue
        Elements paragraphs = wf.fetchWikipedia(source);
        wc.queueInternalLinks(paragraphs);

        for(String link: wc.queue) {
            Elements paragraph = wf.fetchWikipedia(link);
            des.extractSentence(paragraph,link);
            boolean a = index.indexPage(link, paragraph);
            if(!a) {
                break;
            }

        }

        index.serialize();
        des.serialize();

        // loop until we index a new page
//        String res;
//        do {
//            res = wc.crawl(false);
//
//        } while (res == null);
//
//        Map<String, Integer> map = index.getCounts("the");
//        for (Entry<String, Integer> entry: map.entrySet()) {
//            System.out.println(entry);
//        }
    }
}