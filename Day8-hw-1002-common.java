class Solution {
    public List<String> commonChars(String[] words) {
        // Create a list to store the common characters
        List<String> result = new ArrayList<>();
        
        // Iterate through each character from 'a' to 'z'
        for (char c = 'a'; c <= 'z'; c++) {
            // Initialize the minimum count for the current character to the maximum possible integer value
            int minCount = Integer.MAX_VALUE;
            
            // Iterate through each word
            for (String word : words) {
                int count = 0;
                // Count the occurrences of the current character in the current word
                for (char ch : word.toCharArray()) {
                    if (ch == c) {
                        count++;
                    }
                }
                // Update the minimum count for the current character
                minCount = Math.min(minCount, count);
                // If the minimum count becomes 0, no need to check further
                if (minCount == 0) {
                    break;
                }
            }
            
            // Add the current character to the result list the required number of times
            for (int i = 0; i < minCount; i++) {
                result.add(String.valueOf(c));
            }
        }
        
        return result;
    }
}
