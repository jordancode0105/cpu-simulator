// Build an array at addresses 200-299 holding 1..100, then sum it.
// Result ends in r4.
copy 10 r9
leftshift 4 r9
add 15 r9
add 15 r9
add 10 r9
copy 1 r2
copy 10 r3
leftshift 3 r3
add 15 r3
add 5 r3
add 1 r3
copy r0 r0
store r2 r9
add 1 r9
add 1 r2
compare r2 r3
bne -2
copy 10 r9
leftshift 4 r9
add 15 r9
add 15 r9
add 10 r9
copy 0 r4
copy 1 r2
copy r9 r5
load 0 r5
add r5 r4
add 1 r9
add 1 r2
compare r2 r3
bne -3
syscall 0
halt
