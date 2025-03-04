import com.google.gson.Gson;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * used to extract data from a file
 */
public class FileParser {

    // used to parse data from JSON and return a string list

    /**
     * returns a list of congress names from a json file
     * @param path path to JSON file
     * @return
     */
    public ArrayList<String> congressNamesFromJson(String path) {

        // list to be returned
        ArrayList<String> names = new ArrayList<>();
        // gson object used to parse json objects
        Gson gson = new Gson();

        // creates a File object for the path given
        File FilePath = new File(path);
        // returns the string format of the JSON file
        String json = getJsonTextFromPath(FilePath);
        // splits the JSON object via commas
        String[] splitJson = json.split(",");
        // removes brackets from the end and begging objects so they can be parsed
        splitJson[0] = splitJson[0].substring(1);
        int endIndex = splitJson.length-1;
        splitJson[endIndex] = splitJson[endIndex].replace(']',' ');

        // loops through all the JSON objects to be converted
        for (int i=0;i < splitJson.length;i++){
            // returns a name object that contains the string being added to the list
            // note to self-change name object name to be more appropriate at
            // some point this will require reloading all files from scratch so do later
            Name name = gson.fromJson(splitJson[i], Name.class);
            // adds string to list
            names.add(name.getName());
        }
        // returns the list of strings
        return  names;
    }


    String getJsonTextFromPath(File filePath) {
        String JsonText = "";
        try {

            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            JsonText = reader.readLine();


            reader.close();

        } catch (Exception e){

        }


        return  JsonText;

    }

    /**
     * gets the pdf identifiers and year for all transactions in zip
     * adds them to the correct congress member
     * @param congress map of congress members
     * @throws FileNotFoundException
     */
    public void extractAllPdfFileNames(HashMap<String,CongressMember> congress) throws FileNotFoundException {

        //gets the current year and subtracts the farthest year we have data for
        // needed to know how many files to go through
        int currentYear = Year.now().getValue();
        int NumberOfFiles = currentYear - 2016;

        // loops through all PDF files
        for (int i =16;i < NumberOfFiles+17;i++){

            //uses the data parser to extract required info and store it in the relevant Congressperson
            extractPdfFileName("20"+i+"FD.xml",congress);

        }

    }


    // extracts the name of the pdf files from the unzipped xml files for all congresspeople Stores it as a DocID and stores the
    // ear for later retrieval from website

    /**extracts gets the pdf identifiers and year for a unziped xml file
     *
     * @param fileName name of file being extracted from
     * @param congress map of congress members
     * @throws FileNotFoundException
     */
    void extractPdfFileName(String fileName, HashMap<String,CongressMember> congress) throws FileNotFoundException {


        // stores the path to the xml document that has all fillings listed for a year
        File file = new File("Zips/unzipped/"+ fileName);
        // scanner used to go through document
        Scanner scan = new Scanner(file);
        //loops through file
        while (scan.hasNext()){

            // stores next line in file to string
            String line = scan.nextLine();
            // checks for the last name tag in the line if found starts creating or updating a congress person
            if (line.contains("Last")) {


                String Name = getString(line, '<', 10);
                if (congress.containsKey(Name)) {

                    // first makes a temp congress member
                    CongressMember Temp = congress.get(Name);


                    // moves scanner lines to the next line needed
                    line = moveXLines(scan, 2);

                    // if the filling is the correct type continues process
                    if (line.charAt(16) == 'P') {

                        PdfIdentifier TempIdentifier = new PdfIdentifier();
                        // moves down a line
                        line = moveXLines(scan, 1);
                        // gets the year of the filling
                        TempIdentifier.year = getString(line, '<', 10);
                        // moves down another line
                        line = moveXLines(scan, 1);
                        // gets the document id of the filling
                        TempIdentifier.DocID = getString(line, '<', 11);
                        // updates the congress member
                        Temp.pdfIdentifiers.add(TempIdentifier);

                    }


                    congress.put(Name, Temp);

                }
            }
        }

    }



    // advances a counter to the correct index of a string takes a starting index and a char to find

    /**
     * takes a string and moves a index to a chosen char in the string
     * @param line string being parsed
     * @param character char being moved 2
     * @param Index starting index in line
     * @return returns new index
     */
    private int moveIndexToChar(String line,char character,int Index){

        // moves index tell it finds char value
        while(line.charAt(Index) != character){

            Index++;
        }
        // returns index
        return Index;
    }

