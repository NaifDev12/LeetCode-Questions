class Solution {
    public int findNumbers(int[] nums) {
        int count=0;
        for (int num : nums){// iterate through the array 

            if(num > 9 && num < 100 || num > 999 && num < 10000 || num == 100000){
                count++;// using the case where even digits are there its used and uasge of logical operators is used to check those conditions instead of using mutiple conditional statements
            }
        }

        return count;
    }
}
