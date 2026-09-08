# CPU Simulator — ICSI 404

A Java CPU simulator built for ICSI 404, connecting bit-level logic to assembly execution and memory hierarchy design. It implements a custom instruction set, a complete fetch–decode–execute–store cycle, and L1/L2 caches with simulated clock-cycle accounting.

**Java 21 · JUnit 5 · 39 passing tests**

## Highlights

- **Processor:** 32 general-purpose registers, 16-bit instructions packed into 32-bit words, conditional branches, subroutine calls and returns, and memory load/store operations.
- **Arithmetic and logic:** bitwise addition, two's-complement subtraction, shift-and-add multiplication, logical shifts, and signed comparisons.
- **Assembler:** register and immediate operands, numeric branch offsets, and binary instruction encoding.
- **Memory hierarchy:** 1,000 words of main memory, an eight-word L1 instruction cache, and a four-line, directly mapped L2 cache serving instructions and data with write-through stores.
- **Program examples:** Fibonacci and exponentiation tests, plus assembly programs that sum integers using registers, an array, and a linked list.

## Project structure

```text
.
├── src/                      # Simulator classes and JUnit tests
├── sum_no_memory.asm         # Sum 1–100 using registers
├── sum_array_memory.asm      # Build and sum an array in memory
├── linked_list_sum.asm       # Build and sum a linked list in memory
├── .gitignore
└── README.md
```

| Classes | Responsibility |
| --- | --- |
| `Bit`, `Word16`, `Word32` | Bits, fixed-width words, and logical operations |
| `Adder`, `Multiplier`, `Shifter`, `ALU` | Arithmetic, shifts, comparisons, and address calculations |
| `Assembler`, `Processor` | Instruction encoding and execution |
| `Memory`, `InstructionCache`, `L2Cache` | Memory access, caching, and cycle costs |
| `TestConverter`, `*Test` | Integer conversion helper and 10 test classes |

Source and tests retain the original default-package layout together in `src/`. The simulator implementation and original comments are preserved.

## Build and test

Use **JDK 21** with `java` and `javac` on your `PATH`. Run commands from the repository root so the tests can find the assembly files. The only external dependency is JUnit; download its standalone runner once using either workflow below.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force .deps, build/classes | Out-Null
Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.8.1/junit-platform-console-standalone-1.8.1.jar' -OutFile '.deps/junit-platform-console-standalone-1.8.1.jar'

javac --release 21 -cp .deps/junit-platform-console-standalone-1.8.1.jar -d build/classes (Get-ChildItem src/*.java).FullName
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
  -d build/classes src/*.java && \
java -jar .deps/junit-platform-console-standalone-1.8.1.jar \
  --class-path build/classes --scan-class-path --fail-if-no-tests \
  --reports-dir build/test-results
```

JUnit Platform 1.8.1 includes Jupiter 5.8.1, matching the original project dependency. Build output, test reports, and downloaded dependencies are ignored by Git.

## Run an example

The project runs through its JUnit demonstration harness rather than a standalone CLI. After building, run the register-based sum example:

```text
java -jar .deps/junit-platform-console-standalone-1.8.1.jar --class-path build/classes --select-method ProcessorTest#testSumNoMemoryCycles
```

This program sums 1–100 into `r1` and prints the registers and simulated cycle count. Use `testSumArrayMemoryCycles` or `testLinkedListSumCycles` to run the memory-based examples.

To execute a small program from a Java method:

```java
String[] program = { "copy 5 r0", "add 10 r0", "syscall 0", "halt" };
Memory memory = new Memory();
memory.load(Assembler.finalOutput(program));
new Processor(memory).run();
```

## Test results

**39 tests passed, with no failures or skips**, after moving the source to `src/`. Verified on Windows with Amazon Corretto 21.0.9 and JUnit Jupiter 5.8.1.

The suite covers bit and word operations, arithmetic, shifts, assembly encoding, memory access, and processor programs. The assembly experiments also print cycle counts for comparing access patterns.
