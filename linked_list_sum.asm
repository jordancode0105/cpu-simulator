// Build a linked list of 100 nodes starting at address 200, then sum it.
// Each node uses two words: [value, nextAddress]. Each value is 1.
// Result ends in r13 and should be 100.
copy 10 r9
leftshift 4 r9
add 15 r9
add 15 r9
add 10 r9
copy 1 r4
copy 10 r2
leftshift 3 r2
add 15 r2
add 5 r2
subtract 1 r2
copy r0 r0
store r4 r9
copy r9 r10
add 1 r10
copy r9 r11
add 2 r11
store r11 r10
add 2 r9
subtract 1 r2
compare 0 r2
bne -4
store r4 r9
copy r9 r10
add 1 r10
store 0 r10
copy 10 r12
leftshift 4 r12
add 15 r12
add 15 r12
add 10 r12
copy 0 r13
copy r12 r14
load 0 r14
add r14 r13
copy r12 r15
load 1 r15
copy r15 r12
compare 0 r12
bne -3
syscall 0
halt
