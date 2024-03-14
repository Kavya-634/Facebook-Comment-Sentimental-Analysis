package codes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class FacebookPostFetcher {

	// Method to fetch the most recent post from a Facebook page
    public static void fetchMostRecentPost(String pageId, String accessToken) {
        try {
            String urlString = "https://graph.facebook.com/v19.0/" + pageId + "/feed?access_token=" + accessToken + "&fields=message,created_time&limit=1";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response to extract the most recent post
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray postsArray = jsonResponse.getJSONArray("data");
            if (postsArray.length() > 0) {
                JSONObject mostRecentPost = postsArray.getJSONObject(0);
                String message = mostRecentPost.optString("message", "No message available");
                String createdTime = mostRecentPost.optString("created_time", "No creation time available");

                System.out.println("Most recent post from page " + pageId + ":");
                System.out.println("Message: " + message);
                System.out.println("Created Time: " + createdTime);

                // Fetch comments for the most recent post
                fetchComments(mostRecentPost.getString("id"), accessToken);
            } else {
                System.out.println("No posts found for page " + pageId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to fetch comments for a given post ID
    public static String fetchComments(String postId, String accessToken) {

    	String sentiment = "positive";
        try {
            String urlString = "https://graph.facebook.com/v19.0/" + postId + "/comments?access_token=" + accessToken;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response to extract comments
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray commentsArray = jsonResponse.getJSONArray("data");

            System.out.println("Comments for Post " + postId + ":");
            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject comment = commentsArray.getJSONObject(i);
                String commentMessage = comment.optString("message", "No message available");
                String commenterName = comment.getJSONObject("from").optString("name", "Unknown");
                
                System.out.println("Comment from " + commenterName + ": " + commentMessage);
                
                //StanfordCoreNLP
                Properties props = new Properties();
                props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
                
                // Initialize StanfordCoreNLP with these properties
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                
                String text = commentMessage;
                
                CoreDocument document = new CoreDocument(text);
                pipeline.annotate(document);
                
                for (CoreSentence sentence : document.sentences()) {
                    sentiment = sentence.sentiment();
                    System.out.println(sentiment + "\t" + sentence);
                    
                }
                //StanfordCoreNLP
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return sentiment;
    }

    public static void main(String[] args) {
        // Replace <access_token> and <post_id> with your actual access token and post ID
    	String accessToken = "EAAGELRvlq20BO99FHcNwBZAJEwm9AA5UwDFZBetqDkE9cCapHao2F7lxhcwfNeFqfe0LMmQRi30ZAcuT1f1ZAqHCz6BQS01eS0qjJeU8pXooECIwnHe2E6BkoBIKgt5IztLPiOY4sZAFE6Bl4yl73paAqngq0ZAgqulSPmayZBytuLoQ5KNWnWdZAZBNWcnbSQLdGSj63YhFSu5isTtCfN82qwP4ZD";
        
        String pageId = "229975976874128";
        fetchMostRecentPost(pageId, accessToken);

    }
    
    
}
