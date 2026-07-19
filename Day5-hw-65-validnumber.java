public class Solution {
    public boolean isNumber(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        
        s = s.trim();
        boolean isDigitSeen = false;
        boolean isExpSeen = false;
        boolean isDotSeen = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                isDigitSeen = true;
            } else if (c == '.') {
                // A dot is invalid if it's already seen or if it's seen after 'e'
                if (isDotSeen || isExpSeen) {
                    return false;
                }
                isDotSeen = true;
            } else if (c == 'e' || c == 'E') {
                // 'e' is invalid if already seen or if we haven't seen any digits before it
                if (isExpSeen || !isDigitSeen) {
                    return false;
                }
                isExpSeen = true;
                isDigitSeen = false; // Reset to ensure digits exist *after* the exponent
            } else if (c == '+' || c == '-') {
                // A sign is only valid if it's the first character or immediately follows 'e' or 'E'
                if (i != 0 && s.charAt(i - 1) != 'e' && s.charAt(i - 1) != 'E') {
                    return false;
                }
            } else {
                // Any other character is invalid
                return false;
            }
        }

        return isDigitSeen;
    }
}
