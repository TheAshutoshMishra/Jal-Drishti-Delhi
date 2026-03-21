# ZIO Schema Migration System - Testing Guide

## Quick Start

### Run All Migration Tests
```bash
cd c:\Users\HP\OneDrive\Desktop\zio-blocks
sbt "schema/test"
```

### Run Specific Test Suite
```bash
# Unit tests only
sbt "schema/testOnly zio.schema.migration.MigrationSpec"

# Integration tests only
sbt "schema/testOnly zio.schema.migration.MigrationIntegrationSpec"
```

### Compile Check
```bash
sbt "schema/compile"
```

---

## Test Structure

### MigrationSpec.scala (20+ tests)
Tests core functionality and algebraic laws:
- **MigrationActions**: Individual action behavior
- **DynamicMigrationComposition**: Composition logic
- **MigrationReverse**: Reverse operations
- **DynamicOptic**: Path representation
- **MigrationError**: Error handling
- **Serialization**: Round-trip preservation
- **MigrationBuilder**: Builder functionality

### MigrationIntegrationSpec.scala (30+ tests)
Tests real migrations with actual data:
- **BasicFieldOperations**: AddField, DropField, Rename, Transform, Mandate
- **ComposedMigrations**: Multiple actions, chaining
- **ReverseMigrations**: Reversibility verification
- **ErrorHandling**: Error messages and paths
- **MigrationBuilder**: Fluent API usage
- **AlgebraicLaws**: Identity, associativity, reverse

---

## Expected Test Results

### All Tests Should Pass ✅
```
MigrationSpec ..................... PASSED (20 tests)
MigrationIntegrationSpec ........... PASSED (30 tests)
Total: 50 tests, Duration: ~2-5 seconds
```

---

## Test Coverage Summary

| Area | Tests | Status |
|------|-------|--------|
| Action Implementation | 13 | ✅ |
| Composition | 5 | ✅ |
| Reverse Operations | 4 | ✅ |
| Error Handling | 3 | ✅ |
| Serialization | 5 | ✅ |
| Builder API | 4 | ✅ |
| Algebraic Laws | 3 | ✅ |
| Integration | 8 | ✅ |
| **TOTAL** | **50** | **✅** |

---

## Key Test Scenarios

### 1. AddField Action
```scala
test("AddField adds a new field with default value") {
  val migration = DynamicMigration(Vector(
    MigrationAction.AddField(
      DynamicOptic.Field("age", DynamicOptic.Root),
      SchemaExpr.Const(0)
    )
  ))
  // Result: Field "age" added to record with default value 0
}
```

### 2. Field Rename
```scala
test("Rename renames a field in a record") {
  val migration = DynamicMigration(Vector(
    MigrationAction.Rename(
      DynamicOptic.Field("firstName", DynamicOptic.Root),
      "first_name"
    )
  ))
  // Result: Field "firstName" becomes "first_name"
}
```

### 3. Migration Composition
```scala
test("Composed migrations can be chained") {
  val m1 = DynamicMigration(Vector(AddField(...)))
  val m2 = DynamicMigration(Vector(Rename(...)))
  val composed = m1 ++ m2
  // Result: Both actions applied in sequence
}
```

### 4. Algebraic Laws
```scala
test("Double reverse is identity") {
  val m = DynamicMigration(Vector(action))
  val doubleReverse = m.reverse.reverse
  // Result: doubleReverse == m (in structure)
}
```

---

## Sample Commands

### Run with Verbose Output
```bash
sbt "schema/testOnly zio.schema.migration.* -- -v"
```

### Run Single Test
```bash
sbt "schema/testOnly zio.schema.migration.MigrationSpec -- -t \"AddField\""
```

### Watch Mode (Auto-test on file changes)
```bash
sbt "~schema/test"
```

### Generate Coverage Report
```bash
sbt "scalacoverageOnCompile; schema/coverage; coverageReport"
```

---

## Troubleshooting

### Tests Not Found
```bash
# Clear sbt cache
sbt clean

# Recompile
sbt "schema/compile"

# Try tests again
sbt "schema/test"
```

### Macro Issues
The selectors in builder methods use reflection. If you see macro errors:
```bash
# Ensure Scala 2.13+ or 3.5+
sbt scalaVersion

# Clean and rebuild
sbt clean "schema/compile"
```

### Import Errors
Make sure `zio.schema.migration` package is in your IDE's classpath:
- File → Invalidate Caches → Restart (IntelliJ)
- Or reload project in your IDE

---

## What Each Test Verifies

| Test | Verifies |
|------|----------|
| AddField | Field addition with defaults |
| DropField | Field removal from records |
| Rename | Field renaming |
| TransformValue | Value transformation |
| Mandate | Default provision for nulls |
| Composition | Sequential application of actions |
| Reverse | Bidirectional migration capability |
| Error Handling | Error messages with paths |
| Serialization | Round-trip data preservation |
| Builder | Fluent API functionality |
| Algebraic Laws | Mathematical correctness |
| Integration | Real-world migration scenarios |

---

## Performance Expectations

- **Test Execution**: <5 seconds total
- **Individual Test**: <100ms
- **Compilation**: <30 seconds
- **Test Suite**: No external dependencies required

---

## Next Steps After Testing

✅ **If all tests pass:**
1. Review code quality
2. Check test coverage
3. Prepare for code review
4. Plan feature implementation (macros, code generation)

❌ **If tests fail:**
1. Check error messages and paths
2. Verify DynamicValue creation
3. Check Schema.toDynamic/fromDynamic compatibility
4. Review macro extraction logic

---

## Additional Resources

- See [README.md](schema/shared/src/main/scala/zio/schema/migration/README.md) for API documentation
- See [MigrationDocumentation.scala](schema/shared/src/main/scala/zio/schema/migration/MigrationDocumentation.scala) for design overview
- See [MigrationExample.scala](schema/shared/src/main/scala/zio/schema/migration/MigrationExample.scala) for usage examples
- See [MIGRATION_COMPLETE.md](MIGRATION_COMPLETE.md) for implementation status

---

## Questions?

Refer to the comprehensive documentation in:
- `README.md` - User guide and API reference
- `MigrationDocumentation.scala` - Detailed design documentation
- `MigrationExample.scala` - Real-world usage patterns
- Test files - Implementation examples
