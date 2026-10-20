"""Verificação de migração e SQL sobre uma cópia do banco real (não exige Android)."""
import re
import sqlite3
import tempfile
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/assets/database/recipie.db"
CODE = ROOT / "app/src/main/java/com/example/recipie"


def queries(file):
    text = file.read_text()
    return [a or b for a, b in re.findall(r'@Query\((?:"""(.*?)"""|"([^"\n]*)")\)', text, re.S)]


def migration_sql():
    text = (CODE / "data/local/database/RecipieDatabaseProvider.kt").read_text()
    return [a or b for a, b in re.findall(r'db.execSQL\((?:"""(.*?)"""|"([^"\n]*)")\)', text, re.S)]


def run():
    with tempfile.TemporaryDirectory(prefix="recipie-check-") as directory:
        original = sqlite3.connect(f"file:{SOURCE}?mode=ro", uri=True)
        db = sqlite3.connect(Path(directory) / "test.db")
        original.backup(db)
        db.execute("PRAGMA foreign_keys = ON")
        before = {table: db.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0]
                  for table in ["recipes", "ingredientes", "recipe_ingredients"]}
        # Hash do conteúdo integral, inclusive linhas repetidas, antes/depois da migração.
        import hashlib
        def content_hash(table, columns):
            digest = hashlib.sha256()
            for row in db.execute(f"SELECT {columns} FROM {table} ORDER BY rowid"):
                digest.update(repr(row).encode())
            return digest.hexdigest()
        hashes_before = {"recipes": content_hash("recipes", "*"),
                         "ingredientes": content_hash("ingredientes", "ingredient_id, ingredient_name"),
                         "recipe_ingredients": content_hash("recipe_ingredients", "recipe_id, ingredient_id, quantity, unit, preparation_note")}
        with db:
            for sql in migration_sql():
                db.execute(sql)
            db.execute("PRAGMA user_version = 2")
        after = {table: db.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0] for table in before}
        assert before == after, (before, after)
        assert hashes_before["recipes"] == content_hash("recipes", "*")
        assert hashes_before["ingredientes"] == content_hash("ingredientes", "ingredient_id, ingredient_name")
        assert hashes_before["recipe_ingredients"] == content_hash("recipe_ingredients", "recipe_id, ingredient_id, quantity, unit, preparation_note")
        assert not db.execute("PRAGMA foreign_key_check").fetchall()
        assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
        tables = [row[0] for row in db.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'room_master_table'")]
        assert len(tables) >= 5
        schema_files = list((ROOT / "app/schemas").glob("*/2.json"))
        if schema_files:
            import json
            schema = json.loads(schema_files[0].read_text())["database"]
            for entity in schema["entities"]:
                name = entity["tableName"]
                actual = {row[1]: (row[2], bool(row[3]), row[4]) for row in db.execute(f"PRAGMA table_info({name})")}
                expected = {field["columnName"]: (field["affinity"], field["notNull"], field.get("defaultValue")) for field in entity["fields"]}
                assert actual == expected, (name, actual, expected)
                pk = [row[1] for row in sorted(db.execute(f"PRAGMA table_info({name})"), key=lambda row: row[5]) if row[5]]
                assert pk == entity["primaryKey"]["columnNames"], (name, pk)
                indexes = {row[1]: bool(row[2]) for row in db.execute(f"PRAGMA index_list({name})")}
                for index in entity["indices"]:
                    assert indexes[index["name"]] == index["unique"], (name, indexes)
                    columns = [row[2] for row in db.execute(f"PRAGMA index_info({index['name']})")]
                    assert columns == index["columnNames"], (name, columns)
                foreign_keys = {(row[2], row[3], row[4], row[5], row[6]) for row in db.execute(f"PRAGMA foreign_key_list({name})")}
                expected_fks = {(fk["table"], child, parent, fk["onUpdate"], fk["onDelete"])
                                for fk in entity["foreignKeys"]
                                for child, parent in zip(fk["columns"], fk["referencedColumns"])}
                assert foreign_keys == expected_fks, (name, foreign_keys, expected_fks)
            print("Esquema migrado compatível com o JSON gerado pelo compilador Room: OK")
        print(f"Migração: {len(tables)} tabelas; conteúdo original preservado: {after}")
        for table in tables:
            print(f"  {table}: {len(db.execute(f'PRAGMA table_info({table})').fetchall())} colunas")

        # Todas as consultas dos DAOs são preparadas pelo próprio SQLite.
        parameters = dict(id=-1, query="%", category="", favoritesOnly=0, customOnly=0,
                          useFridge=0, completeOnly=0, today="2026-10-12", name="teste")
        tested = 0
        for file in (CODE / "data/local/dao").glob("*.kt"):
            for sql in queries(file):
                db.execute("EXPLAIN " + sql, parameters).fetchall()
                tested += 1
        print(f"DAOs: {tested} consultas SQL válidas.")
        recipe_query = next(sql for sql in queries(CODE / "data/local/dao/RecipeDao.kt") if "requiredCount" in sql)
        begin = time.monotonic()
        results = db.execute(recipe_query, parameters).fetchall()
        assert results
        print(f"Busca no catálogo real: {len(results)} resultados em {time.monotonic() - begin:.2f}s")
        ingredient_id = db.execute("SELECT ingredient_id FROM recipe_ingredients WHERE recipe_id=1 LIMIT 1").fetchone()[0]
        with db:
            db.execute("INSERT INTO geladeira(food_code, quantity_g, added_at) VALUES (?,100,0)", (ingredient_id,))
        fridge_results = db.execute(recipe_query, parameters | dict(useFridge=1)).fetchall()
        assert any(row[0] == 1 for row in fridge_results) or len(fridge_results) == 100
        assert all(row[-1] > 0 for row in fridge_results)
        with db:
            for (ingredient_id,) in db.execute("SELECT DISTINCT ingredient_id FROM recipe_ingredients WHERE recipe_id=1").fetchall():
                db.execute("INSERT OR IGNORE INTO geladeira(food_code,quantity_g,added_at) VALUES (?,100,0)", (ingredient_id,))
        complete = db.execute(recipe_query, parameters | dict(useFridge=1, completeOnly=1, query="%Simple Macaroni and Cheese%")).fetchall()
        assert any(row[0] == 1 for row in complete)
        assert all(row[-1] == row[-2] for row in complete)
        with db:
            db.execute("UPDATE geladeira SET expiration_date='2000-01-01'")
        assert not db.execute(recipe_query, parameters | dict(useFridge=1)).fetchall()
        print("Busca pela geladeira, todos os ingredientes e exclusão de vencidos: OK")

        with db:
            # UC01: ingrediente próprio. UC03: receita e relação própria.
            db.execute("INSERT INTO ingredientes VALUES (-1,'Ingrediente de teste',1,0,'nota')")
            db.execute("INSERT INTO geladeira(food_code,quantity_g,added_at) VALUES (-1,100,0)")
            db.execute("INSERT INTO recipes(id,name,category,instructions,ingredients) VALUES (-1,'Receita de teste','Teste','Misture','1 unidade')")
            db.execute("INSERT INTO fogao(id,name,ingredients,instructions,created_at) VALUES (-1,'Receita de teste','1 unidade','Misture',0)")
            db.execute("INSERT INTO recipe_ingredients(recipe_id,ingredient_id,quantity,unit) VALUES (-1,-1,'1','unidade')")
            # UC05: favoritar e filtrar (UC06/UC07).
            db.execute("INSERT INTO favorites VALUES (-1,0)")
        favorites = db.execute(recipe_query, parameters | dict(favoritesOnly=1)).fetchall()
        assert len(favorites) == 1 and favorites[0][0] == -1
        custom = db.execute(recipe_query, parameters | dict(customOnly=1, category="Teste")).fetchall()
        assert len(custom) == 1 and custom[0][0] == -1
        details_query = next(sql for sql in queries(CODE / "data/local/dao/RecipeIngredientDao.kt") if "AS available" in sql)
        assert db.execute(details_query, parameters).fetchone()[1] == "Ingrediente de teste"
        # UC04/UC02: remover relações, receita, favorito por cascata, geladeira e ingrediente.
        with db:
            db.execute("DELETE FROM recipe_ingredients WHERE recipe_id=-1")
            db.execute("DELETE FROM fogao WHERE id=-1")
            db.execute("DELETE FROM recipes WHERE id=-1")
            assert not db.execute("SELECT * FROM favorites WHERE recipe_id=-1").fetchall()
            db.execute("DELETE FROM geladeira WHERE food_code=-1")
            db.execute("DELETE FROM ingredientes WHERE ingredient_id=-1")
        assert not db.execute("PRAGMA foreign_key_check").fetchall()
        print("CRUD personalizado, favoritos, filtros e detalhes: OK")
        db.close()
        original.close()


if __name__ == "__main__":
    run()
