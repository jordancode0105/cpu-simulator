# CPU Simulator — ICSI 404

A Java CPU simulator developed for ICSI 404. The project models a small custom instruction set, from individual bits and arithmetic circuits through assembly, instruction execution, memory access, and cache cycle accounting.

The original class project layout and implementation are preserved. Production classes and JUnit tests live together in `Skeleton/src/` and use Java's default package.

## Components and features

| Component | Implementation |
| --- | --- |
| `Bit`, `Word16`, `Word32` | Boolean-backed bits, fixed-width words, copying, bit access, AND/OR/XOR/NOT, and splitting 32-bit words into halves. |
| `Adder`, `Multiplier`, `Shifter` | Bitwise addition, two's-complement subtraction, shift-and-add multiplication, and logical left/right shifts. Arithmetic retains the low 32 bits. |
| `ALU` | Arithmetic, logical operations, shifts, signed comparison flags, copying, and address calculations. |
| `Assembler` | Converts assembly strings into 16-bit binary instructions and packs pairs into 32-bit memory words. Supports register/immediate operands, blank lines, and `//` or `#` comments. |
| `Memory` | 1,000 addressable 32-bit words, reads/writes, binary program loading, and address/input validation. |
| `Processor` | 32 general-purpose 32-bit registers; fetch/decode/execute/store loop; conditional branches; calls and returns using Java stacks; loads/stores; register and memory output. |
| `InstructionCache`, `L2Cache` | One eight-word instruction-cache line and four directly mapped eight-word L2 lines. L2 serves instructions and data, writes through to memory, and processor stores invalidate overlapping L1 instructions. |
| `TestConverter` | Integer/word conversion helper used by tests **and by the processor's memory output**; required when compiling the simulator. |

The processor tracks simulated clock cycles and prints the count on `halt`. These are costs assigned by the simulator, not measurements of host CPU performance.

### Instruction set

Implemented assembly mnemonics:

```text
halt       add        and        multiply   leftshift
subtract   or         rightshift syscall    call
return     compare    ble        blt        bge
bgt        beq        bne        load       store      copy
```

Registers are `r0`–`r31`. Ordinary two-operand instructions use a source register or signed five-bit immediate (`-16` to `15`), followed by a destination register. For example, `add 10 r0` adds ten to `r0`; `subtract r1 r2` computes `r2 - r1` into `r2`. `compare` compares the source against the destination. XOR and NOT are word-level operations, not processor opcodes.

Two instructions occupy each memory word. Calls and branches use offsets relative to the current **word** address and target its top instruction half. Their 11-bit field is interpreted as signed by the processor; although the assembler accepts positive values through 2047, values above 1023 execute as negative offsets. There is no label resolution: branch offsets are numeric.

`syscall 0` prints registers; `syscall 1` prints memory. Other syscall numbers have no output action. This is a custom educational architecture, not an emulator for a commercial ISA or operating system.

## Technologies

- Java 21 and the Java standard library.
- JUnit Jupiter 5.8.1 for the existing tests.
- Originally configured in IntelliJ IDEA; the command-line workflow below does not require an IDE, Maven, or Gradle.

Java 21 is required for the complete test suite because `ProcessorTest` uses `List.getFirst()`.

## Project structure

```text
.
├── README.md
├── .gitignore
├── Skeleton/
│   ├── .gitignore             # Retained original ignore rules
│   └── src/
│       ├── *.java             # 13 simulator/helper classes
│       └── *Test.java         # 10 existing JUnit test classes
├── sum_no_memory.asm          # Sum 1–100 using registers
├── sum_array_memory.asm       # Build and sum an array in memory
└── linked_list_sum.asm        # Build and sum a linked list in memory
```

The assembly files are required by three processor tests and must remain at the repository root. This source tree implements the final L1/L2 instruction-and-data design and has no switch to run earlier cache configurations.

Local `.idea/`, `*.iml`, `out/`, `.class`, `__MACOSX/`, and OS metadata are excluded by the root `.gitignore`. They are not required to build or test. Downloaded dependencies and new output go into ignored `.deps/` and `build/` directories.

## Build and test

Run all commands from the repository root (the directory containing the `.asm` files). Install a JDK 21 and put its `bin` directory on `PATH`; verify with `java -version` and `javac -version`.

The JUnit Platform Console Standalone 1.8.1 JAR includes Jupiter 5.8.1 and its dependencies, matching the original IntelliJ configuration. Download it once; subsequent builds/tests can run offline.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force .deps, build/classes | Out-Null
Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.1/junit-platform-console-standalone-1.8.1.jar' -OutFile '.deps/junit-platform-console-standalone-1.8.1.jar'

javac --release 21 -cp .deps/junit-platform-console-standalone-1.8.1.jar -d build/classes (Get-ChildItem Skeleton/src/*.java).FullName
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
java -jar .deps/junit-platform-console-standalone-1.8.1.jar --class-path build/classes --scan-class-path --fail-if-no-tests --reports-dir build/test-results
if ($LASTEXITCODE -ne 0) { throw 'Tests failed' }
```

### macOS / Linux (Bash)

```bash
mkdir -p .deps build/classes
curl --fail --location --output .deps/junit-platform-console-standalone-1.8.1.jar \
  https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.1/junit-platform-console-standalone-1.8.1.jar

javac --release 21 -cp .deps/junit-platform-console-standalone-1.8.1.jar \
  -d build/classes Skeleton/src/*.java && \
java -jar .deps/junit-platform-console-standalone-1.8.1.jar \
  --class-path build/classes --scan-class-path --fail-if-no-tests \
  --reports-dir build/test-results
```

These commands compile fresh source rather than using the archived IntelliJ bytecode. The PowerShell workflow was verified on Windows; the Bash equivalent is provided for portability but was not run on macOS/Linux.

## Run the simulator

The original project has no `public static void main` entry point or standalone CLI. Its existing `ProcessorTest` class is the runnable demonstration harness. After compiling above, run just the assembly examples:

```text
java -jar .deps/junit-platform-console-standalone-1.8.1.jar --class-path build/classes --select-method ProcessorTest#testSumNoMemoryCycles --select-method ProcessorTest#testSumArrayMemoryCycles --select-method ProcessorTest#testLinkedListSumCycles
```

Each example prints registers and its cycle count. To invoke the simulator from another Java class in the default package, the existing API is:

```java
String[] program = { "copy 5 r0", "add 10 r0", "syscall 0", "halt" };
Memory memory = new Memory();
memory.load(Assembler.finalOutput(program));
Processor processor = new Processor(memory);
processor.run();
```

This snippet belongs inside a method; it is not a supplied CLI. To load an assembly file, read its lines into a `String[]` as demonstrated in `ProcessorTest.runAsmFile`. Programs should terminate with `halt` or a returning empty call stack; execution has no instruction-count timeout.

## Verification and scope

Verified on September 8, 2026 using Amazon Corretto **21.0.9** and JUnit Platform Console **1.8.1** on Windows:

- All 23 Java source/test files compiled without source changes.
- **39 tests passed; 0 failed, skipped, or aborted.**
- Tests cover bits, words, arithmetic, shifts, assembly encoding, memory, and processor programs including Fibonacci, powers, and the three assembly examples.

The cycle examples print measurements rather than asserting exact cycle counts or final sums. The basic cycle test only asserts a positive count; there are no dedicated cache test classes. Passing the suite therefore does not establish exhaustive instruction or cache correctness. The original tests and implementation are preserved, including these limits.

Original source comments are retained. Repository preparation adds documentation and ignore rules without changing authorship or assigning a new license. The original `report.docx` is retained locally but excluded from publication.