    // moves a scanner object x number of lines in order to advance to required info

    /**
     * moves scanner x number of lines
     * @param scan scanner object
     * @param index number of lines to skip
     * @return new line
     */
    private String moveXLines(Scanner scan,int index){

        // moves line x number of times
        for (int i = 0;i < index;i++){

            if (scan.hasNext()) {
                scan.nextLine();
            }

        }
        // returns line that is after loop
        if (scan.hasNext()) {
            return scan.nextLine();
        }
        return "";
    }

    // gets all the char in a string from a given start index tell it finds an ending char
    // can take a null value to stop when char are no longer an int value will skip , to get a parsable value

    /**
     * gets a string form a index to a chosen char
     * @param line string being parsed
     * @param character character moving 2
     * @param Index starting index
     * @return string result
     */
    private String getString(String line,Character character,int Index){



        // used to build the string being returned
        StringBuilder newString = new StringBuilder();

        // checks if the char parameter is null
        if (character == null){

            // loops tell it finds a non number skips comas
            while ((isCharInt(line.charAt(Index))||line.charAt(Index) == ',')&&Index != line.length()-1){

                // skips coma
                if (line.charAt(Index) != ','){
                    //append number to string builder
                    newString.append(line.charAt(Index));
                }

                // advance index
                Index++;
            }
            // used to get a string of a in a range based of a char position and a starting index
        } else {

            // checks if char at index is not goal
            while (line.charAt(Index) != character){
                // skips commas
                if (line.charAt(Index) != ','){
                    // appends value to string builder
                    newString.append(line.charAt(Index));
                }
                // advance index
                Index++;
            }

        }

        //return a generated string
        return  newString.toString();
    }

    // checks if a char is an int value 0-9

    /**
     * checks if a char value is a int
     * @param character char being checked
     * @return
     */
    private boolean isCharInt(char character){
        return character >= '0' && character <= '9';

    }




    // gets the transactions for any congressperson by using the index they are stores in the congress object used to store all
    // congresspeople stores data in the transactions arraylist for given congressperson

    /**
     * gets the transactions for any congressperson by using the identifiers
     * they are stores in the congress object used to store all
     * @param congress map of congress members
     * @param name name of congress member being updated
     * @throws IOException
     */
    public void getCongressMemberTransactions(HashMap<String,CongressMember> congress,String name) throws IOException {


        // gets the name of the congressperson being worked on


        // gets the directory there pdfs are stored in
        File dir = new File("PDF/"+name);
        // creates a list of file names for all pdfs in the directory
        File[] files = dir.listFiles();

        // try catch block catch null pointer exception if one happens
        try {

            int count =0;
            // loops through all pdf files in lastname directory
            for (File file : files) {


                System.out.println(file.toString());
                // returns the string text of the pdf
                String pdfText = ReturnPDFText(file.toString());
                // used to get transactions from pdf string and add them to congresspersons transaction list
                count++;
                getStockDataFromPDFText(pdfText, congress, name);


            }

        }catch (NullPointerException n){
            System.out.println("nullPointerError while parsing pdf");
        }



    }


    /**
     * gets the transaction information form a pdf in a string format
     * @param text psf in string form
     * @param congress map of congress
     * @param name name of congress member being uodated
     */
    // gets the transaction data from pdfs note a pdf can have more than one transaction likely many
    public void getStockDataFromPDFText(String text, HashMap<String,CongressMember> congress, String name){

        // used to store a string with an entire transaction stored on it
        String currentLine;
        // scanner used to load text and organize it
        Scanner scan = new Scanner(text);
        // stores the scanner lines to be added to current line or skipped if not needed
        // String priorLine = scan.nextLine();
        String line = scan.nextLine();
        // loops through all lines in a document
        while ((scan.hasNext())){
            // checks for a char that begins all transactions
            if (line.contains("sP")||line.contains("SP")||line.contains("(")) {
                // adds that line to the current transaction line
                currentLine = getNextLine(scan,line);
                if (!currentLine.toUpperCase().contains(": NEW")){

                    currentLine = "did not contain new filling tag";
                }

                // makes sure the transaction is a stock trade as other transactions can be in these reports


                // gets needed info from current line and stores it to the congresspersons transactions
                parseDataFromLine(currentLine,congress,name);


            }


            // prevents an error if code gets to last line in scanner before starting ending loop
            if (scan.hasNext()){
                //priorLine = line;
                line = scan.nextLine();
            }

        }



    }
    // checks if a line contains a trade type indicator returns true if it has one that indicates a stock or if it is missing one
    // because of how some of the pdfs are set up we will need to verify if tickers are a valid stock when getting price info
    // however this function lets me reduce the amount of invalid transactions needed to be sorted through when pricing

