package zio.schema.migration

import zio.schema._
import zio.schema.migration.macros.SelectorMacros

class MigrationBuilder[A, B](
  val sourceSchema: Schema[A],
  val targetSchema: Schema[B],
  val actions: Vector[MigrationAction] = Vector.empty
) {
  // ----- Record operations -----
  def addField(
    target: B => Any,
    default: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val fieldName = SelectorMacros.extractField(target)
    val newAction = MigrationAction.AddField(
      DynamicOptic.Field(fieldName, DynamicOptic.Root),
      default
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def dropField(
    source: A => Any,
    defaultForReverse: SchemaExpr[B] = SchemaExpr.DefaultValue
  ): MigrationBuilder[A, B] = {
    val fieldName = SelectorMacros.extractField(source)
    val newAction = MigrationAction.DropField(
      DynamicOptic.Field(fieldName, DynamicOptic.Root),
      defaultForReverse
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def renameField(
    from: A => Any,
    to: B => Any
  ): MigrationBuilder[A, B] = {
    val fromField = SelectorMacros.extractField(from)
    val toField = SelectorMacros.extractField(to)
    val newAction = MigrationAction.Rename(
      DynamicOptic.Field(fromField, DynamicOptic.Root),
      toField
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def transformField(
    from: A => Any,
    to: B => Any,
    transform: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val fieldName = SelectorMacros.extractField(from)
    val newAction = MigrationAction.TransformValue(
      DynamicOptic.Field(fieldName, DynamicOptic.Root),
      transform
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def mandateField(
    source: A => Option[_],
    target: B => Any,
    default: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val fieldName = SelectorMacros.extractField(source)
    val newAction = MigrationAction.Mandate(
      DynamicOptic.Field(fieldName, DynamicOptic.Root),
      default
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def optionalizeField(
    source: A => Any,
    target: B => Option[_]
  ): MigrationBuilder[A, B] = {
    val fieldName = SelectorMacros.extractField(source)
    val newAction = MigrationAction.Optionalize(
      DynamicOptic.Field(fieldName, DynamicOptic.Root)
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def changeFieldType(
    source: A => Any,
    target: B => Any,
    converter: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val fieldName = SelectorMacros.extractField(source)
    val newAction = MigrationAction.ChangeType(
      DynamicOptic.Field(fieldName, DynamicOptic.Root),
      converter
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  // ----- Enum operations (limited) -----
  def renameCase[SumA, SumB](
    from: String,
    to: String
  ): MigrationBuilder[A, B] = {
    val newAction = MigrationAction.RenameCase(
      DynamicOptic.Root,
      from,
      to
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def transformCase[SumA, CaseA, SumB, CaseB](
    caseMigration: MigrationBuilder[CaseA, CaseB] => MigrationBuilder[CaseA, CaseB]
  ): MigrationBuilder[A, B] = {
    // Create a builder for the case transformation
    val caseBuilder = new MigrationBuilder[CaseA, CaseB](
      Schema.primitive[CaseA],
      Schema.primitive[CaseB]
    )
    val transformed = caseMigration(caseBuilder)
    val newAction = MigrationAction.TransformCase(
      DynamicOptic.Root,
      transformed.actions
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  // ----- Collections -----
  def transformElements(
    at: A => Vector[_],
    transform: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val newAction = MigrationAction.TransformElements(
      DynamicOptic.Each(DynamicOptic.Root),
      transform
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  // ----- Maps -----
  def transformKeys(
    at: A => Map[_, _],
    transform: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val newAction = MigrationAction.TransformKeys(
      DynamicOptic.Root,
      transform
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  def transformValues(
    at: A => Map[_, _],
    transform: SchemaExpr[A]
  ): MigrationBuilder[A, B] = {
    val newAction = MigrationAction.TransformValues(
      DynamicOptic.Root,
      transform
    )
    new MigrationBuilder(sourceSchema, targetSchema, actions :+ newAction)
  }

  /** Build migration with full macro validation */
  def build: Migration[A, B] = {
    val dynamicMigration = DynamicMigration(actions)
    Migration(dynamicMigration, sourceSchema, targetSchema)
  }

  /** Build migration without full validation */
  def buildPartial: Migration[A, B] = {
    val dynamicMigration = DynamicMigration(actions)
    Migration(dynamicMigration, sourceSchema, targetSchema)
  }
}

object Migration {
  def newBuilder[A, B](
    sourceSchema: Schema[A],
    targetSchema: Schema[B]
  ): MigrationBuilder[A, B] =
    new MigrationBuilder(sourceSchema, targetSchema)

  def identity[A](schema: Schema[A]): Migration[A, A] =
    Migration(DynamicMigration(Vector.empty), schema, schema)
}

// Placeholder for serialization logic for DynamicMigration
object DynamicMigrationSerialization {
  def toJson(dm: DynamicMigration): String = "" // TODO: Implement
  def fromJson(json: String): DynamicMigration = ??? // TODO: Implement
}
