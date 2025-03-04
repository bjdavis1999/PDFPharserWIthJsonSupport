import com.google.gson.Gson;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * used to manage files uses file parser to do retrieve data
 */
public class FileManager {
    FileParser fileParser = new FileParser();
    Downloader downloader = new Downloader();
    /*
    * manages all files main goal is to download files so the parser classes can get
    *  data from them.
    * All parser data should be retrieved through file manager
    * should not be used to process any data simple move and get files
    * */


    // used returns a string list from a JSON file used for congress names dates and stock tickers

    /**
     * takes a JSON files of object names (just a string) takes a path for were to get the file from
     * @param path
     * @return
     */
    public ArrayList<String> objectFromJson(String path) {
       // checks if the JSON exists
        if (doesFileExist(path)) {
            // returns the list
            return fileParser.congressNamesFromJson(path);
        }
        // returns null if file is missing
        return  null;
    }
    // checks if a file exists at a location
    public boolean doesFileExist(String filePath){
        //creates a file object referencing the path given
        File f = new File(filePath);
        // returns if it exists or not
        return  f.exists();

    }

    // takes a list of names and uses it to make blank congress members in the hashmap given also generates PDF identifiers

    /**
     *  makes the initial congress hashmap without any data just names
     * @param Names
     * @param congress
     * @throws IOException
     * @throws URISyntaxException
     */
    public void initializeCongressObject(ArrayList<String> Names, HashMap<String,CongressMember> congress) throws IOException, URISyntaxException {

        // loops through all names in list
        for (int i =0;i < Names.size();i++){
            // creates a temp congress member
            CongressMember Temp = new CongressMember();
            // sets the name to the one in the list
            Temp.name = Names.get(i);
            // adds it to the hash map
            congress.put(Names.get(i),Temp);
        }

        // extracts all PDF identifiers for the congress object
        fileParser.extractAllPdfFileNames(congress);


    }
    // unzips all the download zip files with the name correct name

    /**
     * unzip all file to get transaction filling id for all years in range
     * @throws IOException
     */
    public  void unzipAll() throws IOException {

        // there is one zip file for each year, so this code gets the current year and the number of times
        // the code need to unzip a file
        int currentYear = Year.now().getValue();
        int NumberOfFiles = currentYear - 2016;

        // loops through all zip files and runs the unzip function on all of them
        // starts at 14 so names match the correct year
        for (int i = 16; i <= NumberOfFiles+16;i++){
            unZip("Zips/20"+i+"FD.zip");
        }

    }
    // unzips a single file and puts the contents in a shared unzipped directory
    // takes a path object representing the location of the file to unzip

    /**
     * takes a path and unzips a file
     * @param Path
     * @throws IOException
     */

    private void unZip (String Path) throws IOException {

        // creates the Destination directory to store unzipped files
        File DestinationDir = new File("Zips/unzipped");


        // used to store data from an unzipped file before writing it to the directory
        // // makes things more efficient for large files
        byte[] buffer = new byte[1024];

        //used to read the data from the zip file
        ZipInputStream zis = new ZipInputStream(new FileInputStream(Path));

        // used to read and write zip files
        ZipEntry zipEntry = zis.getNextEntry();


        // runs until no files are left in the zipped file
        while (zipEntry != null){
            // stores a directory for the files
            File newFile = new File(DestinationDir,zipEntry.getName());
            // confirms directory exists and tries to make it if not
            if (zipEntry.isDirectory()){
                if (!newFile.isDirectory()  && !newFile.mkdirs()){
                    // error message if code fails to make directory
                    throw  new IOException("Failed to create directory 1");
                }
            } else {
                // used to try and make second directory if failed throws an error message
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()){
                    throw  new IOException("Failed to create directory 2");
                }

                // file output stream used to write the files to the new directory
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                //loops until all data is read from a zipped file
                while ((len = zis.read(buffer)) > 0){
                    // writes the bytes from the buffer to the output file
                    fos.write(buffer,0,len);
                }
                // used to close file output stream
                fos.close();
            }
            // gets next file to unzip from the file if none left are null
            zipEntry = zis.getNextEntry();

        }