    /**
     * checks if a string contains test that indicates a stock trade
     * @param currentLine string being checked
     * @return bool true if it contains an indicator or no indicator is
     * present and false if the wrong indicator is present
     */
    private boolean ContainsStockTradeIndicator(String currentLine){

        // checks for ST the trade type indicator for stocks always contained within []
        if (currentLine.contains("[ST]")){
            return true;
        }
        // returns true if no indicator is present sense this may still be a stock trade
        return !currentLine.contains("[");
    }

    // used to get a price from the price range

    /**
     * gets a price from a pdf stirng for transaction can be min or max of range
     * @param currentLine string being parsed
     * @param index were to start from
     * @return
     */
    private int getPrice(String currentLine,int index){

        // makes sure price is a valid price there are cases were .01 is reported or an improper range is used for certain transaction
        // these cases are set to negative one and will be filtered out of the results later
        try {
            // stores minimum of price range when null is inputted goes until the character at index is not an int value 0-9
            return Integer.parseInt(getString(currentLine, null, index));
        } catch (NumberFormatException n){
            // invalid result indicator
            return  -1;
        }
    }

    // get transaction data from a string and store it to congress persons transactions

    /**
     * gets all necessary transaction info from PDF line
     * @param currentLine line being parsed
     * @param congress congress map
     * @param name name of congress member
     */
    public  void parseDataFromLine(String currentLine, HashMap<String,CongressMember> congress, String name){

        // creates a temporary transaction to be added to congress person at end
        Transaction Temp = new Transaction();
        // stores the price range in an array
        //int[] purchaseRange = new int[2];
        // gets returns true if line has a st indicator or no indicator
        boolean StockIndicator = ContainsStockTradeIndicator(currentLine);
        // used to move around line char for parsing
        int index =0;
        // first check to make sure line is valid needs a ticker contained within (XXXX)
        // and an indicator [ST] for stock trade or no indicator to pass
        if (currentLine.contains("(")&&StockIndicator){
            // moves to the start of the ticker
            index = moveIndexToChar(currentLine,'(',0);
            // moves 1 more to be on actual ticker text
            index++;
            // stores the ticker name in a temp variable so we can determine if its valid
            String TempTicker = (getString(currentLine,')',index));
            // checks if ticker is longer then stock minimum
            if (TempTicker.length() <= 5) {
                // try to parse data from rest of line if fails skips line as it is not format correctly
                // and probably does not contain a trade
                // via debug there is 617 lines that produce a skip most of these are intended because formating
                // sometimes produces a valid line that does not contain a trade however if time allows going back over this to get the edge cass may
                // be beneficial
                try {
                    // stores the ticker in the temp transaction object
                    Temp.Ticker = TempTicker.toUpperCase();

                    // moves index to end of ticker
                    index = moveIndexToChar(currentLine, ')', index);
                    // moves index into the date
                    index = moveIndexToChar(currentLine, '/', index);
                    // the sale type is always 4 or 10 characters away from the date this depends on if it's a (partial) type
                    // not storing this info as im not sure if it would be usefull can come back and add it if needed
                    // sale type can be E S or P stands for Exchange Sale or Purchase
                    // not sure what to do with exchange types yet but there stored so we can pick how to process them later
                    Temp.SaleType = getSaleType(currentLine, index);
                    // moves the index back to the start of the date
                    index -= 2;
                    // stores the date in the Temp Transaction
                    Temp.Date = (getString(currentLine, ' ', index));
                    // moves Index to start of the minmum price range
                    index = moveIndexToChar(currentLine, '$', index);
                    // moves index forward 1 space into the actual price
                    index++;
                    // stores minimum of price is -1 if invalid price
                    Temp.PurchaseMin = getPrice(currentLine,index);
                    // moves to next dollar value
                    index = moveIndexToChar(currentLine, '$', index);
                    // moves index forward 1 space
                    index++;
                    // stores maximum of price is -1 if invalid price
                    Temp.PurchaseMax = getPrice(currentLine,index);
                    // ads price range to temp transaction

                    // stores transaction in congressperson transaction list

                    if (congress.get(name).transactions.containsKey(TempTicker)){
                        ArrayList<Transaction> tempTransactions = congress.get(name).transactions.get(TempTicker);
                        tempTransactions.add(Temp);
                        congress.get(name).transactions.replace(TempTicker,tempTransactions);
                    } else {
                        ArrayList<Transaction> tempTransactions = new ArrayList<>();
                        tempTransactions.add(Temp);
                        congress.get(name).transactions.put(TempTicker,tempTransactions);
                        congress.get(name).StockTickers.add(TempTicker);
                    }



                } catch (StringIndexOutOfBoundsException e){
                    // catch all for lines that create an error because they don't contain the correct formating

                }
            }

        }

    }



