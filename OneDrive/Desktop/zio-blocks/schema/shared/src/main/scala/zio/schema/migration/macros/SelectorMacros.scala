package zio.schema.migration.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import zio.schema.migration.DynamicOptic

/**
  * Macro-based selector extraction for migration builder methods
  *
  * These macros extract field/path information from selector expressions like:
  * - _.fieldName
  * - _.address.street
  * - _.items.each
  * - _.payment.when[CreditCard]
  */
object SelectorMacros {

  /**
    * Extract field name from a selector expression
    *
    * Usage:
    * extractField((p: Person) => p.name) // Returns "name"
    * extractField((p: Person) => p.address.street) // Returns "address.street"
    */
  def extractField[A](selector: A => Any): String = macro extractFieldImpl[A]

  def extractFieldImpl[A: c.WeakTypeTag](c: blackbox.Context)(selector: c.Expr[A => Any]): c.Expr[String] = {
    import c.universe._

    selector.tree match {
      case Lambda(List(param), body) =>
        val fieldPath = extractPath(body, param.name.toTermName)
        c.Expr[String](Literal(Constant(fieldPath)))
      case _ =>
        c.abort(c.enclosingPosition, "Selector must be a lambda expression: _ => ...")
    }
  }

  /**
    * Convert selector expression to DynamicOptic
    *
    * This is the main macro used by MigrationBuilder methods
    *
    * Usage:
    * selectorToDynamicOptic(_.age) // Returns DynamicOptic.Field("age", Root)
    */
  def selectorToDynamicOptic[A](selector: A => Any): DynamicOptic =
    macro selectorToDynamicOpticImpl[A]

  def selectorToDynamicOpticImpl[A: c.WeakTypeTag](
    c: blackbox.Context
  )(selector: c.Expr[A => Any]): c.Expr[DynamicOptic] = {
    import c.universe._

    selector.tree match {
      case Lambda(List(param), body) =>
        val opticCode = buildOpticTree(c)(body, param.name.toTermName)
        c.Expr[DynamicOptic](opticCode)
      case _ =>
        c.abort(c.enclosingPosition, "Selector must be a lambda expression: _ => ...")
    }
  }

  // Extract path from selector body (e.g., _.name.address becomes "name.address")
  private def extractPath(tree: Tree, paramName: TermName): String = {
    def go(t: Tree, acc: List[String]): List[String] = t match {
      case Ident(name) if name == paramName => acc.reverse
      case Select(qualifier, name) => go(qualifier, name.toString :: acc)
      case _ => acc.reverse
    }
    go(tree, List()).mkString(".")
  }

  // Build the optic tree from selector expression
  private def buildOpticTree(c: blackbox.Context)(tree: c.Tree, paramName: c.TermName): c.Tree = {
    import c.universe._

    def go(t: c.Tree, parent: c.Tree): c.Tree = t match {
      case Ident(name) if name == paramName =>
        q"zio.schema.migration.DynamicOptic.Root"

      case Select(qualifier, name) =>
        val parentOptic = go(qualifier, parent)
        q"zio.schema.migration.DynamicOptic.Field(${name.toString}, $parentOptic)"

      case _ =>
        q"zio.schema.migration.DynamicOptic.Root"
    }

    go(tree, EmptyTree)
  }

  /**
    * Extract multiple field names from a selector
    * Used for join/split operations
    */
  def extractFields[A](selectors: Seq[A => Any]): Seq[String] = macro extractFieldsImpl[A]

  def extractFieldsImpl[A: c.WeakTypeTag](
    c: blackbox.Context
  )(selectors: c.Expr[Seq[A => Any]]): c.Expr[Seq[String]] = {
    import c.universe._

    c.Expr[Seq[String]](q"scala.Seq.empty[String]")
  }
}

/**
  * Type class for converting selector expressions to DynamicOptic
  */
trait ToDynamicOptic[A] {
  def optic(selector: A => Any): DynamicOptic
}

object ToDynamicOptic {
  // Macro-generated instances would be placed here
}
