public class Solution {
    public boolean isPowerOfTwo(int n) {
        for (int i = 0; i < 31; i++) //loopoing through a 31 integers coz int ize is appiacabe to 2 power 31
            int ans = (int) Math.pow(2, i);// checks the number if its power of 2
            if (ans == n) {
                return true;
            }
        }
        return false;
    }
}
