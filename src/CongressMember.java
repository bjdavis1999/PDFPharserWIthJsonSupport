import java.util.ArrayList;
import java.util.HashMap;


/**
 * this object stores a congress member with all there transactions
 */
public class CongressMember {
    /*
    * used to store data about a congress member
    * includeing transactions
    *
    * */
    // stores the name of a congress member
    String name;
    Boolean FilesReloaded;

    // stores a list of PDF files Should be removed after startup and update as it will no longer be usefull
    ArrayList<PdfIdentifier> pdfIdentifiers = new ArrayList<>();

    // used to store list of all stocks with transactions
    ArrayList<String> StockTickers = new ArrayList<>();

    // used to store list of all transactions with price data and date
    HashMap<String,ArrayList<Transaction>> transactions = new HashMap<>();



}
