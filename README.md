# Solvians Java Case Study

## Introduction – Feed Generator

Our core business is to create financial websites for some of the top-tier financial institutions in the world. Those pages display information about financial products or so-called “certificates”, sourcing from numerous data feeds in our back-end system.

Certificate updates of data feed are the most important element in our applications. A certificate update has the following properties:

* Timestamp (number of milliseconds since January 1, 1970, 00:00:00 GMT)
* ISIN (string, 2 random uppercase alphabets + 9 random alphanumeric characters + 1 check digit)
* Bid Price (random number, 2 decimal places, range between 100.00 and 200.00 inclusive)
* Bid Size (random number, 0 decimal place, range between 1,000 and 5,000 inclusive)
* Ask Price (random number, 2 decimal places, range between 100.00 and 200.00 inclusive)
* Ask Size (random number, 0 decimal place, range between 1,000 and 10,000 inclusive)
* ISIN check digit calculation: 

To calculate the check digit, the following steps should be followed:
1.	Convert any letters to numbers by the conversion table below, e.g. “DE123456789” will be converted to “13 14 1 2 3 4 5 6 7 8 9”
2.	Starting from the rightmost digit, every other digit is multiplied by two. In the example, we will have “2 3 2 4 2 2 6 4 10 6 14 8 18”.
3.	Add up the resulting string of digits (numbers greater than 9 becoming two separate digits). In the example, we will have 2+3+2+4+2+2+6+4+1+0+6+1+4+8+1+8 = 54.
4.	Subtract the sum from the smallest multiple of 10 which is greater than or equal to it, in the example we will have 60 – 54 = 6. 

Conversion table for characters is:

```
A = 10	F = 15	K = 20	P = 25	U = 30	Z = 35
B = 11	G = 16	L = 21	Q = 26	V = 31	
C = 12	H = 17	M = 22	R = 27	W = 32	
D = 13	I = 18	N = 23	S = 28	X = 33	
E = 14	J = 19	O = 24	T = 29	Y = 34	
```
## Implementation – Feed Generator

* Write an ISIN Generator class with Unit tests to generate the ISIN string. This is the most central part of the case study.

* Write a Callable<String> class with Unit test to generate one line of certificate update, in which the described properties are comma separated (thousand-separator is not needed):
    `1352122280502,DE1234567896,101.23,1000,103.45,1000`

* Generating random numbers is not important in this implementation, you can write a very simple generator or use the following sample: 
```java
        ThreadLocalRandom random = ThreadLocalRandom.current();
```

* Use the `App` main class to
  - Take 2 parameters: (a) Number of threads, and (b) Number of certificate updates, (this is already implemented and tested, but feel free to improve it and fix any issues)
  - According to the parameters, trigger the certificate generations in multiple threads, and
  - Finally, collect and print the lines of generated certificate updates

We will run our own tests on your code so bear that in mind when designing classes

---

## Solution – Implementation Notes

### 1. Build and Run

#### Prerequisites

- Java 11 or later
- Apache Maven 3.x

The original Java 11 target has been deliberately retained to preserve compatibility with the supplied project and avoid unnecessary technology upgrades.

The implementation has been built and tested using Java 11 (Microsoft OpenJDK 11.0.33.1).

The original Maven compiler configuration, plugin versions and dependency versions have been retained. No additional dependencies have been introduced.

#### Build and Test

From the project root:

```bash
mvn clean package
```

To run the automated tests independently:

```bash
mvn clean test
```

#### Run the Application

```bash
java -cp target/classes com.solvians.showcase.App 2 3
```

The application accepts two required command-line parameters:

| Parameter | Description | Constraint |
|---|---|---|
| `threads` | Number of worker threads | Greater than zero |
| `quotes` | Number of certificate updates per worker | Zero or greater |

The total number of generated certificate updates is `threads × quotes`.

| Threads | Updates per worker | Total updates |
|---:|---:|---:|
| 1 | 1 | 1 |
| 2 | 3 | 6 |
| 4 | 25 | 100 |
| 10 | 100 | 1000 |
| 4 | 0 | 0 |

Missing required arguments, non-numeric values, non-positive thread counts and negative update counts are rejected.

A zero update count produces no output.

#### Example Output

```text
1795692389056,OUKC9RZRG791,144.35,2677,169.13,8011
1795692389081,UT1LYZ51P5W0,179.01,4243,126.25,1774
1795692389081,MAJYY3XRWX84,128.53,4288,168.42,3755
```

