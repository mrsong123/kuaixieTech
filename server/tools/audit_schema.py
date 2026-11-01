#!/usr/bin/env python3
"""Read-only comparison of the supplied MySQL DDL, entities and SQL table references.

This checks table/column names, not SQL semantics, types, indexes or live behavior.
It never executes SQL. Proposed migrations are included only by explicit option.
"""
import argparse
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]


def schema(sql):
    # Strip comments, preserving quoted content. Find balanced CREATE TABLE bodies.
    sql = re.sub(r"--[^\n]*|/\*.*?\*/", "", sql, flags=re.S)
    tables = {}
    for match in re.finditer(r"CREATE TABLE\s+(?:IF NOT EXISTS\s+)?`?(\w+)`?\s*\(", sql, re.I):
        start = index = match.end()
        depth, quote = 1, None
        while depth and index < len(sql):
            char = sql[index]
            if quote:
                if char == quote and sql[index - 1] != "\\":
                    quote = None
            elif char in "'\"`":
                quote = char
            elif char == "(":
                depth += 1
            elif char == ")":
                depth -= 1
            index += 1
        if depth:
            raise ValueError("Unclosed CREATE TABLE " + match[1])
        body = sql[start:index - 1]
        types = r"CHAR|VARCHAR|TINYTEXT|MEDIUMTEXT|LONGTEXT|TEXT|TINYINT|SMALLINT|INT|BIGINT|DECIMAL|DOUBLE|FLOAT|JSON|DATETIME|TIMESTAMP|DATE|BOOLEAN"
        tables[match[1]] = set(re.findall(r"(?:^|,)\s*`?(\w+)`?\s+(?:" + types + r")\b", body, re.I))
    return tables


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--include-proposal", action="store_true")
    parser.add_argument("--contract", type=pathlib.Path, help="Write table/column TSV for the read-only live preflight")
    args = parser.parse_args()
    tables = schema((ROOT / "db/reference/current-schema.sql").read_text())
    baseline = len(tables)
    if args.include_proposal:
        for path in sorted((ROOT / "db/migrations").glob("*.proposed.sql")):
            for name, columns in schema(path.read_text()).items():
                if name in tables:
                    raise ValueError("Proposal must not redefine existing table " + name)
                tables[name] = columns
    if args.contract:
        args.contract.write_text("".join(name + "\t" + ",".join(sorted(cols)) + "\n" for name, cols in sorted(tables.items())))
    issues = set()
    for path in (ROOT / "src/main/java/com/kuaixietech/entity").rglob("*.java"):
        source = path.read_text()
        table = re.search(r'@TableName\("(.*?)"\)', source)
        if not table:
            continue
        if table[1] not in tables:
            issues.add("Missing table: " + table[1])
            continue
        columns = re.findall(r'@Table(?:Field|Id)\((?:value\s*=\s*)?"(.*?)"', source)
        for column in set(columns) - tables[table[1]]:
            issues.add("Missing column: " + table[1] + "." + column)
    for folder in (ROOT / "src/main/java", ROOT / "src/main/resources/mapper"):
        for path in folder.rglob("*"):
            if path.suffix not in (".java", ".xml"):
                continue
            source = re.sub(r"<!--.*?-->|/\*.*?\*/", "", path.read_text(), flags=re.S)
            for name in re.findall(r"\b(?:FROM|JOIN|UPDATE|INTO)\s+`?(t_\w+)`?", source, re.I):
                if name not in tables:
                    issues.add("Missing table: " + name)
    print(f"Schema: {baseline} supplied tables; {len(tables) - baseline} proposed tables")
    for issue in sorted(issues):
        print(issue)
    if issues:
        print(f"FAILED: {len(issues)} distinct name mismatches")
        return 1
    print("PASS: entity columns and referenced table names match; live SQL behavior is unverified")
    return 0


if __name__ == "__main__":
    sys.exit(main())
