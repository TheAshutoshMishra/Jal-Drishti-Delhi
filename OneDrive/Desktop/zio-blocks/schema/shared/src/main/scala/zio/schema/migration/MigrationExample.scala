package zio.schema.migration

import zio.schema._

/**
  * Example: Schema Migration from PersonV0 to PersonV1
  *
  * This demonstrates the migration system for evolving a Person schema:
  * - Old version (PersonV0): has firstName and lastName as separate fields
  * - New version (PersonV1): has a single fullName field and an age field
  */
object MigrationExample {

  // Structural type for old version (exists only at compile time)
  type PersonV0 = {
    def firstName: String
    def lastName: String
  }

  // New runtime type
  @schema
  case class PersonV1(fullName: String, age: Int)

  // Old structural schema
  implicit val personV0Schema: Schema[PersonV0] = Schema.structural[PersonV0]

  // New schema
  implicit val personV1Schema: Schema[PersonV1] = Schema.derived[PersonV1]

  /**
    * Define a migration from PersonV0 to PersonV1
    *
    * The migration:
    * 1. Combines firstName and lastName into fullName
    * 2. Adds age field with default value of 0
    */
  def createPersonMigration(): Either[String, Migration[PersonV0, PersonV1]] = {
    try {
      // Build the dynamic migration actions
      val actions = Vector(
        // Action 1: Drop firstName field
        MigrationAction.DropField(
          DynamicOptic.Field("firstName", DynamicOptic.Root),
          SchemaExpr.DefaultValue
        ),
        // Action 2: Drop lastName field
        MigrationAction.DropField(
          DynamicOptic.Field("lastName", DynamicOptic.Root),
          SchemaExpr.DefaultValue
        ),
        // Action 3: Add fullName field (combination of firstName and lastName)
        MigrationAction.AddField(
          DynamicOptic.Field("fullName", DynamicOptic.Root),
          SchemaExpr.Const("Unknown") // Placeholder value
        ),
        // Action 4: Add age field with default 0
        MigrationAction.AddField(
          DynamicOptic.Field("age", DynamicOptic.Root),
          SchemaExpr.Const(0)
        )
      )

      val dynamicMigration = DynamicMigration(actions)
      val migration = Migration(dynamicMigration, personV0Schema, personV1Schema)

      Right(migration)
    } catch {
      case e: Exception => Left(s"Failed to create migration: ${e.getMessage}")
    }
  }

  /**
    * Example: Simple field rename migration
    *
    * Demonstrates renaming a field in a simple record
    */
  type AddressV0 = {
    def zip: String
  }

  @schema
  case class AddressV1(zipCode: String)

  implicit val addressV0Schema: Schema[AddressV0] = Schema.structural[AddressV0]
  implicit val addressV1Schema: Schema[AddressV1] = Schema.derived[AddressV1]

  def createAddressMigration(): Migration[AddressV0, AddressV1] = {
    val renameAction = MigrationAction.Rename(
      DynamicOptic.Field("zip", DynamicOptic.Root),
      "zipCode"
    )
    val dynamicMigration = DynamicMigration(Vector(renameAction))
    Migration(dynamicMigration, addressV0Schema, addressV1Schema)
  }

  /**
    * Example: Composing migrations
    *
    * Shows how to chain multiple migrations together
    */
  type PersonV2 = {
    def fullName: String
    def age: Int
    def country: String
  }

  @schema
  case class PersonV2_Runtime(fullName: String, age: Int, country: String)

  implicit val personV2Schema: Schema[PersonV2] = Schema.structural[PersonV2]
  implicit val personV2RuntimeSchema: Schema[PersonV2_Runtime] = Schema.derived[PersonV2_Runtime]

  def createComposedMigration(): Migration[PersonV0, PersonV2_Runtime] = {
    // First migration: V0 -> V1
    val migration1 = Migration(
      DynamicMigration(Vector(
        MigrationAction.DropField(DynamicOptic.Field("firstName", DynamicOptic.Root), SchemaExpr.DefaultValue),
        MigrationAction.DropField(DynamicOptic.Field("lastName", DynamicOptic.Root), SchemaExpr.DefaultValue),
        MigrationAction.AddField(DynamicOptic.Field("fullName", DynamicOptic.Root), SchemaExpr.Const("Unknown")),
        MigrationAction.AddField(DynamicOptic.Field("age", DynamicOptic.Root), SchemaExpr.Const(0))
      )),
      personV0Schema,
      personV1Schema
    )

    // Second migration: V1 -> V2 (add country field)
    val migration2 = Migration(
      DynamicMigration(Vector(
        MigrationAction.AddField(DynamicOptic.Field("country", DynamicOptic.Root), SchemaExpr.Const("Unknown"))
      )),
      personV1Schema,
      personV2RuntimeSchema
    )

    // Compose: V0 -> V1 -> V2
    migration1 ++ migration2
  }

  /**
    * Example: Reverse migration
    *
    * Shows how migrations can be reversed for downgrading data
    */
  def demonstrateReverse(): Unit = {
    val forwardMigration = createAddressMigration()
    val reverseMigration = forwardMigration.reverse

    println(s"Forward migration: ${forwardMigration.dynamicMigration.actions.length} actions")
    println(s"Reverse migration: ${reverseMigration.dynamicMigration.actions.length} actions")
  }
}
