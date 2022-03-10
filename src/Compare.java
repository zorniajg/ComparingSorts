import java.util.Random;

/**
 * Compare bin sort and quicksort on random data (bin sort way outperforms
 * quicksort), but then show that bin sort has dismal behavior--O(n^2)--on
 * certain data sets.
 * 
 * @author Jacob Zorniak
 * @version 10/2018
 */
public class Compare
{
    public static void main(String[] args)
    {
        int[] rand;
        int[] rand2;
        double startTime;
        Random random = new Random();

        System.out.println("Sorting arrays of random data.");
        System.out.println(String.format("   %s    %s   %s", "Size", "QSort", "Bin Sort"));
        // Create arrays of increasing size filled with random positive integers
        for (int i = 250000; i <= 64000000; i *= 2)
        {
            rand = new int[i];
            rand2 = new int[i];
            for (int j = 0; j < i; j++)
            {
                int randomValue = random.nextInt(Integer.MAX_VALUE);
                rand[j] = randomValue;
                rand2[j] = randomValue;
            }
            System.out.print(String.format("%8d", i));
            // Calculate time to complete a quick sort on the array
            startTime = System.nanoTime();
            qsort(rand);
            double quickTime = (System.nanoTime() - startTime) / 1000000000;
            // Print quick sort time
            System.out.print(String.format("%8.3f", quickTime));
            // Calculate time to complete a bin sort on the array
            startTime = System.nanoTime();
            binSort(rand2);
            double binTime = (System.nanoTime() - startTime) / 1000000000;
            // Print bin sort time or error message if incorrectly sorted
            if (isSorted(rand2))
                System.out.print(String.format("%9.3f\n", binTime));
            else
                System.err.print(String.format("Failed bin sorting an array of size %d.", i));
        }
        /*
         * Creating skewed value array to test the worst case for Bin Sort
         * (values clustered together in one bin)
         */
        int[] skewed = new int[250000];
        int randomValue = 0;
        skewed[0] = 250000;
        for (int i = 1; i < skewed.length; i++)
        {
            randomValue = random.nextInt(100);
            skewed[i] = randomValue;
        }
        System.out.println("\nBin sorting an array of 250000 skewed values ...");
        startTime = System.nanoTime();
        binSort(skewed);
        double skewedBinTime = (System.nanoTime() - startTime) / 1000000000;
        System.out.print(String.format("Sorted in %.3f seconds.", skewedBinTime));

    }

    /**
     * Determine whether an int array is sorted from least to greatest.
     * 
     * @param a the array tested
     * @return true iff a is sorted
     */
    public static boolean isSorted(int[] a)
    {
        for (int i = 1; i < a.length; i++)
            if (a[i] < a[i - 1])
                return false;
        return true;
    }

    /**
     * Sort an int array using bin sort.
     * 
     * @param a array to be sorted
     */
    public static void binSort(int[] a)
    {
        // If array is less than or equal to 500, use insertion sort
        if (a.length <= 500)
            insertionSort(a);
        else
        {
            // Find the number of bins
            int numBins;
            if (a.length % 128 == 0)
                numBins = a.length / 128;
            else
                numBins = (a.length / 128) + 1;

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            long range;
            int binRange;
            // Find min and max of the array
            for (int i = 0; i < a.length; i++)
            {
                if (a[i] > max)
                    max = a[i];
                if (a[i] < min)
                    min = a[i];
            }
            // Calculate range to find the bin range of every bin
            range = max - min;
            binRange = (int) (range / numBins) + 1;
            /*
             * Create an array of counters for each bin to determine how many
             * elements are in each
             */
            int[] count = new int[numBins];
            for (int i = 0; i < a.length; i++)
            {
                count[(a[i] - min) / binRange]++;
            }
            // Translate the count array to offsets for new array (bin array)
            int temp = 0;
            int offset = 0;
            for (int i = 0; i < count.length; i++)
            {
                temp = count[i];
                count[i] = offset;
                offset += temp;
                temp = count[i];
            }
            /*
             * Place the values from source array into the bin array, making it
             * a nearly sorted list
             */
            int[] bin = new int[a.length];
            for (int i = 0; i < a.length; i++)
            {
                int binPlacement = (int) ((a[i] - min) / binRange);
                bin[count[binPlacement]] = a[i];
                count[binPlacement]++;
            }
            /*
             * Insertion sort the bin array back into the source array, making
             * the source array a fully sorted list
             */
            for (int j = 1; j < a.length; j++)
            {
                int element = bin[j];
                int i = j;
                while (0 < i && element < a[i - 1])
                {
                    a[i] = a[i - 1];
                    i--;
                }
                a[i] = element;
            }
        }
    }

    /**
     * Sort an int array using insertion sort.
     * 
     * @param a the array sorted
     */
    public static void insertionSort(int[] a)
    {
        for (int j = 1; j < a.length; j++)
        {
            int element = a[j];
            int i = j;
            while (0 < i && element < a[i - 1])
            {
                a[i] = a[i - 1];
                i--;
            }
            a[i] = element;
        }
    } // insertionSort

    /**
     * Sort an int array using quicksort with the median-of-three improvement
     * and insertion sort for small sublists.
     * 
     * @param a the array sorted
     */
    public static void qsort(int[] a)
    {
        qsortSublist(a, 0, a.length - 1);
        insertionSort(a);
    }

    private static final int SMALL_SUBLIST = 22; // for qsort

    /**
     * Recursively sort a sub-list of an array using quicksort with the
     * median-of-three improvement and insertion sort for small sublists.
     * 
     * @param a the array containing the sub-list
     * @param lb index of the first element of the sub-list
     * @param ub index of the last element of the sub-list
     */
    private static void qsortSublist(int[] a, int lb, int ub)
    {
        // return if the list is small
        if (ub - lb <= SMALL_SUBLIST)
            return;

        // find and position three values, using the median as pivot
        int m = (ub + lb) / 2;
        if (a[m] < a[lb])
        {
            int tmp = a[m];
            a[m] = a[lb];
            a[lb] = tmp;
        }
        if (a[ub] < a[m])
        {
            int tmp = a[ub];
            a[ub] = a[m];
            a[m] = tmp;
        }
        if (a[m] < a[lb])
        {
            int tmp = a[m];
            a[m] = a[lb];
            a[lb] = tmp;
        }
        if (ub - lb < 3)
            return;

        // partition
        int pivot = a[m];
        int tmp = a[ub - 1];
        a[ub - 1] = a[m];
        a[m] = tmp;
        int i = lb;
        int j = ub - 1;
        while (i < j)
        {
            do
            {
                i++;
            }
            while (a[i] < pivot);
            do
            {
                j--;
            }
            while (lb < j && pivot < a[j]);
            tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        tmp = a[i];
        a[ub - 1] = a[j];
        a[j] = tmp;
        a[i] = pivot;

        // recursively sort the sublists
        qsortSublist(a, lb, i - 1);
        qsortSublist(a, i + 1, ub);

    } // qsortSublist

} // Compare