        // resource management just closing unused Things
        zis.closeEntry();
        zis.close();
    }

    /**
     * downloads the zip files for filling id
     * @throws IOException
     * @throws URISyntaxException
     */

    public void updateDownloads() throws IOException, URISyntaxException {

        downloader.downloadAllZips();

        unzipAll();


    }

    // used to load function from file parser


    public void loadPDFDataForCongressMember(String Name,HashMap<String,CongressMember> congress) throws IOException {

        // loads in the base transactions from PDFS no price data
        fileParser.getCongressMemberTransactions(congress,Name);
    }

    // used to load function from file parser
    public void deSerializeTransaction(ArrayList<String> stockTickers, HashMap<String,CongressMember> congress, String name) {

        // used to load in JSON the file of transactions into a congress member
        fileParser.deSerlizeTransaction( stockTickers, congress,name);

    }

    // used to load function from file parser
    public void loadPriceDataFromTransactions(String names, HashMap<String,CongressMember> congress) throws MalformedURLException, URISyntaxException {

        // gets the price data from base transactions
        fileParser.loadPriceDataFromTransactions(names, congress);

    }


    /**
     * gets the lateset transactions from last run date adds them to a congress map
     * @param congress map of congress members
     * @throws FileNotFoundException
     * @throws ParseException
     */
    // gets a list off all PDF Identifiers sense last updated date
    public void updateLatestTransactionsToCongress(HashMap<String,CongressMember> congress) throws FileNotFoundException, ParseException {

        // Gson object used to deserialize date
        Gson gson = new Gson();
        // location of last date JSON
        File filePath = new File("Json/LastDateUpdated.json");
        // returns string of json object
        String json = fileParser.getJsonTextFromPath(filePath);
        // converts string to object
        Name dateObject = gson.fromJson(json, Name.class);
        // converts object to String date
        String date = dateObject.getName();

        // gets the transactions from date put in to the current date
        fileParser.UpdateLatesetTransactionstoCongress(congress,date);

    }

    // downloads PDF for a single congress member
    public void downloadPDFFor(String name, HashMap<String,CongressMember> congress) throws IOException, URISyntaxException {

        downloader.downloadAllPDF(name,congress);
    }

    // Generates a JSON file from full transactions
    public void generateJsonFromTransactions(String name, HashMap<String,CongressMember> congress) throws IOException {
        fileParser.serilizeJsonFromTransactions(name,congress);
    }

    // gets a list of all congress names regardless of the number of transactions
    public ArrayList<String> extractFullNameListFromZips() throws FileNotFoundException {


        return fileParser.extractFullNameListFromAllZips();


    }

    // sets the names of all congress members into a JSON file from a list

    /**
     * creates a json of congress names from list of names
     * @param CongressNames list of congress members
     * @throws FileNotFoundException
     */
    public  void setInitialCongressNames(ArrayList<String> CongressNames) throws FileNotFoundException {

        // stores final JSON file text
        StringBuilder Names = new StringBuilder();
        // start of the JSON file
        Names.append("[");

        // loops through all names in congress list
        for(int i = 0;i < CongressNames.size();i++){


            // adds the base string needed to store the name
                Names.append("{\"Name\":\"" + CongressNames.get(i) + "\"}");
                Names.append(",");



        }
        // removes the last comma so it does not create an error
        Names.deleteCharAt(Names.toString().length()-1);
        // end of json text bracket
        Names.append("]");
        // prints the JSON string out
        System.out.println(Names.toString());
        // Print Writer used to create the file
        PrintWriter out = new PrintWriter("Json/CongressNames.json");
        // adds Stringbuilder JSON text to file
        out.println(Names.toString());
        // closes Print Writer
        out.close();

    }

    // sets congress names from a congress object

    /**
     * sets a final json of congress names from congress map
     * @param congress map of congres smembers
     * @throws FileNotFoundException
     */
    public void setFinalCongressNamesJson(HashMap<String,CongressMember> congress) throws FileNotFoundException {

        // stores final JSON file text
        StringBuilder Names = new StringBuilder();
        // start of the JSON file
        Names.append("[");

        // loops through all names in congress object
        for(String key: congress.keySet()){





            // adds the base string needed to store the name
            Names.append("{\"Name\":\"" + key + "\"}");
            Names.append(",");



        }
        // removes the last comma so it does not create an error
        Names.deleteCharAt(Names.toString().length()-1);
        // end of json text bracket
        Names.append("]");
        // prints the JSON string out
        System.out.println(Names.toString());
        // Print Writer used to create the file
        PrintWriter out = new PrintWriter("Json/CongressNames.json");
        // adds Stringbuilder JSON text to file
        out.println(Names.toString());
        // closes Print Writer
        out.close();


    }

    // gets the base transactions from a PDF identifier for congress member
    public HashMap<String, ArrayList<Transaction>> loadPDFDataFromPDfIdentifiers(String name, HashMap<String,CongressMember> congress) {
        return fileParser.loadPDFDataFromPDfIdentifiers(name,congress);

    }

    // stores the current date in the JSON file for updating based of date

    /**
     * sets the current date to a json file so program knows last time it was run
     * @throws FileNotFoundException
     * @throws ParseException
     */
    public void setCurrentDateJson() throws FileNotFoundException, ParseException {


      // gets current date
            String unparsedDate = LocalDate.now().toString();
            // split date into YYYY DD MM So it can be arranged correctly
            String[] dateSplit = unparsedDate.split("-");
            // creates a for a JSON object with date in MM/DD/YYYY format
            String currentDate = "{\"Name\":\"" + dateSplit[1]+"/"+dateSplit[2]+"/"+dateSplit[0] + "\"}";
            System.out.println(currentDate);


            // used to create JSON file
            PrintWriter out = new PrintWriter("Json/LastDateUpdated.json");
            // adds date to file
            out.println(currentDate);
             // closes Print Writer
            out.close();
    }

    // deletes all pdf identifiers in a congress object

    /**
     * clears the pdf identfiers in a congress map
     * @param congress map of congress members
     */
    public void ResetPDfIdentifiers(HashMap<String,CongressMember> congress) {

        for(String keys:congress.keySet()){
            congress.get(keys).pdfIdentifiers = new ArrayList<>();
        }


    }


    // used to add new transactions to the main congress list

    /**
     * adds a congress member from one map to another congress map
     * @param name congress members name
     * @param fakeCongress congress map data is being taken from
     * @param congress congress map data is being added to
     */
    public void addFakeCongressMemberToCongress(String name,HashMap<String,CongressMember> fakeCongress, HashMap<String,CongressMember> congress) {

        // prints the name of the congress member being worked on
        System.out.println(name);
        // loops through all Stock Tickers for congress Member that need to be updated
        for (int i =0;i < fakeCongress.get(name).StockTickers.size();i++){
            // Sets current Stock Ticker to string
            String StockTicker = fakeCongress.get(name).StockTickers.get(i);
            // sets size of Stock Tickers Transaction
            int size = fakeCongress.get(name).transactions.get(StockTicker).size();
            // loops though all transactions for stock ticker
            for (int j =0; j < size;j++){
                // Loads the transaction into a temp object
                Transaction temp = fakeCongress.get(name).transactions.get(StockTicker).get(j);
                // checks if it has price data
                if (temp.priceData != null) {
                    // if congress member exists
                    if (congress.containsKey(name)) {
                        // if congress mem=ber has transactions for this stock
                        if (congress.get(name).StockTickers.contains(StockTicker)) {
                            // sets a list of all prior transactions
                            ArrayList<Transaction> tempArray = congress.get(name).transactions.get(StockTicker);
                            // adds new transaction to list
                            tempArray.add(temp);
                            // replaces the old list in the congress object
                            congress.get(name).transactions.replace(StockTicker, tempArray);
                        } else {
                            // if no transactions for stock ticker
                            // adds the stock ticker to list
                            congress.get(name).StockTickers.add(StockTicker);
                            // creates a new empty list of transactions for the stock
                            ArrayList<Transaction> tempArray = new ArrayList<>();
                            // adds new transaction to list
                            tempArray.add(temp);
                            // puts the list in the congress object
                            congress.get(name).transactions.put(StockTicker, tempArray);
                        }
                    } else {
                        // if a congress member does not exist yet,
                        // create a temp congress member
                        CongressMember tempCongressMember = new CongressMember();
                        // set there name
                        tempCongressMember.name = name;
                        // add the stock to the list of stocks they have transactions for
                        tempCongressMember.StockTickers.add(StockTicker);
                        // create a new array list of transactions for that stock
                        ArrayList<Transaction> tempArray = new ArrayList<>();
                        // add transaction to list
                        tempArray.add(temp);
                        // put the list in Temp congress member
                        tempCongressMember.transactions.put(StockTicker, tempArray);
                        // put congress member into the main congress object
                        congress.put(name, tempCongressMember);

                    }
                }


            }


        }

    }

    }



