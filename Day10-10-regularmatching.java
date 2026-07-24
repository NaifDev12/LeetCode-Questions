class Solution {
    public boolean isMatch(String s, String p) {
        // Use a Boolean object array to represent three states: null (unvisited), true, false
        Boolean[][] memo = new Boolean[s.length() + 1][p.length() + 1];
        return checkMatch(0, 0, s, p, memo);
    }

    private boolean checkMatch(int i, int j, String s, String p, Boolean[][] memo) {
        // Return cached result if already computed
        if (memo[i][j] != null) {
            return memo[i][j];
        }

        // Base Case: If we consumed the entire pattern, string must also be fully consumed
        if (j == p.length()) {
            return i == s.length();
        }

        // Check if the current characters match
        boolean firstMatch = (i < s.length() && 
                             (p.charAt(j) == '.' || s.charAt(i) == p.charAt(j)));

        boolean result;
        
        // Handle '*' wildcard condition
        if (j + 1 < p.length() && p.charAt(j + 1) == '*') {
            result = (checkMatch(i, j + 2, s, p, memo) ||             // Choice 1: Match 0 times
                     (firstMatch && checkMatch(i + 1, j, s, p, memo))); // Choice 2: Match 1+ times
        } else {
            // No wildcard: standard step forward
            result = firstMatch && checkMatch(i + 1, j + 1, s, p, memo);
        }

        // Save result in cache before returning
        memo[i][j] = result;
        return result;
    }
}
