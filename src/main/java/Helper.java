/**
 * Created by Jukka on 25.6.2017.
 */
public class Helper {

    /**
     * Prints message to console if debug is set to true
     * @param msg Debug message
     */
    public static void debug(String msg){
        if(Main.DEBUG){
            System.out.println(msg);
        }
    }

    /**
     * Removes unicode whitespaces from a String
     * @param str String to be modified
     * @return  String without whitespaces
     */
    //Removes whitespaces from parameter, returns modifies String
    public static String trim(String str){
        if (str != null && str.length() > 0){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++){
                if (!Character.isWhitespace(str.charAt(i))){
                    sb.append(str.charAt(i));
                }
            }
            return sb.toString();
        }
        return str;
    }
}