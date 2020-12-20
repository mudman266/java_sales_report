// Chapter 17
// Sales2020
// Josh Williams


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class Sales2020 {

    static int debugging = 0;
    static String filename = ".\\Sales2020.dat";

    public static void main (String[] args) throws IOException{
        // Check for more than 2 arguments
        if (args.length > 2) {
            System.out.println("Usage: Sales2020 MM/DD/YYYY XX.XX");
            System.exit(0);
        }
        // Check to see if a report is wanted.
        if (args.length < 2) {
            if (args.length == 0)   annualReport();
                else dayReport(args[0]);
        } else {
            // Update sales
            updateSales(args[0], args[1]);
        }
    if (debugging > 0) System.out.println("Done.");
    }

    static void annualReport() throws IOException {
        /** Displays an annual report - number of sales, total sales amount, and average sales for the year */

        // Verify the random access file exists.
        RandomAccessFile raf = checkFile();

        // Gather totals and number of days with a sale
        raf.seek(0);
        int numSales = raf.readInt();
        double totalSales = raf.readDouble();
        int daysWithSales = 0;
        for (int i = 1; i < 367;) {
            int j = raf.readInt();
            if (j > 0) daysWithSales++;
            // Move to the next record
            raf.seek(++i * 12);
        }

        // Calculate the average
        double avg = 0.0;
        if (daysWithSales != 0) {
            avg = totalSales / daysWithSales;
        }

        // Print report
        System.out.println("2020 Sales");
        System.out.printf("Number of Sales: %d\nDays with sales: %d\nTotal: $%.2f \nDaily Average Sale: $%.2f\n",
                numSales, daysWithSales, totalSales, avg);
    }

    static void dayReport(String dateString) throws IOException {
        /** Generates a report for a single day of sales for the dateString given. */

        RandomAccessFile raf = checkFile();
        if (checkDate(dateString)) {
            // Setup a GregorianCalendar object with the date passed in.
            String[] dateArray = dateString.split("/");
            GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(dateArray[2]),
                    Integer.parseInt(dateArray[0]) - 1, Integer.parseInt(dateArray[1]));

            // Find the amount of days from 1/1/2020 so we know where to look in the DB.
            int seekPos = (int) ((cal.getTimeInMillis() - new GregorianCalendar(2020, 0,
                    1).getTimeInMillis()) / 1000 / 60 / 60 / 24) + 1;

            // Move to the location and print our report
            raf.seek(seekPos * 12);
            if (debugging > 0) System.out.println("Seeking to record " + (seekPos + 1));
            int numSales = raf.readInt();
            double totalSales = raf.readDouble();
            double avg = 0;
            if (numSales > 0) {
                avg = totalSales / numSales;
            }
            System.out.printf("%d/%d/%d Sales\nNumber of Sales: %d\nTotal: $%.2f\nAverage sale: $%.2f\n",
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR), numSales,
                    totalSales, avg);
        }
    }

    static void updateSales(String date, String sales) throws IOException {
        /** Increases the overall number of sales by 1, daily number of sales for specified date by 1,
         *  overall sales by amount passed, and daily sales for specified date by amount passed.
         */

        RandomAccessFile raf = checkFile();
        // Verify amount
        try {
            double saleAmount = Double.parseDouble(sales);
            if (saleAmount < .01 || saleAmount > 99999.99) {
                System.out.print("Sales must be between $.01 and $99999.99. Exiting...");
                System.exit(1);
            }
        }
        catch (InputMismatchException | NumberFormatException e) {
            if (debugging > 0) e.printStackTrace();
            System.out.println("Error: Sales must be entered in a numerical format (XX.XX).");
            System.exit(1);
        }

        // Verify date.
        if (checkDate(date)) {
            // Inputs are valid. Add the sale.

            // Create a GregorianCalendar object from the date that was passed in.
            String[] dateArray = date.split("/");
            GregorianCalendar cal2 = new GregorianCalendar(Integer.parseInt(dateArray[2]),
                    Integer.parseInt(dateArray[0]) - 1, Integer.parseInt(dateArray[1]));

            // Update overall totals
            raf.seek(0);
            int totalNum = raf.readInt() + 1;
            double totalSales = raf.readDouble() + Double.parseDouble(sales);
            raf.seek(0);
            raf.writeInt(totalNum);
            raf.writeDouble(totalSales);

            // Update day totals
            int seekPos = (int) ((cal2.getTimeInMillis() - new GregorianCalendar(2020, 0,
                    1).getTimeInMillis()) / 1000 / 60 / 60 / 24) + 1;
            if (debugging > 0) System.out.println("seekPos: " + seekPos);
            raf.seek(seekPos * 12);
            int num = raf.readInt() + 1;
            double originalSales = raf.readDouble() + Double.parseDouble(sales);
            raf.seek(seekPos * 12);
            raf.writeInt(num);
            raf.writeDouble(originalSales);

            // Inform the user
            System.out.println("Sale added.");
        }
    }

    static boolean checkDate(String date) {
        /** Checks that a given date string can build a GregorianCalendar object and is formatted properly for our app. */

        GregorianCalendar cal = null;
        // Check date format
        try {
            String[] dateArray = date.split("/");
            // The GregorianCalendar class will handle negative and excessive values
            // but that is not valid input for our program.
            int month = Integer.parseInt(dateArray[0]);
            int day = Integer.parseInt(dateArray[1]);
            int year = Integer.parseInt(dateArray[2]);
            if (month < 1 || month > 12 || day < 1 || year != 2020 || day > new GregorianCalendar(year, month - 1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)) {
                System.out.println("Invalid date. Exiting...");
                System.exit(1);
            }
            // Check for only 3 parameters, month day year.
            if (dateArray.length != 3) {
                System.out.println("Invalid date format. Exiting...");
                System.exit(1);
            }
        }
        catch (NumberFormatException e) {
            if (debugging > 0) e.printStackTrace();
            System.out.println("Invalid date. Exiting...");
            System.exit(1);
        }
        // Set the calendar
        String[] dateArray = date.split("/");
        cal = new GregorianCalendar(Integer.parseInt(dateArray[2]),
                Integer.parseInt(dateArray[0]) - 1, Integer.parseInt(dateArray[1]));
        return true;
    }

    static RandomAccessFile checkFile() throws IOException {
        /** Verifies the file used for data storage exists and returns the RandomAccessFile. */

        // Check storage file existence
        File f = new File(filename);
        if (!f.exists()) {
            // File doesn't exist, create it
            System.out.println("The storage file at " + filename + " was not detected. Creating file...");
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            // Populate the file with 0's
            int j = 0;
            double k = 0.0;
            for (int i = 0; i < 367; i++) {
                raf.writeInt(j);
                raf.writeDouble(k);
            }
            return raf;
        } else {
        return new RandomAccessFile(f, "rw");
        }
    }
}