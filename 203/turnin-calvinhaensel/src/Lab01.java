public class Lab01
{
    public static void main(String[] args)
    {
        // declaring variables
        int x = 5;
        String y = "hello";
        float z = 9.8f;

        // printing the variables
        System.out.print("x: ");
        System.out.print(x);
        System.out.print(" y: ");
        System.out.print(y);
        System.out.print(" z: ");
        System.out.println(z);
        
        // an array
        int[] nums = {3, 6, -1, 2};
        for (int num : nums) {
            System.out.println(num);
        }        

        // call a function
        char l = 'l';
        int numFound = char_count(y, l);
        System.out.print("Found: ");
        System.out.println(numFound);

        // a counting for loop
        for (int i = 1; i < 11; i++) {
            System.out.print(i);
            System.out.print(" ");
        }
        System.out.println("");
    }
    public static int char_count(String s, char c)
    {
        int count = 0;
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == c) {
                count ++;
            }
            i ++;
        }
        return count;
    }
}