Records are printed to standard output without a CSV header or thousands separators. Prices are formatted to exactly two decimal places.

Generated values vary between executions.

### 2. Project Structure

```text
src/
├── main/java/com/solvians/showcase/
│   ├── App.java
│   ├── CertificateUpdate.java
│   ├── CertificateUpdateGenerator.java
│   ├── generator/
│   │   └── IsinGenerator.java
│   └── task/
│       └── CertificateUpdateTask.java
└── test/java/com/solvians/showcase/
    ├── AppTest.java
    ├── CertificateUpdateTest.java
    ├── CertificateUpdateGeneratorTest.java
    ├── generator/
    │   └── IsinGeneratorTest.java
    └── task/
        └── CertificateUpdateTaskTest.java
```

### 3. Implementation Overview

| Class | Responsibility |
|---|---|
| `IsinGenerator` | Generates synthetic ISINs with valid Luhn check digits |
| `CertificateUpdate` | Immutable certificate update model, field validation and CSV formatting |
| `CertificateUpdateTask` | Implements `Callable<String>` to generate individual CSV records |
| `CertificateUpdateGenerator` | Manages concurrent generation and result collection |
| `App` | Validates command-line arguments and prints generated records |

The implementation uses standard Java APIs and keeps generation, concurrency orchestration and application output separate.

### 4. Design Decisions and Assumptions

#### Update Count Semantics

Although the assignment describes the second parameter as the number of certificate updates, the supplied `CertificateUpdateGeneratorTest` expects `threads × quotes` results.

The second parameter is therefore interpreted as the number of updates per worker. This interpretation is applied consistently throughout the application.

#### Concurrency and Thread Safety

A fixed-size thread pool is used instead of parallel streams to provide explicit control over the configured thread count.

One batch task is submitted per configured worker to reduce task scheduling overhead compared with submitting a separate executor task for every update.

Results are collected using `invokeAll()` and `Future.get()`, with exception propagation, interruption handling and executor cleanup.

Certificate updates are immutable, the ISIN generator and Callable task are stateless, and random values are generated using `ThreadLocalRandom`. Each batch maintains its own local result collection.

Worker threads do not write directly to standard output.

#### Decimal Precision

Prices are generated as integer cents and represented using `BigDecimal` to avoid floating-point precision issues and ensure consistent two-decimal-place formatting.

#### Backward Compatibility

The original Maven project structure, package names and existing public method and constructor signatures have been retained where applicable.

The original `generateQuotes()` method and its return type of `Stream<CertificateUpdate>` are preserved.

The additional `generateCsvLines()` method provides CSV records for the application entry point. Both methods share the same concurrency orchestration logic.

#### Data Generation and Output Ordering

- Bid and ask prices are generated independently because the assignment does not specify a relationship between them.
- Generated ISINs follow the specified structure and checksum algorithm but do not necessarily represent registered financial instruments.
- Results are collected in batch order. Timestamps are not guaranteed to appear in chronological order.

### 5. Testing and Verification

The supplied tests are retained, with additional tests covering:

- ISIN structure, known check digits and invalid inputs.
- Certificate update validation and boundary values.
- Decimal precision and CSV formatting.
- Callable behaviour.
- Single-worker and multiple-worker configurations.
- Zero workloads and invalid arguments.
- Repeated generator execution and application output.
- Interruption handling and preservation of the calling thread's interrupt status.

All 89 automated tests passed using Java 11, with no failures or errors.

The following end-to-end executions were verified using Java 11:

| Configuration | Expected records | Actual records |
|---|---:|---:|
| 1 thread × 1 update | 1 | 1 |
| 1 thread × 100 updates | 100 | 100 |
| 2 threads × 3 updates | 6 | 6 |
| 10 threads × 100 updates | 1000 | 1000 |
| 8 threads × 2000 updates | 16000 | 16000 |
| 4 threads × 0 updates | 0 | 0 |

### 6. Known Limitations

- Generated updates are collected in memory before printing. Memory consumption therefore grows with the total workload.
- Extremely large workloads or thread counts may exceed available JVM or operating-system resources.
- The parameterised `CertificateUpdate` constructor validates the ISIN format but not its checksum. Feed-generated ISINs include valid check digits.
- Tests cover interruption handling for a pre-interrupted caller but do not directly assert worker thread identities or cover every executor failure and interruption path.

For production-scale workloads, bounded resource allocation, incremental output, backpressure and additional concurrency testing could be considered.

These enhancements are intentionally outside the scope of this assignment.