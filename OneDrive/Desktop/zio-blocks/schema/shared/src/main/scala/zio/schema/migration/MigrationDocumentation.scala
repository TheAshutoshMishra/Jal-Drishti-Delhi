package zio.schema.migration

/**
  * # ZIO Schema Migration System
  *
  * ## Overview
  *
  * The migration system provides a pure, algebraic way to describe structural transformations
  * between schema versions. Migrations are represented entirely as first-class, serializable data,
  * enabling:
  *
  * - Schema evolution across versions
  * - Backward/forward compatibility
  * - Data versioning
  * - Offline migrations (JSON, SQL, data lakes, registries, etc.)
  *
  * ## Core Architecture
  *
  * ### Type Hierarchy
  *
  * `Migration[A, B]`: User-facing, typed migration from A to B
  * - Wraps a `DynamicMigration` and schemas
  * - Provides apply, composition, and reverse operations
  *
  * `DynamicMigration`: Pure, serializable, untyped migration
  * - Contains a vector of `MigrationAction` instances
  * - Operates on `DynamicValue`
  * - Can be serialized/deserialized
  *
  * `MigrationAction`: Individual transformation steps
  * - AddField, DropField, Rename, TransformValue, etc.
  * - All actions are path-based via `DynamicOptic`
  * - All actions support reverse operations
  *
  * `DynamicOptic`: Path expression for locating values in a structure
  * - Root: `.`
  * - Field: `.fieldName` (e.g., `.address.street`)
  * - Case: `.when[CaseName]` (enum case selector)
  * - Each: `.each` (collection traversal)
  * - Composable and serializable
  *
  * `SchemaExpr`: Value-level transformation expressions
  * - DefaultValue: Use field's schema default
  * - Const: Use a constant value
  * - Identity: No transformation
  * - For primitive-to-primitive conversions only (in this ticket)
  *
  * ### Structural Types
  *
  * Old schema versions are represented using Scala structural types:
  *
  * ```scala
  * // Old version - exists only at compile time
  * type PersonV0 = {
  *   def firstName: String
  *   def lastName: String
  * }
  *
  * // New version - real runtime type
  * @schema
  * case class Person(fullName: String, age: Int)
  *
  * // Only the current version needs a case class
  * // Old versions use structural types with no runtime cost
  * ```
  *
  * This means:
  * - No old case classes needed
  * - No old optics needed
  * - Zero runtime overhead
  * - Compile-time safety
  *
  * ## User-Facing API: MigrationBuilder
  *
  * Migrations are created using the fluent builder API:
  *
  * ```scala
  * val migration = Migration.newBuilder[PersonV0, Person]
  *   .dropField(_.firstName)
  *   .dropField(_.lastName)
  *   .addField(_.fullName, "Unknown")
  *   .addField(_.age, 0)
  *   .build
  *
  * val result = migration(oldPerson) // Either[MigrationError, Person]
  * ```
  *
  * ### Selector Expressions
  *
  * Builder methods accept selectors that specify locations:
  *
  * - `_.fieldName`: Access a field
  * - `_.field1.field2`: Nested field access
  * - `_.items.each`: Collection traversal
  * - `_.payment.when[CreditCard]`: Case selection (enums)
  *
  * Selectors are extracted and validated via macros (details TBD).
  *
  * ### Builder Methods
  *
  * **Record operations:**
  * - `addField(selector, default)`: Add a new field
  * - `dropField(selector, defaultForReverse)`: Remove a field
  * - `renameField(from, to)`: Rename a field
  * - `transformField(from, to, transform)`: Apply a transformation
  * - `mandateField(selector, default)`: Make optional field required
  * - `optionalizeField(selector)`: Make required field optional
  * - `changeFieldType(from, to, converter)`: Convert primitive types
  *
  * **Enum operations:**
  * - `renameCase(from, to)`: Rename an enum case
  * - `transformCase(caseMigration)`: Transform case contents
  *
  * **Collection operations:**
  * - `transformElements(selector, transform)`: Transform each element
  * - `transformKeys(selector, transform)`: Transform map keys
  * - `transformValues(selector, transform)`: Transform map values
  *
  * **Building:**
  * - `build`: Build with full macro validation
  * - `buildPartial`: Build without validation
  *
  * ## Laws
  *
  * Migrations follow these laws:
  *
  * **Identity:**
  * ```scala
  * Migration.identity[A].apply(a) == Right(a)
  * ```
  *
  * **Associativity:**
  * ```scala
  * (m1 ++ m2) ++ m3 == m1 ++ (m2 ++ m3)
  * ```
  *
  * **Structural Reverse:**
  * ```scala
  * m.reverse.reverse == m
  * ```
  *
  * **Best-Effort Semantic Inverse:**
  * ```scala
  * m.apply(a) == Right(b) ⇒ m.reverse.apply(b) == Right(a)  // when possible
  * ```
  *
  * ## Error Handling
  *
  * All errors return `MigrationError`, which captures:
  * - Error message
  * - Path where error occurred (via `DynamicOptic`)
  *
  * Example error:
  * ```
  * "Failed to apply TransformValue at .addresses.each.streetNumber"
  * ```
  *
  * ## Serialization
  *
  * `DynamicMigration` is fully serializable:
  *
  * ```scala
  * val migrationJson = DynamicMigrationSerialization.toJson(dm)
  * val restored = DynamicMigrationSerialization.fromJson(migrationJson)
  * ```
  *
  * This enables:
  * - Storing migrations in registries
  * - Transmitting migrations over the network
  * - Applying migrations dynamically at runtime
  * - Generating code from migrations (future work)
  *
  * ## Example
  *
  * ```scala
  * // Old version
  * type PersonV0 = { def firstName: String; def lastName: String }
  *
  * // New version
  * @schema case class Person(fullName: String, age: Int)
  *
  * // Migration
  * val migration = Migration.newBuilder[PersonV0, Person]
  *   .dropField(_.firstName)
  *   .dropField(_.lastName)
  *   .addField(_.fullName, "Unknown")
  *   .addField(_.age, 0)
  *   .build
  *
  * // Use
  * val old = new { val firstName = "John"; val lastName = "Doe" }
  * migration(old)  // Right(Person("Unknown", 0))
  * ```
  *
  * ## Future Work
  *
  * - Full macro implementation for selector validation
  * - Code generation for DDL/DML from migrations
  * - Support for record/enum construction in SchemaExpr
  * - Composite value migrations
  * - Enum case addition/removal
  * - Advanced path expressions (key access, wrappers, etc.)
  *
  * ## Design Philosophy
  *
  * The migration system prioritizes:
  *
  * 1. **Purity**: Migrations are pure data, no functions or side effects
  * 2. **Serializability**: Migrations can be stored and transmitted
  * 3. **Introspectability**: Migrations can be inspected and transformed
  * 4. **Type Safety**: The user API is typed; the core is flexible
  * 5. **Zero Runtime Overhead**: Old versions don't pollute the runtime
  * 6. **Composability**: Migrations can be combined and reversed
  *
  * This design enables building powerful tools on top of the migration system,
  * including code generators, schema registries, and offline transformation engines.
  */
object MigrationDocumentation
