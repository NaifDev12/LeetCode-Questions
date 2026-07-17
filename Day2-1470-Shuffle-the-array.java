class Solution {
    public int[] shuffle(int[] nums, int n) {

        int[] result = new int[n*2];//creates the new array of size which is needed for the required output array.
        int index =0;
        for(int i = 0;i < n ; i++){

            result[index++]=nums[i];// settles the array for the first group.
            result[index++]=nums[i+n];// setlles array for the secod group 

        }
        return result;
        
    }
}
