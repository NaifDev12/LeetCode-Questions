class Solution {
    
    public boolean checkStraightLine(int[][] coordinates) {
        
        
        int xMove = coordinates[1][0] - coordinates[0][0];//slopes
        int yMove = coordinates[1][1] - coordinates[0][1];
        
        for(int i=1; i<coordinates.length; i++){

            int x = coordinates[i][0] - coordinates[i-1][0];//slopes
            int y = coordinates[i][1] - coordinates[i-1][1];
            

            
            if(y * xMove != x * yMove) return false;
            //checking the slope condition for line to be straight
        }
        
        return true;
    }
}
