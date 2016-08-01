package sample;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}

	
	/**
	 * Prints the contents in order of term frequency.
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}

	public Set<String> getKeys() {
		return map.keySet();
	}

	public Set<String> getAllKeys(WikiSearch that) {
		Set<String> current = getKeys();
		Set<String> something = new HashSet<String>();
		something.addAll(that.getKeys());
		for(String key:current){
			something.add(key);
		}
		return something;
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        Map<String,Integer> answer = new HashMap<String,Integer>();
        Set<String> keys = getAllKeys(that);
        for(String key:keys) {
        	int total = totalRelevance(getRelevance(key),that.getRelevance(key));
        	answer.put(key,total);
        }
        WikiSearch done = new WikiSearch(answer);
		return done;
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        Map<String, Integer> answer = new HashMap<String,Integer>();
        Set<String> keys = getAllKeys(that);
        for(String key:keys) {
        	int rel1 = getRelevance(key);
        	int rel2 = that.getRelevance(key);
        	if(rel1 == 0 || rel2 == 0) {
        		answer.put(key, 0);
        	} else {
        		answer.put(key, totalRelevance(rel1,rel2));
        	}
        }
        WikiSearch done = new WikiSearch(answer);
		return done;
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        Map<String, Integer> answer = new HashMap<String,Integer>();
        Set<String> keys = getAllKeys(that);
        Set<String> badKeys = that.getKeys();
        for(String key:keys) {
        	if(badKeys.contains(key)) {
        		answer.put(key,0);
        	} else {
        		answer.put(key, getRelevance(key));
        	}
        }
        WikiSearch done = new WikiSearch(answer);
		return done;
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
		Comparator<Entry<String,Integer>> comparator = new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String,Integer> e1, Entry<String,Integer> e2) {
				if(e1.getValue() < e2.getValue()) {
					return -1;
				} else if (e1.getValue() > e2.getValue()){
					return 1;
				}
				return 0;
			}
		};
        List aList = new LinkedList();
        for(Entry<String,Integer> entry:map.entrySet()){
        	aList.add(entry);
        }
        Collections.sort(aList,comparator);
		return aList;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