    // returns the sales type purchase is true selling is false

    /**
     * returns if sale or purchase
     * @param currentLine line being parsed
     * @param index were to start
     * @return returns s if sale p if purchase e if Exchange
     */
    public char getSaleType(String currentLine,int index){
        // moves index to the correct space
        index -= 4;
        // if transaction is a partial one moves index further to get to the actual space
        if (currentLine.charAt(index) == ')'){
            index -= 10;
        }
        // returns the transaction type P S or E
        // purchase sale or exchange
        return  currentLine.charAt(index);
    }


    // checks if a line is a stock trade and not another asset or invalid line

    /**
     * checks if line is a stock trade
     * @param currentLine string being parsed
     * @return true if stock trade false if not
     */
    public Boolean isStockTrade(String currentLine){


        // stores index for were trade type ends
        int Index = 0;
        // moves to the start of the trade line characters
        if (currentLine.contains("[")){
            //moves to end of transactions
            while (currentLine.charAt(Index) != '['){
                Index++;
            }
            Index++;

            // checks if transaction matches stock trade id ST
            return currentLine.startsWith("ST", Index);

        }
        // returns false if transaction is note a stock trade
        return false;

    }

    // gets the next unprocessed transaction string
    // it's all added to one line because variations in the documents made it easier to parse on large string rather than several split up ones

    /**
     * combines several scanner line to get next line to be parsed
      * @param scan scanner object
     * @param line current line
     * @return string with several scanner lines merged
     */
    public  String getNextLine(Scanner scan,String line){

        // used to combine all lines were the transaction info is stores
        StringBuilder combinedLine = new StringBuilder();
        // appends first line that contained a valid transaction identifier normally () or SP
        combinedLine.append(line);
        // used to make sure loop does not try and create a mega line
        int count = 0;
        // loops through document until out of lines, or it completes a transaction line
        while (!line.contains("s: New")&&scan.hasNext()){
            // Edge case 2 common terms found in invalid lines that the parser logic try to run
            // believe I have fixed the need for this case however commited out so I can re add if removal proves to be a problem
                /*
                if(line.toUpperCase().contains("OWNER: JT")||line.toUpperCase().contains("OWNER: SP")){
                    combinedLine.append(line);
                    return "bad line";

                }
                */
            // edge case for invalid lines some congresspeople like giving descriptions about the transaction that sometimes includes
            // the ticker name resulting in an invalid duplicate of the transaction or an error when it attempts to parse data
            // from the resulting line
            if (line.toUpperCase().contains("DESCRIPTION:")||line.toUpperCase().contains("COMMENTS:")||line.toUpperCase().contains("FILING ID")){
                // just a line that I know the parse won't attempt to get data from
                return  "bad Line description";
            }
            // used to find end of transaction most end with new
            // deleted used for some of the weird documents may filter out those transactions
            // haven't decided yet
            if (line.contains(": Deleted")||line.contains(": New")){
                combinedLine.append(line);
                return combinedLine.toString();
            }
            // advances the loop to the next line and adds line to the combined string
            line = scan.nextLine();
            combinedLine.append(line);
            count++;
        }

        // returns the entire transaction line
        return combinedLine.toString();

    }


    // returns the plain text of the pdf for some reason this has some variability that I believe has been accounted for in the text parser

    /**
     * gets the string version of a pdf
      * @param Path path to pdf
     * @return string version of pdf
     * @throws IOException
     */
    public String ReturnPDFText(String Path) throws IOException {

        // loads path to pdf
        File pdfFile = new File(Path);

        // opens pdf
        PDDocument document = PDDocument.load(pdfFile);

        // strips text from pdf
        PDFTextStripper pdfStripper = new PDFTextStripper();
        // stores pdf as a string
        String text = pdfStripper.getText(document);

        // closes the pdf document for resource management
        document.close();

        // returns the pdf string
        return text;

    }

