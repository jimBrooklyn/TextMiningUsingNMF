/* database HW10(NMF)  by Xiangbo Zhao. 
 * this program is written in Java. All 360 URLs are from CNN;
 * extraction of text from html uses Jsoup package;
 * NMF uses a JAVA library: JML;
 */

import java.io.*;
import java.util.*;
import org.jsoup.Jsoup;
import jml.options.*;
import jml.clustering.*;
import jml.clustering.NMF.*;
import jml.matlab.*;
import org.apache.commons.math.linear.*;

public class TextMining {
  
  public TextMining(String filePath) throws IOException{
      File folder = new File(filePath);
      listOfFiles = folder.listFiles();
      makeStopWordSet();     
  }
  

       
  public void buildMatrix()throws IOException{
      buildWordsMap();
      initMatrix();      
      for(int docId =0; docId < n; docId++){
          File file = listOfFiles[docId]; 
          String tokens[] = htmlToText(file);          
          for (String word: tokens) {
               if (word.length() >1 && !stopWords.contains(word)&&
               wordsMap.containsKey(word))
               {
                  matrix[docId][wordsMap.get(word)]+= 1;
               }
          }
      }
  }
  
  public void nmfCal(){
      NMFOptions NMFOptions = new NMFOptions();
      NMFOptions.nClus = k;
      NMFOptions.maxIter = 200;
      NMFOptions.verbose = true;
      NMFOptions.calc_OV = false;
      NMFOptions.epsilon = 1e-5;
      Clustering nmf = new NMF(NMFOptions);
      nmf.feedData(matrix);
      nmf.clustering(null); // If null, KMeans will be used for initialization       
      
      System.out.printf("%nTopics and *topic word*: %n%n");
      RealMatrix H = nmf.getIndicatorMatrix();
        for(int i=0; i <k; i++){
            double[] arr = H.getColumn(i);
            int maxId = getMaxId(arr,m);
            topic[i] = wordsList.get(maxId);
            System.out.println("topic "+ (i+1) +"       *" + topic[i]+"*");
        }    
    
     System.out.printf("%nURLs and their *topic*: %n%n");  
        RealMatrix W = nmf.getCenters();
        for(int i=0; i < n; i++){
            double[] arr = W.getRow(i);
            int maxId = getMaxId(arr,k);
            System.out.println(listOfFiles[i].getName()+ "      *"+ topic[maxId]+"*");       
        } 
  }
   
  private String[] htmlToText(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();  
        String line;
        String[]tokens;    
        while ( (line=br.readLine()) != null) {
          sb.append(line);
        }    
        String textOnly = Jsoup.parse(sb.toString()).text();      
        tokens = textOnly.toLowerCase().split("[^a-z]+");
        return tokens;
  }
  
  
  private void buildWordsMap()throws IOException{
      Map<String, Integer> glossary  = new TreeMap<>();     
      for(File file: listOfFiles){
          String[] tokens = htmlToText(file);     
          for (String word: tokens) {
               if (word.length() >1 &&!stopWords.contains(word))
               {
                   int frequency;
                   if(glossary.containsKey(word)){
                         frequency = glossary.get(word) + 1;  
                   }
                   else{
                       frequency = 1;
                   }                 
                   glossary.put(word,frequency);
                }
          }
      }
      // remove words that appeared less than 5 times in all docs.      
      Iterator<Map.Entry<String,Integer>> iter = glossary.entrySet().iterator();
      while (iter.hasNext()) {
          Map.Entry<String,Integer> entry = iter.next();
          if(5 > entry.getValue()){
          iter.remove();
          }
      }
     
      wordsMap = new HashMap<>();
      int index =0;
      for(String word: glossary.keySet()){
          wordsMap.put(word, index++);
          wordsList.add(word);
      }      
  }
  
  private void initMatrix(){
      for(int i= 0; i<n; i++){
          for(int j=0; j<m; j++){
              matrix[i][j]= 0;
            }
      }
  }
  
  // to remove non-meaningful words
  private void makeStopWordSet()throws IOException{
      stopWords = new HashSet<String>();
      Scanner sc = new Scanner(new File("C:/test/project10/stopwordlist.txt"));
      while(sc.hasNext()){
          String line = sc.next();
          stopWords.add(line);
      }
  }
  
  private static int getMaxId(double[] arr, int n){
        double maxVal = arr[0];
        int maxId =0;
        for(int i=0; i<n; i++){
            if(arr[i] > maxVal){
                maxVal = arr[i];
                maxId = i;  
            }
        }
        return maxId;
  }
  
   public static void main(String[] args) throws IOException{    
      TextMining txtmn = new TextMining("C:/test/project10/docs/"); 
      txtmn.buildMatrix();
      txtmn.nmfCal();
  }
      

  private int n = 330;  
  private int m = 7873; 
  private int k = 10;
  private String[] topic = new String[k];
  private File[] listOfFiles;
  private Map<String, Integer> wordsMap = new HashMap<>();
  private List<String> wordsList = new ArrayList<String>();
  private double[][] matrix = new double[n][m];
  private Set<String> stopWords;  
}