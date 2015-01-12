package eu.hydrologis.rap.log;

public class StageLogger {

    public static void logInfo( String msg ) {
        System.out.println(msg);
    }

    public static void logDebug( String msg ) {
        System.out.println(msg);
    }

    public static void logError( String msg, Exception e ) {
        System.err.println(msg);
        e.printStackTrace();
    }

}
