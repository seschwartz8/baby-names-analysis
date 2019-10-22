/**
 * Processes baby name data and gives results like "Based on my name's rank in 2014, I would've been named _____ in 1980"
 * 
 * @author Sasa Schwartz
 */
import edu.duke.*;
import org.apache.commons.csv.*;
import java.io.File;

public class BabyBirths {
    public void printNames () {
        // SUMMARY: prints baby names and info if at least 100 babies with that name were born
        FileResource fr = new FileResource();
            // Iterate over records in file, but with no header row ("false")
            for (CSVRecord rec : fr.getCSVParser(false)) {
                int numBorn = Integer.parseInt(rec.get(2));
                if (numBorn <= 100) {
                    System.out.println("Name: " + rec.get(0) +
                                       ", Gender: " + rec.get(1) +
                                       ", Num Born: " + rec.get(2));
                }
            }
    }
    
    public void totalBirths (FileResource fr) {
        // SUMMARY: calculates and prints total births and total girls and boys
        int totalBirths = 0;
        int totalBoys = 0;
        int totalGirls = 0;
        for (CSVRecord rec : fr.getCSVParser(false)) {
            int numBorn = Integer.parseInt(rec.get(2));
            totalBirths += numBorn;
            if (rec.get(1).equals("M")) {
                totalBoys += numBorn;
            } else {
                totalGirls += numBorn;
            }
        }
        System.out.println("total births = " + totalBirths);
        System.out.println("total girls = " + totalGirls);
        System.out.println("total boys = " + totalBoys);
    }
    
    public int totalGender (FileResource fr, String gender) {
        // SUMMARY: returns total births of given gender
        int totalGender = 0;
        for (CSVRecord rec : fr.getCSVParser(false)) {
            int numBorn = Integer.parseInt(rec.get(2));
            if (rec.get(1).equals("M") && (gender == "M")) {
                totalGender += numBorn;
            } else if (rec.get(1).equals("F") && (gender == "F")) {
                totalGender += numBorn;
            }
        }
        return totalGender;
    }
    
    public int getRank (int year, String name, String gender) {
        // SUMMARY: returns rank of name in file with given gender, or -1 if no such name exists
        String yearStr = Integer.toString(year);
        // Access file for given year
        FileResource fr = new FileResource("data/yob" + yearStr + ".csv");
        // Calculate number babies born with given name
        int numBorn = 0;
        int rank = 1;
        for (CSVRecord rec : fr.getCSVParser(false)) {
            // If the rec's name and gender match the givens assign number of babies to numBorn
            if (rec.get(0).equals(name) && rec.get(1).equals(gender)) {
                numBorn = Integer.parseInt(rec.get(2));
            }
            // Otherwise do nothing, leaving numBorn = 0
        }
        // Iterate through recs and update how many names rank higher (for same-gendered babies), if name existed
        if (numBorn > 0) {
            for (CSVRecord rec : fr.getCSVParser(false)) {
                int currNumBorn = Integer.parseInt(rec.get(2));
                // If rec's gender matches given and number of babies rank higher add 1 to rank
                if (rec.get(1).equals(gender) && (currNumBorn > numBorn)) {
                    rank += 1;
                }
            }
            return rank;
        } else {
            return -1;
        }
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
    
    public String getName(int year, int rank, String gender) {
        // SUMMARY: Returns name with given rank and gender, or "NO NAME"
        String yearStr = Integer.toString(year);
        FileResource fr = new FileResource("data/yob" + yearStr + ".csv");
        // Iterate through recs of same gender and getRank on each name until rank matches search
        String currName = "";
        int currRank = -1;
        for (CSVRecord rec : fr.getCSVParser(false)) {
            // If rec's gender matches given, get its rank
            if (rec.get(1).equals(gender)) {
                currName = rec.get(0);
                currRank = getRank(year, currName, gender);
                // Break loop if rank equals the given search
                if (currRank == rank) {
                    break;
                } else {
                    // If rank didn't equal given, reset currRank to -1 and continue loop
                    currRank = -1;
                }
            }
        }
        if (currRank == -1) {
            return "NO NAME";
        } else {
            return currName;
        }
    }
    
    public void whatIsNameInYear(String name, int year, int newYear, String gender) {
        // SUMMARY: determines what name you would be in a different year based on your name's rank
        // Get your rank based on given parameters
        int rank = getRank(year, name, gender);
        // Get name in new year with current rank
        String newName = getName(newYear, rank, gender);
        System.out.println(name + " born in " + year + " would be " + newName + " if born in " + newYear + ".");
    }
    
    public int yearOfHighestRank(String name, String gender) {
        // SUMMARY: Processes multiple years' files and returns the year with highest rank for given name and gender
        StorageResource dates = getDatesManyFiles(name, gender);
        int bestRank = -1;
        int currentRank = -1;
        int bestYear = -1;
        // Interate through each year and get ranks for comparisons of highest rank (unless the name isn't contained in a file)
        for (String year : dates.data()) {
            int yearInt = Integer.parseInt(year);
            currentRank = getRank(yearInt, name, gender);
            if (currentRank == -1) {
                bestYear = -1;
                break;
            } else if ((bestRank == -1) || (currentRank < bestRank)) {
                bestRank = currentRank;
                bestYear = yearInt;
            }
        }
        return bestYear;
    }
    
    public double getAverageRank (String name, String gender) {
        // Processes multiple files and returns average rank for given name and gender
        StorageResource dates = getDatesManyFiles(name, gender);
        double averageRank = 0.0;
        int currentRank = -1;
        int rankSum = 0;
        int fileCount = 0;
        // Interate through each year, get rank, and add rank to sum (unless the name isn't contained in a file)
        for (String year : dates.data()) {
            int yearInt = Integer.parseInt(year);
            currentRank = getRank(yearInt, name, gender);
            if (currentRank == -1) {
                averageRank = -1.0;
                break;
            } else {
                rankSum += currentRank;
                fileCount += 1;
            }
        }
        if (averageRank == -1.0) {
            return averageRank;
        } else {
            double rankSumDb = rankSum;
            double fileCountDb = fileCount;
            averageRank = (rankSumDb / fileCountDb);
            return averageRank;
        }
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
    
    public void testBirths () {
        FileResource fr = new FileResource("data/example-small.csv");
        totalBirths(fr);
        int results = totalGender(fr, "M");
        System.out.println("Number of boys: " + results);
    }
    
    public void testRank () {
        int rank = getRank(2010, "Emma", "F");
        System.out.println(rank);
    }
    
    public void testName () {
        String name = getName(2010, 1, "F");
        System.out.println(name);
    }
    
    public void testWhatIsNameInYear () {
        whatIsNameInYear("Sarah", 1994, 2014, "F");
        whatIsNameInYear("Matt", 1997 ,2014, "M");
    }
    
    public void testYearOfHighestRank () {
        int bestYear = yearOfHighestRank("Emma", "F");
        System.out.println("Best year for chosen name and gender was " + bestYear);
    }
    
    public void testGetAverageRank () {
        double averageRank = getAverageRank("Emma", "F");
        System.out.println("Average rank for Emma, gender F, over chosen years was " + averageRank);
    }
    
    public void testGetTotalBirthsRankedHigher () {
        int higherRanked = getTotalBirthsRankedHigher(2014, "Olivia", "F");
        System.out.println("There were " + higherRanked + " babies born of the same gender but higher ranked than Olivia in 2014");
    }
}