    /**
     * converts jsons of transactions into a transaction list and adds to congress
     * @param stockTickers list of stock tickers
     * @param congress congress map
     * @param name name of congress member
     */
    public void deSerlizeTransaction(ArrayList<String> stockTickers, HashMap<String,CongressMember> congress, String name) {

        System.out.println(name);

        for (int i = 0; i < stockTickers.size(); i++) {

            File file = new File("Json/" + name + "/Transactions/" + stockTickers.get(i) + ".json");
            String json = getJsonTextFromPath(file);

            Gson gson = new Gson();

            json = json.replace("},{", "}~{");


            String[] splitJson = json.split("~");
            splitJson[0] = splitJson[0].substring(1);
            int endIndex = splitJson.length - 1;
            splitJson[endIndex] = splitJson[endIndex].substring(0, splitJson[endIndex].length() - 1);


            for (int j = 0; j < splitJson.length; j++) {
                Transaction temp = gson.fromJson(splitJson[j], Transaction.class);


                if (congress.get(name).transactions.isEmpty()) {
                    ArrayList<Transaction> TemTransaction = new ArrayList<>();
                    TemTransaction.add(temp);
                    congress.get(name).transactions.put(stockTickers.get(i), TemTransaction);
                } else {


                    HashMap<String, ArrayList<Transaction>> TemMap = congress.get(name).transactions;
                    if (TemMap.containsKey(stockTickers.get(i))) {

                        ArrayList<Transaction> TemTransaction = TemMap.get(stockTickers.get(i));
                        TemTransaction.add(temp);
                        TemMap.replace(stockTickers.get(i), TemTransaction);
                        congress.get(name).transactions = TemMap;


                    } else {


                        ArrayList<Transaction> TemTransaction = new ArrayList<>();
                        TemTransaction.add(temp);
                        TemMap.put(stockTickers.get(i), TemTransaction);
                        congress.get(name).transactions = TemMap;


                    }


                    }
                }


            }


        }

    /**
     * loads the price data object for the transactions of a congress member
     * @param name name of congress member
     * @param congress congress map
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public void loadPriceDataFromTransactions(String name, HashMap<String,CongressMember> congress) throws MalformedURLException, URISyntaxException {


        for (int i=0;i < congress.get(name).StockTickers.size();i++){



            String stockTiker = congress.get(name).StockTickers.get(i);
            String json = getJsonTextFromUrl(stockTiker);
            if (!json.isEmpty()) {
                for (int j = 0; j < congress.get(name).transactions.get(stockTiker).size(); j++) {
                    String date = congress.get(name).transactions.get(stockTiker).get(j).Date;
                    date = convertDate(date);
                    PriceData temp = getPriceforDate(json, date);

                    congress.get(name).transactions.get(stockTiker).get(j).priceData = temp;



                }
            } else {

                congress.get(name).transactions.remove(stockTiker);
                congress.get(name).StockTickers.remove(stockTiker);


            }




        }

    }

    /**
     * converts a date into the format needed to get info from stock website
     * @param date original date
     * @return altered date
     */
    public String convertDate(String date){
        String[] splitDate = date.split("/");
        if (splitDate[1].length() < 2){
            splitDate[1] = "0"+splitDate[1];
        }
        if (splitDate.length > 2) {
            return splitDate[2] + "-" + splitDate[0] + "-" + splitDate[1];
        }
        return "";
    }

    /**
     * returns the string version of a json file for a stock ticker for a url
     * @param Ticker stock getting data for
     * @return string version of json
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public String getJsonTextFromUrl(String Ticker) throws MalformedURLException, URISyntaxException {
        String url = "https://api.stockanalysis.com/api/symbol/s/"+Ticker.toUpperCase()+"/history?range=10Y&p";
        return getJsonText(url);
    }


    /**
     * gets the price data object from a json for a date
     * @param json json string text
     * @param Date date being grabbed
     * @return
     */
    public PriceData getPriceforDate(String json,String Date){
        PriceData price = null;

        StringBuilder jsonLine = new StringBuilder();
        //{"t":"2025-02-04"
        if (json.contains("{\"t\":\"" + Date + "\"")) {
            int Index = json.indexOf("{\"t\":\"" + Date + "\"");
            while (json.charAt(Index) != '}') {

                jsonLine.append(json.charAt(Index));
                Index++;
            }
            jsonLine.append(json.charAt(Index));

        }
        price = deSerialize(jsonLine.toString());




        return price;
    }

