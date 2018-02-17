import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.*;

public class LocalFile {

    private Path path;
    private String folderName;
    private String fileName;
    private Date lastScrape;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private URL url;
    private static final boolean PRECON;
    private static final boolean POSTCON;
    private static final boolean INVARIANT;

    private final Logger LOGGER = Logger.getLogger(LocalFile.class.getName());
    private FileHandler filehandler;

    //Design-by-Contract switches
    static{
        PRECON = true;
        POSTCON = true;
        INVARIANT = true;
    }

    public static void main(String[] args){
        LocalFile testfile = new LocalFile("./Downloads/whitepaper/substratum_whitepaper.pdf", "http://substratum.net/wp-content/uploads/2017/08/substratum_whitepaper.pdf");
        try {
            testfile.update(0.01, new LevenshteinSimilairty());
        } catch(IOException io){
            System.out.println("ERROR: File could not be accessed.");
        }
    }

    /*
    CONSTRUCTOR (Autor: Joel Barmettler, 17.02.2019)
    Sets file path and separates into direcotry and filename part
    Checks validity of URL
    Creates Folder if not existent, downloads initial file if not existent
     */
    public LocalFile(String path, String url){
        //PRECONDITION
        if(PRECON){
            assert StringUtils.countMatches(path, ".") == 1 : "Path links to one file";
            assert url.startsWith("http://") || url.startsWith("www.") || url.startsWith("https://"): "Valid URL";
            invariant();
        }

        //ROUTINE
        try{
            filehandler = new FileHandler("LocalFile_log.log", 1024*8, 1, true);
            LOGGER.addHandler(filehandler);
            SimpleFormatter formatter = new SimpleFormatter();
            filehandler.setFormatter(formatter);
            LOGGER.setLevel(Level.FINE);
            filehandler.setLevel(Level.INFO);
        }catch(IOException io){
            System.out.println("ERROR: Could not set logging handler to file");
            System.exit(1);
        }

        LOGGER.info("****************RESTART****************");
        this.path = Paths.get(path);

        try {
            this.url = new URL(url);
        } catch(MalformedURLException ml){
            LOGGER.severe("URL is invalid, make new request.");
            System.exit(1);
        }
        LOGGER.fine("URL was set to "+url);

        path = path.replace("//", "\\");
        path = path.replace("/", "\\");
        LOGGER.fine("Path is set to: "+path);

        String[] parts = path.split("\\\\");
        this.fileName = parts[parts.length-1];              //seperate path into parts "/" and save last one as filename

        StringBuilder folderParts = new StringBuilder("");
        for (int i = 0; i<parts.length-1; i++){
            folderParts.append(parts[i]+"\\");
        }

        folderName = folderParts.toString();
        LOGGER.fine("Directory was set to: "+folderName);
        LOGGER.fine("Filename was set to: "+fileName);

        File dir = new File(folderName);        //create folder from path if not already existing
        if(!dir.exists()){
            LOGGER.info("Directory does not exist, creating new one at "+folderName);
            dir.mkdir();
        }

        File file = new File(path);
        if(!file.exists()){                     //save initial file if not existing
            LOGGER.info("File does not exist, download and save it initially");
            try{
                download();
            } catch (IOException io){
                LOGGER.severe("Could not download file due to inaccessability/inexistence during runtime. Closing application.");
                System.exit(1);
            }

        }else{
            LOGGER.fine("File does exist, requesting initial file information");
            setInfo();                              //get informations about file
        }

        //POSTCONDITION
        if(POSTCON){
            assert this.path != null : "Path properly set";
        }
    }

    /*
        GET ONLINE CONTENT (Autor: Joel Barmettler, 17.02.2019)
        Get bytes array from url
        Downloads an online file to datastream and returns it as bytes array
     */
    private byte[] getOnlineContent() throws IOException{
        LOGGER.fine("Getting online content from url: "+url);
        InputStream in = url.openStream();
        LOGGER.fine("Successfully streamed data from online");
        return in.readAllBytes();

    }

    /*
        GET OFFLINE CONTENT (Autor: Joel Barmettler, 17.02.2019)
        Get bytes array from local path
        Opens up a local file and returns it as bytes array
     */
    private byte[] getOfflineContent() throws IOException{
        LOGGER.fine("Getting offline content from file at "+path);
        return Files.readAllBytes(path);
    }


