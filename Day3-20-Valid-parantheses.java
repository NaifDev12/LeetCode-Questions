import java.util.Stack;

class Solution {
    public boolean isValid(String s) {
        // Create a temporary stack to store opening brackets
        Stack<Character> stack = new Stack<>();
        
        // Loop through  character by character
        for(char ch : s.toCharArray()) {
            
            // If it is an opening bracket, save it on top of the stack
            if(ch == '[' || ch == '{' || ch == '(') {
                stack.push(ch);
            } 
            // If it is a closing bracket instead
            else {
                // Return false 
                if(stack.isEmpty()) { 
                    return false; 
                }
                
                // Remove and store the most recently saved opening bracket
                char top = stack.pop();
                
                // Return false if the closing bracket type does not match the opening type of bracket in the stack tht has been popped
                if(ch == ']' && top != '[') { return false; }
                if(ch == '}' && top != '{') { return false; }
                if(ch == ')' && top != '(') { return false; }
            }
        }
        
        // Return true only if all opening brackets found their perfect match and the stack is empty with according to the input
        return stack.isEmpty();
    }
}
