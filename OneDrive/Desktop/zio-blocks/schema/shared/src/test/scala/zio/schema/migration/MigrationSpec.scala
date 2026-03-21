package zio.schema.migration

import zio.schema._
import zio.test._
import zio.test.Assertion._

object MigrationSpec extends ZIOSpecDefault {
  def spec = suite("MigrationSpec")(
    suite("MigrationActions")(
      test("AddField action creates with correct optic") {
        val action = MigrationAction.AddField(
          DynamicOptic.Field("age", DynamicOptic.Root),
          SchemaExpr.Const(0)
        )

        assert(action.at.show)(equalTo(".age"))
      },

      test("DropField action creates with correct optic") {
        val action = MigrationAction.DropField(
          DynamicOptic.Field("age", DynamicOptic.Root),
          SchemaExpr.DefaultValue
        )

        assert(action.at.show)(equalTo(".age"))
      },

      test("Rename action creates with new name") {
        val action = MigrationAction.Rename(
          DynamicOptic.Field("firstName", DynamicOptic.Root),
          "first_name"
        )

        assert(action)(isInstanceOf[MigrationAction.Rename](
          hasField("to", (a: MigrationAction.Rename) => a.to, equalTo("first_name"))
        ))
      },

      test("Mandate action creates with default") {
        val action = MigrationAction.Mandate(
          DynamicOptic.Field("age", DynamicOptic.Root),
          SchemaExpr.Const(0)
        )

        assert(action.at.show)(equalTo(".age"))
      },

      test("Optionalize action creates correctly") {
        val action = MigrationAction.Optionalize(
          DynamicOptic.Field("age", DynamicOptic.Root)
        )

        assert(action.at.show)(equalTo(".age"))
      },

      test("RenameCase reverses correctly") {
        val action = MigrationAction.RenameCase(
          DynamicOptic.Root,
          "CreditCard",
          "Card"
        )
        val reversed = action.reverse

        assert(reversed)(isInstanceOf[MigrationAction.RenameCase](
          hasField("from", (a: MigrationAction.RenameCase) => a.from, equalTo("Card"))
        ))
      }
    ),

    suite("DynamicMigrationComposition")(
      test("DynamicMigration.++ composes migrations") {
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

        assert(composed.actions.length)(equalTo(2))
      },

      test("DynamicMigration.++ is associative") {
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

        assert(left.actions.length)(equalTo(right.actions.length)) &&
        assert(left.actions.map(_.at.show))(equalTo(right.actions.map(_.at.show)))
      }
    ),

    suite("MigrationReverse")(
      test("DynamicMigration.reverse reverses migrations") {
        val addAction = MigrationAction.AddField(
          DynamicOptic.Field("age", DynamicOptic.Root),
          SchemaExpr.Const(0)
        )
        val dm = DynamicMigration(Vector(addAction))
        val reversed = dm.reverse

        assert(reversed.actions.length)(equalTo(1))
      },

      test("Double reverse is identity") {
        val action = MigrationAction.Rename(
          DynamicOptic.Field("name", DynamicOptic.Root),
          "fullName"
        )
        val migration = DynamicMigration(Vector(action))
        val doubleReverse = migration.reverse.reverse

        assert(doubleReverse.actions.length)(equalTo(migration.actions.length))
      },

      test("Reverse of Rename swaps from and to") {
        val renameAction = MigrationAction.Rename(
          DynamicOptic.Field("name", DynamicOptic.Root),
          "fullName"
        )
        val reversed = renameAction.reverse

        assert(reversed)(isInstanceOf[MigrationAction.Rename]) &&
        assert(reversed.at)(equalTo(renameAction.at))
      }
    ),

    suite("DynamicOptic")(
      test("DynamicOptic.show produces correct string representation") {
        val field = DynamicOptic.Field("name", DynamicOptic.Root)
        assert(field.show)(equalTo(".name"))
      },

      test("Nested field path displays correctly") {
        val nested = DynamicOptic.Field("street", DynamicOptic.Field("address", DynamicOptic.Root))
        assert(nested.show)(equalTo(".address.street"))
      },

      test("Root optic shows as dot") {
        assert(DynamicOptic.Root.show)(equalTo("."))
      },

      test("Case optic shows correctly") {
        val caseOptic = DynamicOptic.Case("CreditCard", DynamicOptic.Root)
        assert(caseOptic.show)(containsString("when[CreditCard]"))
      },

      test("Each optic shows correctly") {
        val eachOptic = DynamicOptic.Each(DynamicOptic.Root)
        assert(eachOptic.show)(containsString("each"))
      }
    ),

    suite("MigrationError")(
      test("MigrationError captures path information") {
        val at = DynamicOptic.Field("age", DynamicOptic.Root)
        val err = MigrationError.Generic("Test error", Some(at))

        assert(err.path)(isSome(anything)) &&
        assert(err.message)(equalTo("Test error"))
      },

      test("Error message is accessible") {
        val err = MigrationError.Generic("Cannot transform field")
        assert(err.message)(equalTo("Cannot transform field"))
      }
    ),

    suite("Serialization")(
      test("Serialization round-trip preserves DynamicOptic") {
        val optic = DynamicOptic.Field("name", DynamicOptic.Root)
        val serialized = DynamicOpticSerialization.serialize(optic)
        val deserialized = DynamicOpticSerialization.deserialize(serialized)

        assert(deserialized)(isRight)
      },

      test("Serialized DynamicOptic can be deserialized") {
        val optic = DynamicOptic.Field("firstName", DynamicOptic.Root)
        val serialized = DynamicOpticSerialization.serialize(optic)
        val result = DynamicOpticSerialization.deserialize(serialized)

        assert(result)(isRight(anything))
      },

      test("Serialization round-trip preserves MigrationAction") {
        val action = MigrationAction.Rename(
          DynamicOptic.Field("name", DynamicOptic.Root),
          "fullName"
        )
        val serialized = MigrationActionSerialization.serialize(action)
        val deserialized = MigrationActionSerialization.deserialize(serialized)

        assert(deserialized)(isRight)
      },

      test("SchemaExpr serializes correctly") {
        val expr = SchemaExpr.Const(42)
        val serialized = SchemaExprSerialization.serialize(expr)

        assert(serialized)(containsString("Const"))
      },

      test("DefaultValue serializes and deserializes") {
        val expr = SchemaExpr.DefaultValue
        val serialized = SchemaExprSerialization.serialize(expr)
        val result = SchemaExprSerialization.deserialize(serialized)

        assert(result)(isRight(anything))
      }
    ),

    suite("MigrationBuilder")(
      test("MigrationBuilder creates actions from builder calls") {
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

      test("MigrationBuilder.build creates Migration") {
        val builder = new MigrationBuilder(
          Schema.primitive[String],
          Schema.primitive[String]
        )
        val migration = builder.build

        assert(migration.sourceSchema)(anything) &&
        assert(migration.targetSchema)(anything)
      },

      test("Migration.identity creates no-op migration") {
        val schema = Schema.primitive[String]
        val identity = Migration.identity(schema)

        assert(identity.dynamicMigration.actions.length)(equalTo(0))
      }
    )
  )
}

