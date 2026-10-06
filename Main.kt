import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// ---------- Data classes & enum ----------

enum class Genre { FICTION, SCIENCE, HISTORY, TECH }

// Data class: book in the catalog
data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    val genre: Genre,
    val price: Double
)

// ---------- Interface ----------

interface Describable {
    fun describe(): String
}

// ---------- Inheritance & polymorphism ----------

abstract class Member(val id: Int, val name: String) : Describable {
    abstract val maxBooks: Int
    open fun loanDays(): Int = 14

    override fun describe(): String =
        "${this::class.simpleName} $name (limit: $maxBooks books, ${loanDays()} days)"
}

class Student(id: Int, name: String) : Member(id, name) {
    override val maxBooks = 2
}

class Teacher(id: Int, name: String) : Member(id, name) {
    override val maxBooks = 5
    override fun loanDays(): Int = 30
}

// ---------- Sealed class ----------

sealed class LoanResult {
    data class Success(val book: Book, val dueInDays: Int) : LoanResult()
    data class Failure(val reason: String) : LoanResult()
    object NotFound : LoanResult()
}

// ---------- Library (main logic) ----------

class Library : Describable {
    private val books = mutableListOf<Book>()                       // List
    private val members = mutableSetOf<Member>()                    // Set
    private val loans = mutableMapOf<Int, MutableList<Book>>()      // Map: memberId -> books
    private val borrowedIds = mutableSetOf<Int>()                   // Set of borrowed book ids

    fun addBook(book: Book) = books.add(book)
    fun addMember(member: Member) = members.add(member)

    fun borrowBook(member: Member, bookId: Int): LoanResult {
        val book = books.find { it.id == bookId } ?: return LoanResult.NotFound
        if (book.id in borrowedIds) return LoanResult.Failure("'${book.title}' is already borrowed")

        val current = loans.getOrPut(member.id) { mutableListOf() }
        if (current.size >= member.maxBooks) {
            return LoanResult.Failure("${member.name} reached the limit of ${member.maxBooks} books")
        }
        current.add(book)
        borrowedIds.add(book.id)
        return LoanResult.Success(book, member.loanDays())
    }

    // Higher-order function: takes a lambda as a predicate
    fun findBooks(predicate: (Book) -> Boolean): List<Book> = books.filter(predicate)

    fun titlesByGenre(): Map<Genre, List<String>> =
        books.groupBy { it.genre }.mapValues { (_, list) -> list.map { it.title } }

    fun totalValue(): Double = books.map { it.price }.reduce { acc, price -> acc + price }

    fun loansOf(member: Member): List<Book> = loans[member.id].orEmpty()

    override fun describe(): String =
        "Library: ${books.size} books, ${members.size} members, ${borrowedIds.size} borrowed"
}

// ---------- Suspend function (simulated remote call) ----------

suspend fun loadNewArrivals(): List<Book> {
    delay(1000) // simulates network delay
    return listOf(
        Book(101, "Kotlin in Action", "Dmitry Jemerov", 2017, Genre.TECH, 39.9),
        Book(102, "Cosmos", "Carl Sagan", 1980, Genre.SCIENCE, 19.5)
    )
}

// Higher-order function with a generic lambda
fun <T> printAll(title: String, items: List<T>, formatter: (T) -> String) {
    println("\n=== $title ===")
    for (item in items) println(" - ${formatter(item)}")
}

// ---------- main ----------

fun main() = runBlocking {
    // Variables and types
    val libraryName: String = "City Library"
    var operationsCount: Int = 0
    val discount: Double = 0.1
    val isOpen: Boolean = true

    println("Welcome to $libraryName! Open: $isOpen")

    val library = Library()
    listOf(
        Book(1, "Dune", "Frank Herbert", 1965, Genre.FICTION, 12.0),
        Book(2, "A Brief History of Time", "Stephen Hawking", 1988, Genre.SCIENCE, 15.5),
        Book(3, "SPQR", "Mary Beard", 2015, Genre.HISTORY, 18.0),
        Book(4, "Clean Code", "Robert Martin", 2008, Genre.TECH, 30.0),
        Book(5, "Neuromancer", "William Gibson", 1984, Genre.FICTION, 11.0)
    ).forEach { library.addBook(it) }

    // Polymorphism: list of Member holding different subclasses
    val members: List<Member> = listOf(Student(1, "Aigerim"), Teacher(2, "Mr. Kim"))
    members.forEach { library.addMember(it) }
    printAll("Members", members) { it.describe() }

    // Collection operations: filter, map, reduce, groupBy
    val oldBooks = library.findBooks { it.year < 1990 }
    printAll("Books before 1990", oldBooks) { "${it.title} (${it.year})" }

    println("\nTitles by genre: ${library.titlesByGenre()}")
    println("Total value: ${library.totalValue()}")
    println("Total with discount: ${"%.2f".format(library.totalValue() * (1 - discount))}")

    // Loop + condition + sealed class with exhaustive 'when'
    println("\n=== Borrowing ===")
    val student = members[0]
    val requests = listOf(1, 2, 3, 1, 99) // 3rd goes over the limit, 1 again is taken, 99 doesn't exist
    for (bookId in requests) {
        val result = library.borrowBook(student, bookId)
        val message = when (result) {
            is LoanResult.Success -> "OK: '${result.book.title}' for ${result.dueInDays} days"
            is LoanResult.Failure -> "Denied: ${result.reason}"
            LoanResult.NotFound -> "Book #$bookId not found"
        }
        println(message)
        operationsCount++
    }

    // while loop
    var i = 0
    while (i < library.loansOf(student).size) {
        println("${student.name} has: ${library.loansOf(student)[i].title}")
        i++
    }

    // Coroutine: load new books asynchronously while doing other work
    println("\n=== Coroutines ===")
    val arrivals = async { loadNewArrivals() }
    println("Loading new arrivals... (main code keeps running)")
    val newBooks = arrivals.await()
    newBooks.forEach { library.addBook(it) }
    printAll("New arrivals", newBooks) { "${it.title} by ${it.author}" }

    println("\n${library.describe()}")
    println("Operations performed: $operationsCount")
}
