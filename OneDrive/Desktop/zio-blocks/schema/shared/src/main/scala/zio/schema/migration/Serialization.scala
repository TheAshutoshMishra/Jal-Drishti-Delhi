package zio.schema.migration

import zio.schema._
import scala.util.Try

object DynamicOpticSerialization {
  def serialize(optic: DynamicOptic): String = optic.show

  def deserialize(str: String): Either[MigrationError, DynamicOptic] = {
    // Parse string representation back into DynamicOptic
    // Simple parser for now - handles basic cases
    Try {
      if (str == ".") DynamicOptic.Root
      else if (str.startsWith(".")) {
        val parts = str.drop(1).split("\\.").toList
        parts.foldLeft[DynamicOptic](DynamicOptic.Root) { (parent, part) =>
          if (part.contains("[")) {
            val tag = part.dropWhile(_ != '[').drop(1).dropRight(1)
            DynamicOptic.Case(tag, parent)
          } else if (part == "each") {
            DynamicOptic.Each(parent)
          } else {
            DynamicOptic.Field(part, parent)
          }
        }
      } else {
        DynamicOptic.Root
      }
    }.toEither.left.map(e => MigrationError.Generic(s"Failed to deserialize optic: ${e.getMessage}"))
  }
}

object SchemaExprSerialization {
  def serialize(expr: SchemaExpr[_]): String = {
    expr match {
      case SchemaExpr.DefaultValue => "DefaultValue"
      case SchemaExpr.Const(v)      => s"Const($v)"
      case SchemaExpr.Identity()    => "Identity"
      case _                        => "Unknown"
    }
  }

  def deserialize(str: String): Either[MigrationError, SchemaExpr[_]] = {
    Try {
      str match {
        case "DefaultValue"         => SchemaExpr.DefaultValue
        case "Identity"             => SchemaExpr.Identity()
        case s if s.startsWith("Const(") && s.endsWith(")") =>
          val value = s.drop(6).dropRight(1)
          SchemaExpr.Const(value)
        case _ => SchemaExpr.DefaultValue
      }
    }.toEither.left.map(e => MigrationError.Generic(s"Failed to deserialize SchemaExpr: ${e.getMessage}"))
  }
}

object MigrationActionSerialization {
  def serialize(action: MigrationAction): Map[String, String] = {
    action match {
      case MigrationAction.AddField(at, default) =>
        Map(
          "type" -> "AddField",
          "at" -> DynamicOpticSerialization.serialize(at),
          "default" -> SchemaExprSerialization.serialize(default)
        )
      case MigrationAction.DropField(at, defaultForReverse) =>
        Map(
          "type" -> "DropField",
          "at" -> DynamicOpticSerialization.serialize(at),
          "defaultForReverse" -> SchemaExprSerialization.serialize(defaultForReverse)
        )
      case MigrationAction.Rename(at, to) =>
        Map(
          "type" -> "Rename",
          "at" -> DynamicOpticSerialization.serialize(at),
          "to" -> to
        )
      case MigrationAction.TransformValue(at, transform) =>
        Map(
          "type" -> "TransformValue",
          "at" -> DynamicOpticSerialization.serialize(at),
          "transform" -> SchemaExprSerialization.serialize(transform)
        )
      case MigrationAction.Mandate(at, default) =>
        Map(
          "type" -> "Mandate",
          "at" -> DynamicOpticSerialization.serialize(at),
          "default" -> SchemaExprSerialization.serialize(default)
        )
      case MigrationAction.Optionalize(at) =>
        Map(
          "type" -> "Optionalize",
          "at" -> DynamicOpticSerialization.serialize(at)
        )
      case MigrationAction.RenameCase(at, from, to) =>
        Map(
          "type" -> "RenameCase",
          "at" -> DynamicOpticSerialization.serialize(at),
          "from" -> from,
          "to" -> to
        )
      case _ => Map("type" -> "Unknown")
    }
  }

  def deserialize(data: Map[String, String]): Either[MigrationError, MigrationAction] = {
    val actionType = data.get("type")
    actionType match {
      case Some("AddField") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
          default <- SchemaExprSerialization.deserialize(data("default"))
        } yield MigrationAction.AddField(at, default)
      case Some("DropField") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
          defaultForReverse <- SchemaExprSerialization.deserialize(data("defaultForReverse"))
        } yield MigrationAction.DropField(at, defaultForReverse)
      case Some("Rename") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
        } yield MigrationAction.Rename(at, data("to"))
      case Some("TransformValue") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
          transform <- SchemaExprSerialization.deserialize(data("transform"))
        } yield MigrationAction.TransformValue(at, transform)
      case Some("Mandate") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
          default <- SchemaExprSerialization.deserialize(data("default"))
        } yield MigrationAction.Mandate(at, default)
      case Some("Optionalize") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
        } yield MigrationAction.Optionalize(at)
      case Some("RenameCase") =>
        for {
          at <- DynamicOpticSerialization.deserialize(data("at"))
        } yield MigrationAction.RenameCase(at, data("from"), data("to"))
      case _ =>
        Left(MigrationError.Generic(s"Unknown action type: ${actionType.getOrElse("null")}"))
    }
  }
}

object DynamicMigrationSerialization {
  def toJson(dm: DynamicMigration): String = {
    val actionsList = dm.actions.map(MigrationActionSerialization.serialize(_))
    // Simple JSON representation
    val json = new StringBuilder()
    json.append("{\n  \"actions\": [\n")
    actionsList.zipWithIndex.foreach {
      case (actionMap, idx) =>
        json.append("    {\n")
        actionMap.foreach {
          case (key, value) =>
            json.append(s"""      "$key": "$value"""")
            if (actionMap.keys.last != key) json.append(",\n")
            else json.append("\n")
        }
        json.append("    }")
        if (idx < actionsList.length - 1) json.append(",\n")
        else json.append("\n")
    }
    json.append("  ]\n}")
    json.toString
  }

  def fromJson(json: String): Either[MigrationError, DynamicMigration] = {
    // Simple JSON parser - for production, use a proper JSON library
    Try {
      val lines = json.split("\n").map(_.trim).filter(_.nonEmpty)
      val actions = scala.collection.mutable.ListBuffer[MigrationAction]()
      // This is a placeholder - real implementation would use proper JSON parsing
      DynamicMigration(actions.toVector)
    }.toEither.left.map(e => MigrationError.Generic(s"Failed to parse JSON: ${e.getMessage}"))
  }
}
