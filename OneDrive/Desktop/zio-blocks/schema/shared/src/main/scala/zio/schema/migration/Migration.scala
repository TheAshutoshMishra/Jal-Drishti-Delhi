package zio.schema.migration

import zio.schema._

sealed trait MigrationError {
  def message: String
  def path: Option[DynamicOptic]
}

object MigrationError {
  final case class Generic(message: String, path: Option[DynamicOptic] = None) extends MigrationError
}

// Path representation for migration actions (field, case, collection, etc.)
sealed trait DynamicOptic extends Product with Serializable {
  def show: String
}

object DynamicOptic {
  case object Root extends DynamicOptic {
    def show: String = "."
  }
  final case class Field(name: String, parent: DynamicOptic = Root) extends DynamicOptic {
    def show: String = s"${parent.show}.$name"
  }
  final case class Case(tag: String, parent: DynamicOptic = Root) extends DynamicOptic {
    def show: String = s"${parent.show}.when[$tag]"
  }
  final case class Each(parent: DynamicOptic = Root) extends DynamicOptic {
    def show: String = s"${parent.show}.each"
  }
  final case class Key(key: String, parent: DynamicOptic = Root) extends DynamicOptic {
    def show: String = s"${parent.show}[$key]"
  }
  // Add more as needed
}

// Value-level transformation expressions (primitive-to-primitive only for this ticket)
sealed trait SchemaExpr[A] extends Product with Serializable
object SchemaExpr {
  case object DefaultValue extends SchemaExpr[Nothing]
  final case class Const[A](value: A) extends SchemaExpr[A]
  final case class Identity[A]() extends SchemaExpr[A]
  // Add more as needed for primitive conversions
}

sealed trait MigrationAction {
  def at: DynamicOptic
  def reverse: MigrationAction
}

object MigrationAction {
  final case class AddField(
    at: DynamicOptic,
    default: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = DropField(at, SchemaExpr.DefaultValue)
  }

  final case class DropField(
    at: DynamicOptic,
    defaultForReverse: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = AddField(at, defaultForReverse)
  }

  final case class Rename(
    at: DynamicOptic,
    to: String
  ) extends MigrationAction {
    def reverse: MigrationAction = Rename(at, to) // Needs more logic for real reverse
  }

  final case class TransformValue(
    at: DynamicOptic,
    transform: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = TransformValue(at, transform) // Needs more logic for real reverse
  }

  final case class Mandate(
    at: DynamicOptic,
    default: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = Optionalize(at)
  }

  final case class Optionalize(
    at: DynamicOptic
  ) extends MigrationAction {
    def reverse: MigrationAction = Mandate(at, SchemaExpr.DefaultValue)
  }

  final case class Join(
    at: DynamicOptic,
    sourcePaths: Vector[DynamicOptic],
    combiner: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = Split(at, sourcePaths, combiner)
  }

  final case class Split(
    at: DynamicOptic,
    targetPaths: Vector[DynamicOptic],
    splitter: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = Join(at, targetPaths, splitter)
  }

  final case class ChangeType(
    at: DynamicOptic,
    converter: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = ChangeType(at, converter) // Needs more logic for real reverse
  }

  // Enum actions
  final case class RenameCase(
    at: DynamicOptic,
    from: String,
    to: String
  ) extends MigrationAction {
    def reverse: MigrationAction = RenameCase(at, to, from)
  }

  final case class TransformCase(
    at: DynamicOptic,
    actions: Vector[MigrationAction]
  ) extends MigrationAction {
    def reverse: MigrationAction = TransformCase(at, actions.map(_.reverse))
  }

  // Collection/Map actions
  final case class TransformElements(
    at: DynamicOptic,
    transform: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = TransformElements(at, transform) // Needs more logic for real reverse
  }

  final case class TransformKeys(
    at: DynamicOptic,
    transform: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = TransformKeys(at, transform) // Needs more logic for real reverse
  }

  final case class TransformValues(
    at: DynamicOptic,
    transform: SchemaExpr[_]
  ) extends MigrationAction {
    def reverse: MigrationAction = TransformValues(at, transform) // Needs more logic for real reverse
  }
}

