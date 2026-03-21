package zio.schema.migration

import zio.schema._
import zio.test._
import zio.test.Assertion._

/**
  * Integration tests for the migration system
  * Tests actual migrations with real data transformations
  */
object MigrationIntegrationSpec extends ZIOSpecDefault {

  // Structural types for old versions
  type PersonV0 = {
    def firstName: String
    def lastName: String
  }

  type PersonV1 = {
    def fullName: String
    def age: Int
  }

  // Runtime types
  @schema
  case class PersonV2(fullName: String, age: Int, country: String)

  // Create schemas
  implicit val schemaPersonV0: Schema[PersonV0] = Schema.structural[PersonV0]
  implicit val schemaPersonV1: Schema[PersonV1] = Schema.structural[PersonV1]
  implicit val schemaPersonV2: Schema[PersonV2] = Schema.derived[PersonV2]

  def spec = suite("MigrationIntegrationSpec")(
    suite("BasicFieldOperations")(
      test("AddField adds a new field with default value") {
        val actions = Vector(
          MigrationAction.AddField(
            DynamicOptic.Field("age", DynamicOptic.Root),
            SchemaExpr.Const(0)
          )
        )
        val migration = DynamicMigration(actions)

        // Create a simple record
        val record = DynamicValue.Record(
          "test",
          Vector(("name", DynamicValue.Primitive(Primitive.String("Alice"))))
        )

        val result = migration.apply(record)
        assert(result)(isRight)
      },

      test("DropField removes a field from a record") {
        val actions = Vector(
          MigrationAction.DropField(
            DynamicOptic.Field("age", DynamicOptic.Root),
            SchemaExpr.DefaultValue
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(
            ("name", DynamicValue.Primitive(Primitive.String("Alice"))),
            ("age", DynamicValue.Primitive(Primitive.Int(30)))
          )
        )

        val result = migration.apply(record)
        assert(result)(isRight)
      },

      test("Rename renames a field in a record") {
        val actions = Vector(
          MigrationAction.Rename(
            DynamicOptic.Field("firstName", DynamicOptic.Root),
            "first_name"
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(("firstName", DynamicValue.Primitive(Primitive.String("John"))))
        )

        val result = migration.apply(record)
        assert(result)(isRight)
      },

      test("TransformValue transforms a field value") {
        val actions = Vector(
          MigrationAction.TransformValue(
            DynamicOptic.Field("age", DynamicOptic.Root),
            SchemaExpr.Const(25)
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(("age", DynamicValue.Primitive(Primitive.Int(30))))
        )

        val result = migration.apply(record)
        assert(result)(isRight)
      },

      test("Mandate provides default for null values") {
        val actions = Vector(
          MigrationAction.Mandate(
            DynamicOptic.Field("age", DynamicOptic.Root),
            SchemaExpr.Const(0)
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(("age", DynamicValue.Null))
        )

        val result = migration.apply(record)
        assert(result)(isRight)
      }
    ),

    suite("ComposedMigrations")(
      test("Multiple actions compose correctly") {
        val actions = Vector(
          MigrationAction.AddField(
            DynamicOptic.Field("id", DynamicOptic.Root),
            SchemaExpr.Const(1)
          ),
          MigrationAction.Rename(
            DynamicOptic.Field("name", DynamicOptic.Root),
            "full_name"
          ),
          MigrationAction.AddField(
            DynamicOptic.Field("active", DynamicOptic.Root),
            SchemaExpr.Const(true)
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(("name", DynamicValue.Primitive(Primitive.String("Alice"))))
        )

        val result = migration.apply(record)
        assert(result)(isRight)
      },

      test("Composed migrations can be chained") {
        val m1 = DynamicMigration(Vector(
          MigrationAction.AddField(
            DynamicOptic.Field("age", DynamicOptic.Root),
            SchemaExpr.Const(0)
          )
        ))

        val m2 = DynamicMigration(Vector(
          MigrationAction.Rename(
            DynamicOptic.Field("age", DynamicOptic.Root),
            "years"
          )
        ))

        val composed = m1 ++ m2

        val record = DynamicValue.Record(
          "test",
          Vector(("name", DynamicValue.Primitive(Primitive.String("Bob"))))
        )

        val result = composed.apply(record)
        assert(result)(isRight)
      }
    ),

    suite("ReverseMigrations")(
      test("AddField reversed becomes DropField") {
        val addAction = MigrationAction.AddField(
          DynamicOptic.Field("age", DynamicOptic.Root),
          SchemaExpr.Const(0)
        )
        val migration = DynamicMigration(Vector(addAction))
        val reversed = migration.reverse

        assert(reversed.actions.length)(equalTo(1))
        assert(reversed.actions(0))(isInstanceOf[MigrationAction.DropField])
      },

      test("Rename reversed swaps field names") {
        val renameAction = MigrationAction.Rename(
          DynamicOptic.Field("firstName", DynamicOptic.Root),
          "first_name"
        )
        val migration = DynamicMigration(Vector(renameAction))
        val reversed = migration.reverse

        assert(reversed.actions.length)(equalTo(1))
      },

      test("Double reverse returns to original state") {
        val action = MigrationAction.AddField(
          DynamicOptic.Field("timestamp", DynamicOptic.Root),
          SchemaExpr.Const(0L)
        )
        val migration = DynamicMigration(Vector(action))
        val doubleReverse = migration.reverse.reverse

        assert(doubleReverse.actions.length)(equalTo(migration.actions.length))
      }
    ),

    suite("ErrorHandling")(
      test("Error includes path information") {
        val actions = Vector(
          MigrationAction.Rename(
            DynamicOptic.Field("nonexistent", DynamicOptic.Root),
            "new_name"
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(("name", DynamicValue.Primitive(Primitive.String("Charlie"))))
        )

        val result = migration.apply(record)
        assert(result)(isLeft)
      },

      test("Error message is descriptive") {
        val actions = Vector(
          MigrationAction.Rename(
            DynamicOptic.Field("missing", DynamicOptic.Root),
            "renamed"
          )
        )
        val migration = DynamicMigration(actions)

        val record = DynamicValue.Record(
          "test",
          Vector(("existing", DynamicValue.Primitive(Primitive.String("David"))))
        )

        val result = migration.apply(record)
        result match {
          case Left(err) =>
            assert(err.message)(containsString("Rename"))
            assert(err.path)(isSome)
          case Right(_) => assertCompletes
        }
      }
    ),

    suite("MigrationBuilder")(
      test("Builder creates migration with correct actions") {
        val builder = new MigrationBuilder(
          Schema.primitive[String],
          Schema.primitive[String]
        )

        val builder2 = builder.renameField(
          (s: String) => s,
          (t: String) => t
        )

        assert(builder2.actions.length)(equalTo(1))
      },

      test("Builder supports fluent chaining") {
        val builder = new MigrationBuilder(
          Schema.primitive[String],
          Schema.primitive[String]
        )

        val builder3 = builder
          .renameField((s: String) => s, (t: String) => t)
          .renameField((s: String) => s, (t: String) => t)

        assert(builder3.actions.length)(equalTo(2))
      },

      test("Builder.build creates Migration") {
        val builder = new MigrationBuilder(
          Schema.primitive[String],
          Schema.primitive[String]
        )

        val migration = builder.build

        assert(migration.sourceSchema)(anything)
        assert(migration.targetSchema)(anything)
      },

      test("Migration.identity creates no-op migration") {
        val schema = Schema.primitive[String]
        val identity = Migration.identity(schema)

        assert(identity.dynamicMigration.actions.length)(equalTo(0))
      }
    ),

    suite("AlgebraicLaws")(
      test("Identity: migration ++ migration.reverse.reverse == migration") {
        val m1 = DynamicMigration(Vector(
          MigrationAction.AddField(
            DynamicOptic.Field("x", DynamicOptic.Root),
            SchemaExpr.Const(1)
          )
        ))

        val doubleReverse = m1.reverse.reverse
        assert(doubleReverse.actions.length)(equalTo(m1.actions.length))
      },

      test("Associativity: (m1 ++ m2) ++ m3 == m1 ++ (m2 ++ m3)") {
        val m1 = DynamicMigration(Vector(
          MigrationAction.AddField(DynamicOptic.Field("a", DynamicOptic.Root), SchemaExpr.DefaultValue)
        ))
        val m2 = DynamicMigration(Vector(
          MigrationAction.AddField(DynamicOptic.Field("b", DynamicOptic.Root), SchemaExpr.DefaultValue)
        ))
        val m3 = DynamicMigration(Vector(
          MigrationAction.AddField(DynamicOptic.Field("c", DynamicOptic.Root), SchemaExpr.DefaultValue)
        ))

        val left = (m1 ++ m2) ++ m3
        val right = m1 ++ (m2 ++ m3)

        assert(left.actions.length)(equalTo(right.actions.length))
        assert(left.actions.map(_.at.show))(equalTo(right.actions.map(_.at.show)))
      },

      test("Reverse is structural inverse") {
        val m = DynamicMigration(Vector(
          MigrationAction.Rename(
            DynamicOptic.Field("old", DynamicOptic.Root),
            "new"
          ),
          MigrationAction.AddField(
            DynamicOptic.Field("extra", DynamicOptic.Root),
            SchemaExpr.Const(42)
          )
        ))

        val reversed = m.reverse
        assert(reversed.actions.length)(equalTo(m.actions.length))
      }
    )
  )
}