    /**
     * gets the price data from the string form a json
     * @param json string being grabbed from
     * @return pricedata object from string
     */
    public PriceData deSerialize(String json){

        PriceData Temp = null;
        Gson gson = new Gson();
        try {
            Temp = gson.fromJson(json, PriceData.class);
        }
        catch (Exception e){

        }
        return Temp;
    }


    /**
     * gets the json text from a url
     * @param urlString path of url
     * @return string version of json
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public String getJsonText(String urlString) throws URISyntaxException, MalformedURLException {

        String JsonText = "";
        try {
            URL url = new URI(urlString).toURL();
            InputStream is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            JsonText = reader.readLine();


            is.close();

        } catch (Exception e){
        }


        return  JsonText;
    }

    /**
     * serilizes a object into a json string
     * @param object object being serilized
     * @return string version of json
     * @param <T> a object type to serilize
     */
    public <T> String serialize(T object){
        Gson gson = new Gson();
        return gson.toJson(object);


    }


    /**
     * takes transactions for a congress member and turns them into json files
     * @param name congress member being converted
     * @param congress congress map
     * @throws IOException
     */
    public void serilizeJsonFromTransactions(String name, HashMap<String,CongressMember> congress) throws IOException {



        StringBuilder TransactionsJson = new StringBuilder();
        StringBuilder StockTickersJson = new StringBuilder();

        StockTickersJson.append("[");
        Files.createDirectories(Paths.get("Json/"+name+"/Transactions"));
        for (int i=0;i < congress.get(name).StockTickers.size();i++){

            TransactionsJson.append("[");


            String StockTicker = congress.get(name).StockTickers.get(i);
            Name Temp = new Name(StockTicker);




            if (congress.get(name).transactions.get(StockTicker).getFirst().priceData != null) {
                for (int j = 0; j < congress.get(name).transactions.get(StockTicker).size(); j++) {



                    String temp = serialize(congress.get(name).transactions.get(StockTicker).get(j));
                    if (temp != null) {
                        TransactionsJson.append(serialize(congress.get(name).transactions.get(StockTicker).get(j)));
                        if (j != congress.get(name).transactions.get(StockTicker).size() - 1) {
                            TransactionsJson.append(",");
                        }
                    }


                }
                if (!TransactionsJson.toString().equals("[")) {

                    StockTickersJson.append(serialize(Temp));


                    StockTickersJson.append(",");


                    PrintWriter TrasactionsOut = new PrintWriter("Json/" + name + "/Transactions/" + StockTicker + ".json");
                    if (TransactionsJson.charAt(TransactionsJson.toString().length() - 1) == ',') {

                        TransactionsJson.deleteCharAt(TransactionsJson.toString().length() - 1);
                    }
                    TransactionsJson.append("]");
                    TrasactionsOut.println(TransactionsJson.toString());
                    TrasactionsOut.close();
                }

            }
            TransactionsJson = new StringBuilder();


        }


        if (!StockTickersJson.toString().equals("[")) {
            PrintWriter StockTickersOut = new PrintWriter("Json/" + name + "/" + name + ".json");
            StockTickersJson.deleteCharAt(StockTickersJson.toString().length() - 1);
            StockTickersJson.append("]");
            StockTickersOut.println(StockTickersJson.toString());
            StockTickersOut.close();
        }  else {

            congress.remove(name);


        }



    }

    /**
     * gets a list of congress names from all filling zips
     * @return returns list of congress names
     * @throws FileNotFoundException
     */
    public  ArrayList<String> extractFullNameListFromAllZips () throws FileNotFoundException {
        ArrayList<String> names = new ArrayList<>();
        int currentYear = Year.now().getValue();
        int NumberOfFiles = currentYear - 2016;

        // loops through all pdf files
        for (int i =16;i < NumberOfFiles+17;i++){

            //uses the data parser to extract required info and store it in the relevant Congressperson
            names = extractFullNameListFromZip("20"+i+"FD.xml",names);

        }
        return  names;
    }

