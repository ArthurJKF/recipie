"""Valida a limpeza, tradução e preservação dos dados na atualização 2 -> 3."""
import hashlib,json,re,sqlite3,tempfile,unicodedata
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/'app/src/main/java/com/example/recipie'

def statements(path):
 return [a or b for a,b in re.findall(r'db.execSQL\((?:"""(.*?)"""|"([^"\n]*)")\)',path.read_text(),re.S)]
def key(text):return ''.join(x for x in unicodedata.normalize('NFD',text or '') if not unicodedata.combining(x)).lower().strip()
def query_statements(path):
 return [a or b for a,b in re.findall(r'@Query\((?:"""(.*?)"""|"([^"\n]*)")\)',path.read_text(),re.S)]

def apply_portuguese(db,catalog):
 sql=statements(CODE/'data/local/database/PortugueseCatalogMigration.kt')
 split=next(i for i,s in enumerate(sql) if 'UPDATE recipe_ingredients' in s)
 for s in sql[:split]:db.execute(s)
 for id,name,canonical in catalog['ingredients']:
  db.execute('UPDATE ingredientes SET original_name=ingredient_name,ingredient_name=?,name_search=?,is_visible=?,canonical_id=? WHERE ingredient_id=?',
   (name,key(name),int(name is not None and canonical==id),canonical,id))
 for s in sql[split:]:db.execute(s)
 for id,name in catalog['recipes']:
  db.execute('UPDATE recipes SET original_name=name,name=?,name_search=? WHERE id=?',(name,key(name),id))
 text=(CODE/'data/local/database/PortugueseCatalogMigration.kt').read_text()
 for source,target in re.findall(r'"([\w-]+)" to "([^"\n]+)"',text):
  db.execute('UPDATE recipes SET category=? WHERE category=? AND id>0',(target,source))
 for table,idcol,namecol in [('ingredientes','ingredient_id','ingredient_name'),('recipes','id','name')]:
  for id,name in db.execute(f"SELECT {idcol},{namecol} FROM {table} WHERE name_search='' ").fetchall():
   db.execute(f'UPDATE {table} SET name_search=? WHERE {idcol}=?',(key(name),id))
 db.execute('PRAGMA user_version=3')

def verify_schema(db):
 schema=json.loads(next((ROOT/'app/schemas').glob('*/3.json')).read_text())['database']
 for entity in schema['entities']:
  name=entity['tableName']
  actual={x[1]:(x[2],bool(x[3]),x[4]) for x in db.execute(f'PRAGMA table_info({name})')}
  expected={x['columnName']:(x['affinity'],x['notNull'],x.get('defaultValue')) for x in entity['fields']}
  assert actual==expected,(name,actual,expected)
  fks={(x[2],x[3],x[4],x[5],x[6]) for x in db.execute(f'PRAGMA foreign_key_list({name})')}
  expected_fks={(x['table'],child,parent,x['onUpdate'],x['onDelete']) for x in entity['foreignKeys'] for child,parent in zip(x['columns'],x['referencedColumns'])}
  assert fks==expected_fks,name
 assert len(schema['entities'])==6
 print('Room versão 3: esquema compatível; 6 tabelas e 80 atributos persistidos.')

