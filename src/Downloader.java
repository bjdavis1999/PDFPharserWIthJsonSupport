import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;


public class Downloader {





// loops through the download zip function to download all needed files
    void downloadAllZips() throws IOException, URISyntaxException {

        // used to figure out how many zips to download
        int currentYear = Year.now().getValue();
        int NumberOfYears = currentYear - 2015;

        // used to store the path to download the xip files to
        Path dirPath = Paths.get("Zips");

        //loops based of the number of years to download all the files
        for (int fileName = 15; fileName <= NumberOfYears + 15;fileName++){

            // downloads a xip file from the congress discolour site
            //file contains a list of all fillings for a specific year
            downloadZip(fileName,dirPath);
        }

    }

    // downloads a zip from the congress website for a given year file  contains a list of all fillings for a specific year
    private void downloadZip(int name,Path dirPath) throws IOException, URISyntaxException {

        // used to reference what to name a file and were to store it
        File zip = new File("Zips/20"+name+"FD.zip");
        if (!zip.exists()){


            // url for the zip files name coresponds to A int between 2014 and current year
            String url= "https://disclosures-clerk.house.gov/public_disc/financial-pdfs/20"+ name +"FD.zip" ;

            // uri object used to download from the url
            URI uri = new URI(url);
            // opens up the url in an input stream
            InputStream in = uri.toURL().openStream();

            // makes file path for zip
            Path filePath = dirPath.resolve("20" + name + "FD.zip");

            // stores file in path will replace file if already existing
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);

            // download message
            System.out.println("20" + name + "FD.zip downloaded");
        }

    }


    // downloads all pdf files using the data extracted from zips stores files in a folder based on the congresspersons last name
    void downloadAllPDF(String name, HashMap<String,CongressMember> congress) throws IOException, URISyntaxException {

        // stores the info of the document we are looking for
        // name of congressperson

        // year of filing
        String Year;
        // the id # of the document
        String DocID;

        //for(int i =0;i < congress.size()-1;i++){

            for (int j =0;j<congress.get(name).pdfIdentifiers.size();j++){
                Year = congress.get(name).pdfIdentifiers.get(j).year;
                DocID = congress.get(name).pdfIdentifiers.get(j).DocID;

                File pdf = new File("PDF/"+name+"/"+DocID+".pdf");
                // checks if file already exists only downloads if it does not
                if (!pdf.exists()){
                    // downloads the document using the download function
                    // takes a docid year and lastname
                    downloadPDF(DocID,Year,name);
                    // download message
                    System.out.println(name +" "+ Year+" "+ DocID );

                }

            }

        //}

    }




    // used to download a single pdf file using a file id and a year name just used to store it in the correct location
    private void downloadPDF(String DocID,String Year,String name) throws IOException, URISyntaxException {

        // stores path to folder to store files in
        Path dirPath = Paths.get("PDF/"+name);
        // creates the directory of the last name in the pdf folder
        Files.createDirectories(dirPath);

        // used to store the 2 possible urls the file can be found at
        // government making shit hard lol
        String[] urls = new String[2];

        // the 2 possible url in string format
        urls[0] =  "https://disclosures-clerk.house.gov/public_disc/ptr-pdfs/" + Year + "/" + DocID + ".pdf";
        urls[1] =  "https://disclosures-clerk.house.gov/public_disc/financial-pdfs/" + Year + "/" + DocID + ".pdf";


        // loops through all possible urls 2 will try them and spits out message if failed
        for (String url : urls){

            try {

                // uri object used to download pdf
                URI uri = new URI(url);
                // input stream used to get file
                InputStream in = uri.toURL().openStream();
                // path of file being stored and its name
                Path filePath = dirPath.resolve(DocID+".pdf");
                // downloads pdf and replaces existing file redundant sense if it exists you will never get to it
                // but needs to be in the function
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                // stops the loop if a link works
                return;
            } catch (IOException e){
                // failure message
                System.out.println("PDF Download Failed");
            }

        }

    }

}