    /**
     * checks if a date is closer to the current date then anouther
     * @param date1 first date being compared
     * @param date2 secod date being compared
     * @return true if date one is close false if not
     * @throws ParseException
     */
    public boolean isDatesCloserToPresent(String date1,String date2) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date dateOne = dateFormat.parse(date1);
        Date dateTwo = dateFormat.parse(date2);
        int closer = dateOne.compareTo(dateTwo);
        if (closer < 0){
            return  true;
        } else {
            return  false;
        }


    }

    /**
     * adds the latest transactions sense a date to a congress object
     * @param congress map of congress
     * @param date date being checked from
     * @throws FileNotFoundException
     * @throws ParseException
     */
    public void UpdateLatesetTransactionstoCongress(HashMap<String,CongressMember> congress,String date) throws FileNotFoundException, ParseException {

        //1/16/2025
        String[] SplitDate = date.split("/");

        int currentyear = Year.now().getValue();
        int years = currentyear - Integer.parseInt(SplitDate[2]);
        for (int i =0;i <= years;i++) {

            File file = new File("Zips/unzipped/" +  (currentyear - i) + "FD.xml");

            Scanner scan = new Scanner(file);
            //loops through file
            if (!date.equals(LocalDate.now().toString())) {
                while (scan.hasNext()) {

                    // stores next line in file to string
                    String line = scan.nextLine();
                    // checks for the last name tag in the line if found starts creating or updating a congress person
                    if (line.contains("Last")) {

                        // first makes a temp congress member


                        // gets the last name stores on current line
                        String name = getString(line, '<', 10);


                        line = moveXLines(scan, 2);

                        // if the filling is the correct type continues process
                        if (line.charAt(16) == 'P') {
                            line = moveXLines(scan, 2);
                            String docDate = getString(line, '<', 16);
                            if (isDatesCloserToPresent(date, docDate)) {
                                line = scan.nextLine();
                                String docID = getString(line, '<', 11);

                                PdfIdentifier identifier = new PdfIdentifier();
                                identifier.DocID = docID;
                                identifier.year = Integer.toString(currentyear - i);
                                if (congress.containsKey(name)) {
                                    congress.get(name).pdfIdentifiers.add(identifier);
                                    System.out.println("added :" + docDate);
                                } else {
                                    CongressMember Temp = new CongressMember();
                                    Temp.name = name;
                                    Temp.pdfIdentifiers.add(identifier);
                                    congress.put(name, Temp);
                                }

                            }

                        }


                    }


                }
            }

        }
    }

    /**
     * gets a list of congress names from a filling zip
     * @param fileName name of file
     * @param Names list of names
     * @return list of names
     * @throws FileNotFoundException
     */

    public ArrayList<String> extractFullNameListFromZip(String fileName,ArrayList<String> Names) throws FileNotFoundException {
        ArrayList<String> names = Names;
        // stores the path to the xml document that has all fillings listed for a year
        File file = new File("Zips/unzipped/"+ fileName);
        // scanner used to go through document
        Scanner scan = new Scanner(file);
        //loops through file
        while (scan.hasNext()){

            // stores next line in file to string
            String line = scan.nextLine();
            // checks for the last name tag in the line if found starts creating or updating a congress person
            if (line.contains("Last")){

                // first makes a temp congress member


                // gets the last name stores on current line
                String name = getString(line,'<',10);


                line = moveXLines(scan,2);

                // if the filling is the correct type continues process
                if (line.charAt(16) == 'P'){
                    if (!names.contains(name)){
                        names.add(name);
                    }
                }

                // moves scanner lines to the next line needed
                line = moveXLines(scan,8);



            }


        }
        return names;
    }

    /**
     * gets the pdf data for only the pdf identfiers not all pdfs in a directory
     * @param name name of congress member
     * @param congress map of congress
     * @return
     */
    public HashMap<String, ArrayList<Transaction>> loadPDFDataFromPDfIdentifiers(String name, HashMap<String,CongressMember> congress) {



        for (int i = 0;i < congress.get(name).pdfIdentifiers.size();i++){

            try {

                int count =0;
                // loops through all PDF files in lastname directory


                    String docId = congress.get(name).pdfIdentifiers.get(i).DocID;
                    File filePath = new File("PDF/"+name+"/"+docId+".pdf");
                    System.out.println(filePath.toString());

                    // returns the string text of the pdf
                    String pdfText = ReturnPDFText(filePath.toString());
                    // used to get transactions from PDF string and add them to a congressperson transaction list
                    count++;
                    getStockDataFromPDFText(pdfText, congress, name);


                } catch (Exception e) {
                throw new RuntimeException(e);
            }





        }

        return null;
    }
}