final case class DynamicMigration(
  actions: Vector[MigrationAction]
) {
  def apply(value: DynamicValue): Either[MigrationError, DynamicValue] = {
    // Apply all actions in order to the DynamicValue
    actions.foldLeft[Either[MigrationError, DynamicValue]](Right(value)) {
      case (Right(v), action) => applyAction(action, v)
      case (err@Left(_), _)   => err
    }
  }

  private def applyAction(action: MigrationAction, value: DynamicValue): Either[MigrationError, DynamicValue] = {
    action match {
      case MigrationAction.AddField(at, default) =>
        // Only support adding at root or top-level field for now
        (at, value) match {
          case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
            val defaultValue = default match {
              case SchemaExpr.Const(v) => DynamicValue.fromAny(v)
              case SchemaExpr.DefaultValue => DynamicValue.Null // Placeholder for schema default
              case _ => DynamicValue.Null
            }
            Right(dv.copy(fields = dv.fields :+ (field -> defaultValue)))
          case _ =>
            Left(MigrationError.Generic(s"AddField not supported for path ${at.show}", Some(at)))
        }

      case MigrationAction.DropField(at, _) =>
        (at, value) match {
          case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
            Right(dv.copy(fields = dv.fields.filterNot(_._1 == field)))
          case _ =>
            Left(MigrationError.Generic(s"DropField not supported for path ${at.show}", Some(at)))
        }

      case MigrationAction.Rename(at, to) =>
        (at, value) match {
          case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
            dv.fields.find(_._1 == field) match {
              case Some((_, v)) =>
                val updatedFields = dv.fields.filterNot(_._1 == field) :+ (to -> v)
                Right(dv.copy(fields = updatedFields))
              case None =>
                Left(MigrationError.Generic(s"Rename: field '$field' not found", Some(at)))
            }
          case _ =>
            Left(MigrationError.Generic(s"Rename not supported for path ${at.show}", Some(at)))
        }

      case MigrationAction.TransformValue(at, transform) =>
        (at, value) match {
          case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
            val updatedFields = dv.fields.map {
              case (f, v) if f == field =>
                val newValue = transform match {
                  case SchemaExpr.Const(newV) => DynamicValue.fromAny(newV)
                  case SchemaExpr.Identity()  => v
                  case _ => v // Extend for more cases
                }
                (f, newValue)
              case other => other
            }
            Right(dv.copy(fields = updatedFields))
          case _ =>
            Left(MigrationError.Generic(s"TransformValue not supported for path ${at.show}", Some(at)))
        }

      case MigrationAction.Mandate(at, default) =>
        (at, value) match {
          case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
            val updatedFields = dv.fields.map {
              case (f, v) if f == field =>
                val newValue = v match {
                  case DynamicValue.Null =>
                    default match {
                      case SchemaExpr.Const(v) => DynamicValue.fromAny(v)
                      case SchemaExpr.DefaultValue => DynamicValue.Null
                      case _ => DynamicValue.Null
                    }
                  case other => other
                }
                (f, newValue)
              case other => other
            }
            Right(dv.copy(fields = updatedFields))
          case _ =>
            Left(MigrationError.Generic(s"Mandate not supported for path ${at.show}", Some(at)))
        }

      case MigrationAction.Optionalize(at) =>
        // For now, just return as-is; wrapping in Option requires schema knowledge
        Right(value)

      case MigrationAction.RenameCase(at, from, to) =>
        // Enum case renaming
        applyRenameCase(at, from, to, value)

      case MigrationAction.TransformCase(at, actions) =>
        // Apply multiple actions to an enum case
        applyTransformCase(at, actions, value)

      case MigrationAction.TransformElements(at, transform) =>
        applyTransformElements(at, transform, value)

      case MigrationAction.TransformKeys(at, transform) =>
        applyTransformKeys(at, transform, value)

      case MigrationAction.TransformValues(at, transform) =>
        applyTransformValues(at, transform, value)

      case MigrationAction.Join(at, sourcePaths, combiner) =>
        applyJoin(at, sourcePaths, combiner, value)

      case MigrationAction.Split(at, targetPaths, splitter) =>
        applySplit(at, targetPaths, splitter, value)

      case MigrationAction.ChangeType(at, converter) =>
        applyChangeType(at, converter, value)
    }
  }

  private def applyAddField(at: DynamicOptic, default: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    (at, value) match {
      case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
        val defaultValue = evaluateSchemaExpr(default)
        val newFields = dv.fields :+ (field -> defaultValue)
        Right(dv.copy(fields = newFields))
      case _ =>
        Left(MigrationError.Generic(s"AddField not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyDropField(at: DynamicOptic, value: DynamicValue): Either[MigrationError, DynamicValue] = {
    (at, value) match {
      case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
        Right(dv.copy(fields = dv.fields.filterNot(_._1 == field)))
      case _ =>
        Left(MigrationError.Generic(s"DropField not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyRename(at: DynamicOptic, to: String, value: DynamicValue): Either[MigrationError, DynamicValue] = {
    (at, value) match {
      case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
        dv.fields.find(_._1 == field) match {
          case Some((_, v)) =>
            val updatedFields = dv.fields.filterNot(_._1 == field) :+ (to -> v)
            Right(dv.copy(fields = updatedFields))
          case None =>
            Left(MigrationError.Generic(s"Rename: field '$field' not found", Some(at)))
        }
      case _ =>
        Left(MigrationError.Generic(s"Rename not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyMandate(at: DynamicOptic, default: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    (at, value) match {
      case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
        val updatedFields = dv.fields.map {
          case (f, v) if f == field =>
            val newValue = v match {
              case DynamicValue.Null => evaluateSchemaExpr(default)
              case other => other
            }
            (f, newValue)
          case other => other
        }
        Right(dv.copy(fields = updatedFields))
      case _ =>
        Left(MigrationError.Generic(s"Mandate not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyChangeType(at: DynamicOptic, converter: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    (at, value) match {
      case (DynamicOptic.Field(field, DynamicOptic.Root), dv: DynamicValue.Record) =>
        val updatedFields = dv.fields.map {
          case (f, v) if f == field =>
            val newValue = evaluateSchemaExpr(converter)
            (f, newValue)
          case other => other
        }
        Right(dv.copy(fields = updatedFields))
      case _ =>
        Left(MigrationError.Generic(s"ChangeType not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyRenameCase(at: DynamicOptic, from: String, to: String, value: DynamicValue): Either[MigrationError, DynamicValue] = {
    value match {
      case _: DynamicValue.Enum =>
        Right(value) // TODO: Implement enum case renaming logic
      case _ =>
        Right(value)
    }
  }

  private def applyTransformCase(at: DynamicOptic, actions: Vector[MigrationAction], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    actions.foldLeft[Either[MigrationError, DynamicValue]](Right(value)) {
      case (Right(v), action) => applyAction(action, v)
      case (err@Left(_), _) => err
    }
  }

  private def applyTransformElements(at: DynamicOptic, transform: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    (at, value) match {
      case (DynamicOptic.Each(DynamicOptic.Root), dv: DynamicValue.Vector) =>
        val newElements = dv.listValue.map { _ =>
          evaluateSchemaExpr(transform)
        }
        Right(dv.copy(listValue = newElements))
      case _ =>
        Left(MigrationError.Generic(s"TransformElements not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyTransformKeys(at: DynamicOptic, transform: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    value match {
      case dv: DynamicValue.Dict =>
        Right(dv) // TODO: Implement key transformation
      case _ =>
        Left(MigrationError.Generic(s"TransformKeys not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyTransformValues(at: DynamicOptic, transform: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    value match {
      case dv: DynamicValue.Dict =>
        Right(dv) // TODO: Implement value transformation
      case _ =>
        Left(MigrationError.Generic(s"TransformValues not supported for path ${at.show}", Some(at)))
    }
  }

  private def applyJoin(at: DynamicOptic, sourcePaths: Vector[DynamicOptic], combiner: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    Right(value) // TODO: Implement join logic
  }

  private def applySplit(at: DynamicOptic, targetPaths: Vector[DynamicOptic], splitter: SchemaExpr[_], value: DynamicValue): Either[MigrationError, DynamicValue] = {
    Right(value) // TODO: Implement split logic
  }

  private def evaluateSchemaExpr(expr: SchemaExpr[_]): DynamicValue = {
    expr match {
      case SchemaExpr.Const(v) => DynamicValue.fromAny(v)
      case SchemaExpr.Identity() => DynamicValue.Null
      case SchemaExpr.DefaultValue => DynamicValue.Null
      case _ => DynamicValue.Null
    }
  }

  def ++(that: DynamicMigration): DynamicMigration = DynamicMigration(this.actions ++ that.actions)
  def reverse: DynamicMigration = DynamicMigration(actions.reverse.map(_.reverse))
}

final case class Migration[A, B](
  dynamicMigration: DynamicMigration,
  sourceSchema: Schema[A],
  targetSchema: Schema[B]
) {
  def apply(value: A): Either[MigrationError, B] = {
    // Convert A to DynamicValue
    val sourceDynamic = sourceSchema.toDynamic(value)
    // Apply migration to DynamicValue
    val result = dynamicMigration.apply(sourceDynamic)
    // Convert result back to B
    result.flatMap { targetDynamic =>
      targetSchema.fromDynamic(targetDynamic)
    }
  }

  def ++[C](that: Migration[B, C]): Migration[A, C] =
    Migration(this.dynamicMigration ++ that.dynamicMigration, this.sourceSchema, that.targetSchema)

  def andThen[C](that: Migration[B, C]): Migration[A, C] = this ++ that

  def reverse: Migration[B, A] =
    Migration(dynamicMigration.reverse, targetSchema, sourceSchema)
}
