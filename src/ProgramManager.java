import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProgramManager
/*
* this Class is used to handle loading running the functions of program via Manager classes
* Examples File Manager
* UIManager etc.
* the idea being all tasks are put in sections to organize them
* if further organization is needed. use a file name like JsonParser to describe what is being put in a lower tier file.
* manager names should be descriptive of what it manages and function in it should be related to that.
* if another manager needs something from a different manger, it should be called
* as a function that returns the necessary date
*
* example, if ui manager wants a list of names from CongressManager, it should be returned as a list
*
* */

{
    // manager object declarations go here there should never be more than one of any manager object
    // as its meant to process all data of its type and is where all data of its type will be stored
    FileManager fileManager = new FileManager();
    HashMap<String,CongressMember> congress = new HashMap<>();


    /*
    * this constructor will start up the program should run initial
    * checks to make sure data exists and run appropriates functions
    * after it will start the application loop
    * */
    ProgramManager() throws IOException, URISyntaxException, ParseException {

        // the first thing code does is verifiers any files it may need
        setupStartUpFiles();

        // next, it updates congress data if needed
        updateCongressData();


        // were the start of the program loop and ui goes
        
        

    }

    // updates congress data and JSON to include the latest stock information and any
    // new reported transactions

    private void updateCongressData() throws IOException, ParseException, URISyntaxException {


        // deletes all PDF identifiers in the congress list given
        fileManager.ResetPDfIdentifiers(congress);
        // gets a list of all transaction sense last updated this is determined via a JSON in the
        // JSON folder with the date of last run
        fileManager.updateLatestTransactionsToCongress(congress);


        // a fake list of congress members for the members that need to be updated
        HashMap<String,CongressMember> FakeCongress = new HashMap<>();
        // used to check if any files got updated
        boolean updated = false;



        // loops through all congress members looking for PDF identifiers
        for(String key:congress.keySet()){
            // if a congress member has a PDF that needs to be parsed and has not yet been updated by the startup function
            // loads new transactions and replaces JSON files
            if (!congress.get(key).pdfIdentifiers.isEmpty() && !congress.get(key).FilesReloaded){
                // marks that the code has updates at least one congress member
                updated = true;
                // downloads any new PDF files needed for transaction parsing
                fileManager.downloadPDFFor(key, congress);

                // creates a temp congress member for data manipulation
                CongressMember Temp = new CongressMember();
                //creates a copy of all the data needed had to do this because if a full copy is not made separate
                // from the original congress member, it results in the transaction being added infinitely
                Temp.name = key;
                Temp.transactions = new HashMap<>();
                Temp.StockTickers = new ArrayList<>();
                Temp.pdfIdentifiers = congress.get(key).pdfIdentifiers;
                // stores temp member in a fake congress list
                FakeCongress.put(key,Temp);

                // loads base transaction information for all new transactions from the list of PDF identifiers
                HashMap<String,ArrayList<Transaction>> newTransactions = fileManager.loadPDFDataFromPDfIdentifiers(key, FakeCongress);
               // loads the new transactions into the fake congress member
                fileManager.loadPriceDataFromTransactions(key, FakeCongress);
                // adds the new transactions to the old congress member and creates a new one if not already present
                fileManager.addFakeCongressMemberToCongress(key,FakeCongress,congress);

                // generates the new JSON files from the transaction list
                fileManager.generateJsonFromTransactions(key, congress);
            }


        }
        if (updated) {
            // updates congress names JSON if any congress member was updates
            // needed to make sure new congress members are added to the list to be processed
            fileManager.setFinalCongressNamesJson(congress);

        }

        // stores the current date so code knows were to update from
        fileManager.setCurrentDateJson();






    }

    public void setupStartUpFiles() throws IOException, URISyntaxException {

        boolean loadedPDF = false;
        fileManager.updateDownloads();

        // this function should verify the file we use to store transaction
        // info for all the congress members we want it to
        // then if it does not exist, we will parse data from PDF to recreate it.
        // note getting data from PDF Takes A Long Time

        // step load in congress member names
        ArrayList<String> congressNames = fileManager.objectFromJson("Json/CongressNames.json");



        if (congressNames != null){
            // this loads creates the blank congress members for all the names in the JSON file
            fileManager.initializeCongressObject(congressNames,congress);
            // declares a list of stockTickers to be set later
            ArrayList<String> StockTickers = null;

            // loops through the congress members to be processed based of JSON file
            for (int i =0;i < congressNames.size();i++){



                // runs if a JSON file with SStock Ticker list is present.
                /*
                future note move if else block into function, so I can
                delete JSON if it is missing a transaction JSON and recall it so it would load PDF instead.
                 */
                    if (fileManager.doesFileExist("Json/" + congressNames.get(i) + "/" + congressNames.get(i) + ".json")) {

                        // deletes the list of PDF identifiers for the stock
                        // this prevents the updater from trying to load old transactions
                        congress.get(congressNames.get(i)).pdfIdentifiers = new ArrayList<>();
                        // loads in a list of stock tickers from a JSON file for this congress member if statement verifies this exists
                        StockTickers = fileManager.objectFromJson("Json/" + congressNames.get(i) + "/" + congressNames.get(i) + ".json");
                        // adds the list to this congress member
                        congress.get(congressNames.get(i)).StockTickers = StockTickers;

                        // loads in all the JSON files for the transactions of this congress member from the list
                        fileManager.deSerializeTransaction(StockTickers, congress, congressNames.get(i));

                    } else {

                        // runs if JSON files does not exist


                        // used to trigger the JSON names file updater so code doesn't attempt
                        // to run invalid congress members in the future
                        loadedPDF = true;
                        // downloads the PDF files for this congress member if they do not exist
                        fileManager.downloadPDFFor(congressNames.get(i), congress);
                        // deletes PDF identifiers so updater does not try to load transactions
                        congress.get(congressNames.get(i)).pdfIdentifiers = new ArrayList<>();
                        // loads the transaction Date price range and purchase type for all stocks
                        fileManager.loadPDFDataForCongressMember(congressNames.get(i), congress);
                        // gets the stock price on all transaction dates and stores it
                        fileManager.loadPriceDataFromTransactions(congressNames.get(i), congress);
                        // generates the necessary JSON files to prevent PDF from needing to be parsed in the future
                        fileManager.generateJsonFromTransactions(congressNames.get(i), congress);
                        // this boolean makes sure this congress member is not included in the updater because loading
                        // the PDF will load all transactions tell current date and the updater could then result in duplicate transactions
                        congress.get(congressNames.get(i)).FilesReloaded = true;


                    }

            }
        } else {
            // runs if JSON file for congress names does not exist
            congressNames = fileManager.extractFullNameListFromZips();
            // generates a list of all names from zips for transactions
            // note this will take a long time because it requires the reloading of all congress members without valid jsons including
            // ones with no valid transactions
            // generates a JSON file
            fileManager.setInitialCongressNames(congressNames);

            // reruns this function list should exist now
            setupStartUpFiles();

        }

        if (loadedPDF) {
            // used to update congress names JSON to exclude any congress members without transactions
            fileManager.setFinalCongressNamesJson(congress);
        }


    }

}
