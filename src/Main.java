import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, ParseException {

        /*
        * this project needs to do several things
        * download congress stock transactions
        * and store stock information for them
        *
        * have a way to update information without needing to reload all pdf transactions
        *
        * and have a way to recall all info without needing to process all pdf files
        *
        * live aspects will be it downloading and fixing data when files are damaged or removed
        * and current stock info / price will also be live
        *
        *
        * */

        ProgramManager programManager = new ProgramManager();

        // main is just a simple driver it will make a program manager object to start the project
    }
}