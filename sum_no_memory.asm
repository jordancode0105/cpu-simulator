// Sum integers 1 through 100 without storing the integers in memory.
// Result ends in r1.
copy 10 r0
leftshift 3 r0
add 15 r0
add 5 r0
copy 0 r1
copy 1 r2
copy r0 r3
add 1 r3
add r2 r1
add 1 r2
compare r2 r3
bne -1
syscall 0
halt
