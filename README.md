# Library Manager (Kotlin Console App)

A small console application that simulates a city library: a catalog of books, members (students and teachers) with different borrowing limits, borrowing logic, and asynchronous loading of new arrivals.

## How to run

Requirements: JDK 17+ and IntelliJ IDEA (Community is fine).

**IntelliJ IDEA:** open the project folder, wait for Gradle sync, open `src/main/kotlin/Main.kt` and click the green ▶ next to `main()`.

**Terminal:**
```bash
./gradlew run        # Linux / macOS
gradlew.bat run      # Windows
```

## Where the requirements are demonstrated (`Main.kt`)

| Requirement | Where |
|---|---|
| Variables, data types, conditions, loops | `main()`: `val`/`var`, `Int`, `Double`, `Boolean`, `String`, `for`, `while`, `if`, `when`; `Library.borrowBook` |
| List, Set, Map | `Library`: `books` (MutableList), `members` and `borrowedIds` (MutableSet), `loans` (MutableMap) |
| map, filter, reduce | `Library.totalValue()` (map + reduce), `findBooks` (filter), `titlesByGenre` (groupBy + mapValues) |
| Functions, higher-order functions, lambdas | `Library.findBooks(predicate)`, `printAll(title, items, formatter)`, lambdas in `main()` |
| Classes and objects | `Library`, `Book`, instances created in `main()` |
| Inheritance | `Member` (abstract) -> `Student`, `Teacher` (override `maxBooks`, `loanDays()`) |
| Interfaces and polymorphism | `Describable` interface; `members: List<Member>` calls `describe()` on different subclasses |
| Data class | `Book`, `LoanResult.Success`, `LoanResult.Failure` |
| Sealed class | `LoanResult` with exhaustive `when` in `main()` |
| Suspend function + coroutine | `loadNewArrivals()` (suspend, `delay`), launched with `async`/`await` inside `runBlocking` in `main()` |