    /*
        UPDATE FILE (Autor: Joel Barmettler, 17.02.2019)
        Creates string from online and offline bytes array
        Compares it using similarity function
        If strings do not match, archive old file and save new one
     */
    protected int update(double treshold, Similarity sim) throws IOException{
        LOGGER.fine("Requesting online Content");
        byte[] online = getOnlineContent();
        LOGGER.fine("Requesting offline Content");
        byte[] offline = getOfflineContent();

        if(online.length + offline.length > 1024 * 256 ){
            LOGGER.info("File bigger than "+ 258+"kB, setting Similarity function to simple similarity instead of chosen one.");
            sim = new SimpleSimilarity();
        }

        LOGGER.fine("Converting bytearrays to strings");
        String onString = new String(online);
        String offString = new String(offline);

        LOGGER.fine("Calculating similarity of strings");
        boolean similar = sim.similarity(onString, offString, treshold);
        if (!similar){
            LOGGER.info("Files do not match. Archiving and Updating File now.");
            int isArchived = archive();
            switch (isArchived){
                case 0:
                    LOGGER.info("Archiving completed, requesting new version now");
                    save(online);
                    break;
                case 1:
                    LOGGER.warning("Error while archiving, do not download file to prevent overwriting");
            }

            return(1);
        } else{
            LOGGER.info("Files do Still match, no action needed.");
            return(0);
        }
    }


    /*
        DOWNLOAD (Autor: Joel Barmettler, 17.02.2019)
        Overloaded save function if no bytes string is provided
     */
    protected void download() throws IOException{
        save(getOnlineContent());
    }


    /*
        DOWNLOAD BYTES ARRAY (Autor: Joel Barmettler, 17.02.2019)
        Download saves a bytes array to local file
        Set new file info after save
     */
    protected void save(byte[] bytes) throws  IOException{
        LOGGER.fine("Saving bytes array to local file");
        FileOutputStream stream = new FileOutputStream(path.toFile());
        stream.write(bytes);
        stream.close();
        setInfo();
    }

    /*
        ARCHIVE FILE (Autor: Joel Barmettler, 17.02.2019)
        Renames old file according to creational date
        Moves file to archive folder, create new one when needed
     */
    private int archive() {
        File directory = new File(folderName + "\\" + fileName.replace(".", "_"));
        LOGGER.fine("Archiving to folder"+directory.toString());
        if (!directory.exists()) {
            LOGGER.fine("Directory does not exist, creating one");
            boolean created = directory.mkdir();
            if (!created) {
                LOGGER.warning("Directory could not be created. Aborting archive process with error code 1.");
                return 1;
            }
        }
        File oldFile = new File(path.toString());
        String renamedFileName = fileName.replaceAll("\\.","_("+lastScrape.toString().replaceAll("\\:","-")+").");
        renamedFileName = renamedFileName.replaceAll("\\s+","_");
        String renamedFilePath = directory+"\\"+renamedFileName;
        File newFile = new File(renamedFilePath);
        LOGGER.info("Copy file from "+oldFile.toString()+" to "+newFile.toString());
        boolean isRenamed = oldFile.renameTo(newFile);              //renames AND moves file to new location!

        if(isRenamed){
            LOGGER.info("File successfully moved and archived");
        }else{
            LOGGER.warning("ERROR: File could not be moved. Returning with error code 1");
            return 1;
        }
        return 0;
    }


    /*
        SET FILE INFOS (Autor: Joel Barmettler, 17.02.2019)
        Read creational date out of newest file
        set new creational date instance variable
     */
    private void setInfo(){
        try {
            LOGGER.fine("Getting file Infos via Basic File Attributes");
            BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            lastScrape = dateFormat.parse(dateFormat.format(fileAttributes.creationTime().toMillis()));
        } catch (IOException io){
            LOGGER.warning("WARNING: LocalFile does not Exist");
            LOGGER.warning(io.getMessage());
            lastScrape = null;
        } catch (ParseException par){
            LOGGER.severe("ERROR: Could not parse FileTime to Date");
            LOGGER.warning(par.getMessage());
            lastScrape = null;
        }
    }

    protected final void invariant(){
        if(INVARIANT){
            assert true : "No invariant";
        }
    }


}
