package sample;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


/**
 * Encapsulates a map from search term to frequency (count).
 * 
 * @author downey
 *
 */
public class TermCounter implements Serializable{

	private Map<String, Double> map;
	private String label;
	
	public TermCounter(String label) {
		this.label = label;
		this.map = new HashMap<String, Double>();
	}
	
	public String getLabel() {
		return label;
	}

	/**
	 * Takes a collection of Elements and counts their words.
	 * 
	 * @param paragraphs
	 */
	public void processElements(Elements paragraphs) {
		for (Node node: paragraphs) {
			processTree(node);
		}
        tf();
	}
	
	/**
	 * Finds TextNodes in a DOM tree and counts their words.
	 * 
	 * @param root
	 */
	public void processTree(Node root) {
		// NOTE: we could use select to find the TextNodes, but since
		// we already have a tree iterator, let's use it.
		for (Node node: new WikiNodeIterable(root)) {
			if (node instanceof TextNode) {
				processText(((TextNode) node).text());
			}
		}
	}

	/**
	 * Splits `text` into words and counts them.
	 * 
	 * @param text  The text to process.
	 */
	public void processText(String text) {
		// replace punctuation with spaces, convert to lower case, and split on whitespace
		String[] array = text.replaceAll("\\pP", " ").toLowerCase().split("\\s+");
		
		for (int i=0; i<array.length; i++) {
			String term = array[i];
			incrementTermCount(term);
		}
	}

	/**
	 * Increments the counter associated with `term`.
	 * 
	 * @param term
	 */
	public void incrementTermCount(String term) {
		// System.out.println(term);
		put(term, get(term) + 1);
	}

	/**
	 * Adds a term to the map with a given count.
	 * 
	 * @param term
	 * @param count
	 */
    public void put(String term, double count) {
        map.put(term, count);
    }

	/**
	 * Returns the count associated with this term, or 0 if it is unseen.
	 * 
	 * @param term
	 * @return
	 */
    public Double get(String term) {
        Double count = map.get(term);
        return count == null ? 0 : count;
    }

    /**
     * Print the terms and their counts in arbitrary order.
     */
    public void printTF() {
        for (String key: keySet()) {
            Double count = get(key);
            System.out.println(key + ", " + count);
        }
        // System.out.println("java" + get("java"));
    }

	/**
	 * Returns the set of terms that have been counted.
	 * 
	 * @return
	 */
	public Set<String> keySet() {
		return map.keySet();
	}

    /**
     * Calculates tf and puts in map
     *
     * @return
     */
    public void tf() {

        double max = 0;
        for (Double value: map.values()) {
            if ( value > max )
                max = value;
        }
        // System.out.println ( total );

        for ( Map.Entry<String, Double> entry: map.entrySet() ) {
            // System.out.println( entry );
            // System.out.println( entry.getValue()/total );

            put( entry.getKey(), 0.4+(1-0.4)*entry.getValue()/max );
            // put( entry.getKey(), entry.getValue()/max );
        }
    }

    private void writeObject(java.io.ObjectOutput out) throws IOException {
    	out.writeObject(label);
		out.writeObject(map);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		label = (String)in.readObject();
		map = (HashMap<String, Double>)in.readObject();
	}

	private void readOBjectNoData() throws ObjectStreamException {

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		
		WikiFetcher wf = new WikiFetcher();
		Elements paragraphs = wf.fetchWikipedia(url);
		
		TermCounter counter = new TermCounter(url.toString());
		counter.processElements(paragraphs);
		counter.printTF();
	}
}
