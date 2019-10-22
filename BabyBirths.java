/**
 * Processes baby name data and gives results like "Based on my name's rank in 2014, I would've been named _____ in 1980"
 * 
 * @author Sasa Schwartz
 */
import edu.duke.*;
import org.apache.commons.csv.*;
import java.io.File;

public class BabyBirths {    
    
    public int getRank (int year, String name, String gender) {
        // SUMMARY: returns rank of name in file with given gender, or -1 if no such name exists
        String yearStr = Integer.toString(year);
        // Access file for given year
        FileResource fr = new FileResource("data/yob" + yearStr + ".csv");
        // Iterate through recs, track rank (for same-gendered babies), until you find matching name
        int rank = 1;
        for (CSVRecord rec : fr.getCSVParser(false)) {
            if (rec.get(0).equals(name) && rec.get(1).equals(gender)) {
                // If the rec's name and gender match the givens
                return rank;
            }
            if (rec.get(1).equals(gender)) {
                // Otherwise, if gender matches, increase rank
                rank += 1;
            } 
        }
        return -1;
    }
    
    public String getName(int year, int rank, String gender) {
        // SUMMARY: Returns name with given rank and gender, or "NO NAME"
        String yearStr = Integer.toString(year);
        FileResource fr = new FileResource("data/yob" + yearStr + ".csv");
        // Iterate through recs of same gender and getRank on each name until rank matches search
        for (CSVRecord rec : fr.getCSVParser(false)) {
            // If rec's gender matches given, get its name and rank
            if (rec.get(1).equals(gender)) {
                String currName = rec.get(0);
                int currRank = getRank(year, currName, gender);
                // Break loop if rank equals the given search
                if (currRank == rank) {
                    return currName;
                }
            }
        }
        return "NO NAME";
    }
    
    public void whatIsNameInYear(String name, int year, int newYear, String gender) {
        // SUMMARY: determines what name you would be in a different year based on your name's rank
        // Get your rank based on given parameters
        int rank = getRank(year, name, gender);
        // Get name in new year with current rank
        String newName = getName(newYear, rank, gender);
        System.out.println(name + " born in " + year + " would be " + newName + " if born in " + newYear + ".");
    }
    
    public StorageResource getDatesManyFiles (String name, String gender) {
        // Processes multiple years' files and returns the dates as StorageResource, as long as files end with YYYY.csv
        DirectoryResource dr = new DirectoryResource();
        StorageResource dates = new StorageResource();
        String fileName = "";
        int length = 0;
        // Get year of each file and add it as string to a storage resource container
        for (File f : dr.selectedFiles()) {
            fileName = f.getName();
            length = fileName.length();
            String currDate = fileName.substring((length-8), (length-4));
            dates.add(currDate);
        }
        return dates;
    }
    
    public int yearOfHighestRank(String name, String gender) {
        // SUMMARY: Processes multiple years' files and returns the year with highest rank for given name and gender
        StorageResource dates = getDatesManyFiles(name, gender);
        int bestRank = -1;
        int bestYear = -1;
        int currentRank = -1;
        // Iterate through each year and get ranks for comparisons of highest rank
        for (String year : dates.data()) {
            int yearInt = Integer.parseInt(year);
            currentRank = getRank(yearInt, name, gender);
            if (currentRank == -1) {
                // If name isn't contained in file, return -1
                return -1;
            } else if ((bestRank == -1) || (currentRank < bestRank)) {
                // If bestRank has yet to be set or currentRank is better than running bestRank
                // Update bestRank
                bestRank = currentRank;
                // Update bestYear
                bestYear = yearInt;
            }
        }
        return bestYear;
    }
    
    public double getAverageRank (String name, String gender) {
        // Processes multiple files and returns average rank for given name and gender
        StorageResource dates = getDatesManyFiles(name, gender);
        int rankSum = 0;
        int fileCount = 0;
        // Interate through each year, get rank, and add rank to sum (unless the name isn't contained in a file)
        for (String year : dates.data()) {
            int yearInt = Integer.parseInt(year);
            int currentRank = getRank(yearInt, name, gender);
            if (currentRank == -1) {
                // If name isn't contained in file, return -1
                return -1;
            } else {
                rankSum += currentRank;
                fileCount += 1;
            }
        }
        double rankSumDb = rankSum;
        double fileCountDb = fileCount;
        double averageRank = (rankSumDb / fileCountDb);
        return averageRank;
    }
    
    public int getTotalBirthsRankedHigher (int year, String name, String gender) {
        // SUMMARY: Returns total number of births of all same-gendered babies who ranked higher than given name
        // Access file for given year
        String yearStr = Integer.toString(year);
        FileResource fr = new FileResource("data/yob" + yearStr + ".csv");        
        // Calculate number of babies born with given name
        int numBorn = 0;
        int higherRanked = 0;
        for (CSVRecord rec : fr.getCSVParser(false)) {
            if (rec.get(0).equals(name) && rec.get(1).equals(gender)) {
                numBorn = Integer.parseInt(rec.get(2));
            }
        }
        // Iterate through recs and update how many total babies born that rank higher (for same-gendered babies), if name existed
        if (numBorn > 0) {
            for (CSVRecord rec : fr.getCSVParser(false)) {
                int currNumBorn = Integer.parseInt(rec.get(2));
                if (rec.get(1).equals(gender) && (currNumBorn > numBorn)) {
                    higherRanked += currNumBorn;
                }
            }
            return higherRanked;
        } else {
            return -1;
        }
    }
    
    public void testMethods () {
        whatIsNameInYear("Sarah", 1994, 2014, "F");
        whatIsNameInYear("Matt", 1997 ,2013, "M");
        //
        int bestYear = yearOfHighestRank("Emma", "F");
        System.out.println("Best year for chosen name and gender was " + bestYear);
        //
        double averageRank = getAverageRank("Emma", "F");
        System.out.println("Average rank for Emma, gender F, over chosen years was " + averageRank);
        //
        int higherRanked = getTotalBirthsRankedHigher(2014, "Olivia", "F");
        System.out.println("There were " + higherRanked + " babies born of the same gender but higher ranked than Olivia in 2014");
    }
}