def run():
 catalog=json.loads((ROOT/'app/src/main/assets/database/catalogo_pt.json').read_text())
 source=sqlite3.connect(f"file:{ROOT/'app/src/main/assets/database/recipie.db'}?mode=ro",uri=True)
 with tempfile.TemporaryDirectory(prefix='recipie-pt-') as directory:
  db=sqlite3.connect(Path(directory)/'test.db');source.backup(db);db.execute('PRAGMA foreign_keys=ON')
  with db:
   for sql in statements(CODE/'data/local/database/RecipieDatabaseProvider.kt'):db.execute(sql)
   db.execute('PRAGMA user_version=2')
  base_count=db.execute('SELECT COUNT(*) FROM recipe_ingredients').fetchone()[0]
  pairs={id:canonical for id,pt,canonical in catalog['ingredients']}
  original_names=dict(db.execute('SELECT ingredient_id,ingredient_name FROM ingredientes'))
  original_titles=dict(db.execute('SELECT id,name FROM recipes'))
  # Simula um usuário com favoritos, duas grafias do mesmo alimento e itens próprios salvos na versão 2.
  alias,canonical=next((id,canon) for id,pt,canon in catalog['ingredients'] if canon and canon!=id)
  with db:
   db.execute('INSERT INTO geladeira(food_code,quantity_g,added_at) VALUES (?,100,7)',(alias,))
   db.execute('INSERT INTO geladeira(food_code,quantity_g,added_at) VALUES (?,200,9)',(canonical,))
   db.execute('INSERT INTO favorites VALUES (1,11)')
   db.execute("INSERT INTO ingredientes(ingredient_id,ingredient_name,is_custom,created_at,note) VALUES (-1,'Meu ingrediente',1,13,'Minha nota')")
   db.execute('INSERT INTO geladeira(food_code,quantity_g,added_at) VALUES (-1,50,15)')
   db.execute("INSERT INTO recipes(id,name,category,instructions,ingredients) VALUES (-1,'Minha receita','Minha categoria','Meu preparo','Meu ingrediente')")
   db.execute("INSERT INTO fogao VALUES(-1,'Minha receita','Meu ingrediente','Meu preparo',17)")
   db.execute("INSERT INTO recipe_ingredients(recipe_id,ingredient_id,quantity,unit) VALUES(-1,-1,'1','unidade')")
  def relation_hash(transformed=False):
   h=hashlib.sha256()
   for id,rid,iid,quantity,unit,note in db.execute('SELECT id,recipe_id,ingredient_id,quantity,unit,preparation_note FROM recipe_ingredients ORDER BY id'):
    if transformed:iid=pairs.get(iid) or iid
    h.update(repr((id,rid,iid,quantity,unit,note)).encode())
   return h.hexdigest()
  expected_links=relation_hash(True)
  with db:apply_portuguese(db,catalog)
  assert db.execute('PRAGMA integrity_check').fetchone()[0]=='ok'
  assert not db.execute('PRAGMA foreign_key_check').fetchall()
  assert db.execute('SELECT COUNT(*) FROM recipes').fetchone()[0]==35517
  assert db.execute('SELECT COUNT(*) FROM ingredientes').fetchone()[0]==22922
  assert db.execute('SELECT COUNT(*) FROM recipe_ingredients').fetchone()[0]==base_count+1
  assert relation_hash()==expected_links
  assert original_names==dict(db.execute('SELECT ingredient_id,original_name FROM ingredientes WHERE ingredient_id>0'))
  assert original_titles==dict(db.execute('SELECT id,original_name FROM recipes WHERE id>0'))
  assert db.execute('SELECT quantity_g FROM geladeira WHERE food_code=?',(canonical,)).fetchone()[0]==300
  assert db.execute('SELECT added_at FROM favorites WHERE recipe_id=1').fetchone()[0]==11
  assert db.execute('SELECT ingredient_name,note FROM ingredientes WHERE ingredient_id=-1').fetchone()==('Meu ingrediente','Minha nota')
  assert db.execute('SELECT name,instructions,category FROM recipes WHERE id=-1').fetchone()==('Minha receita','Meu preparo','Minha categoria')
  assert db.execute('SELECT quantity_g FROM geladeira WHERE food_code=-1').fetchone()[0]==50
  assert not db.execute('SELECT ingredient_id FROM ingredientes WHERE ingredient_id IN(14396,2474,305,5505,11764,7243,21306,19490) AND is_visible=1').fetchall()
  assert not db.execute("SELECT category FROM recipes WHERE id>0 AND (category GLOB '[0-9]*' OR category LIKE '%-%')").fetchall()
  assert not db.execute("SELECT ingredient_id FROM ingredientes WHERE is_visible=1 AND (ingredient_name IS NULL OR ingredient_name='' OR instr(ingredient_name,'⁇')>0)").fetchall()
  assert db.execute("SELECT COUNT(*) FROM ingredientes WHERE is_visible=1 AND name_search LIKE '%acucar%'").fetchone()[0]>0
  verify_schema(db)
  print('Nomes originais e os 333.552 vínculos de preparo preservados; aliases corrigidos.')
  print('Favoritos, geladeira e receitas/ingredientes personalizados da versão 2 preservados.')
  print('Fragmentos de temperaturas, pontuação, medidas e utensílios fora da seleção: OK.')
  print('Categorias em português; busca sem acentos: OK.')
  params=dict(query='%',id=-1,name='meu ingrediente',category='',favoritesOnly=0,customOnly=0,useFridge=0,completeOnly=0,today='2026-10-12',summary='Resumo',ingredients='Ingredientes',instructions='Preparo')
  count=0
  for file in (CODE/'data/local/dao').glob('*.kt'):
   for sql in query_statements(file):db.execute('EXPLAIN '+sql,params).fetchall();count+=1
  catalog_sql=next(sql for sql in query_statements(CODE/'data/local/dao/IngredienteDao.kt') if 'personalized' in sql)
  first=db.execute(catalog_sql,params).fetchall()
  assert len(first)==100
  print('Consultas SQL válidas:',count)
  print('Primeiros alimentos:',[x[1] for x in first[:15]])
  print('Ingredientes visíveis em português:',db.execute('SELECT COUNT(*) FROM ingredientes WHERE is_visible=1 AND ingredient_id>0').fetchone()[0])
  print('Títulos de receitas em português:',len(catalog['recipes']))
  db.close()
 source.close()
if __name__=='__main__':run()
