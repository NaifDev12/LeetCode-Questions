class Solution {
    public int myAtoi(String str) {
        int index = 0, sign = 1, total = 0;
        
        // 1. Empty string check
        if (str == null || str.length() == 0) return 0;
        
        // 2. Remove Spaces (Fix: Prevent index out of bounds by checking index < length FIRST)
        while (index < str.length() && str.charAt(index) == ' ') {
            index++;
        }
        
        // Check if string only contained spaces
        if (index == str.length()) return 0;
        
        // 3. Handle signs (Fix: Change "-1;" string typo to integer -1)
        if (str.charAt(index) == '+' || str.charAt(index) == '-') {
            sign = str.charAt(index) == '+' ? 1 : -1;
            index++;
        }
        
        // 4. Convert number and avoid overflow
        while (index < str.length()) {
            int digit = str.charAt(index) - '0';
            if (digit < 0 || digit > 9) break;
            
            // Check if total will overflow
            if (Integer.MAX_VALUE / 10 < total || (Integer.MAX_VALUE / 10 == total && Integer.MAX_VALUE % 10 < digit)) {
                return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }
            
            total = 10 * total + digit;
            index++;
        }
        
        return total * sign;
    }
}
