import java.util.Scanner;

class Solution{
    public static void main(String[] args){
        
        Scanner sc = new Scanner(System.in);
        
        int num = 1234;
        int rev = 0;
        int last = 0;
        while(num!=0){
            last = num%10;
            rev=rev*10+last;
            num=num/10;
            
        }
        
        System.out.println(rev);
        
    }
}
