class Solution {
    public int divide(int dividend, int divisor) {
        // Handle overflow edge cases directly
        if (dividend == Integer.MIN_VALUE && divisor == -1) {
            return Integer.MAX_VALUE;
        }
        if (dividend == Integer.MIN_VALUE && divisor == 1) {
            return Integer.MIN_VALUE;
        }

        // Determine the sign of the quotient
        boolean negative = (dividend < 0) ^ (divisor < 0);

        // Convert to long to prevent overflow during Math.abs()
        long absDividend = Math.abs((long) dividend);
        long absDivisor = Math.abs((long) divisor);

        int quotient = 0;

        // Perform exponential subtraction using bit shifting
        while (absDividend >= absDivisor) {
            long tempDivisor = absDivisor;
            int multiple = 1;

            // Double the divisor and the multiple until it exceeds the dividend
            while (absDividend >= (tempDivisor << 1)) {
                tempDivisor <<= 1;
                multiple <<= 1;
            }

            // Subtract the largest found multiple and update the quotient
            absDividend -= tempDivisor;
            quotient += multiple;
        }

        return negative ? -quotient : quotient;
    }
}
