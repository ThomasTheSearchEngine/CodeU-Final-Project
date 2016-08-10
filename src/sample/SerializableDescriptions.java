package sample;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by ericachia on 8/9/16.
 */
public class SerializableDescriptions {

    private Map<String, String > map = new HashMap<>();


    public void put(String url, String img) {
        map.put(url, img);
    }

    public void extractSentence(Elements imgs, String url) {
        String answer = "";
        Element main = imgs.get(0);
        Iterable<Node> iter = new WikiNodeIterable(main);
        for(Node node: iter) {
            if(node instanceof TextNode) {
                answer += node.toString();
            }
        }
        System.out.println("Sentence is: " + answer);
        put(url, answer);
    }

    public String get(String url) {
        System.out.println("THE VALUE OF " + url + " IS " + map.get(url));
        return map.get(url);
    }

    public void serialize() {
        try
        {
            FileOutputStream fos =
                    new FileOutputStream("descriptions.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in descriptions.ser");
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public void deserialize() {
        try {
            FileInputStream fis = new FileInputStream("descriptions.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (HashMap) ois.readObject();
            ois.close();
            fis.close();
            System.out.println("Deserialized HashMap for images..");
            System.out.println("IMG map size is : " + map.keySet().size());
            System.out.println(map.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

    }

    public String toString() {
        return map.toString();
    }


}
