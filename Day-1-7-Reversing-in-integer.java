class Solution {
    public int reverse(int x) {
        long rev = 0;//used for taking overflow in reversing scenario

        while (x != 0) {
            int digit = x % 10;// extract the last digit
            rev = rev * 10 + digit;//adds up the digit to the rev 
            x /= 10;// modifies the current number for remaing reversing process
        }

        if (rev > Integer.MAX_VALUE || rev < Integer.MIN_VALUE) {
            return 0;// checks whether the rev value is within the integer datatype range 
        }

        return (int) rev;
    }
}
