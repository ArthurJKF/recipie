# Verificações realizadas

- `assembleDebug`: APK compilado com sucesso.
- `assembleDebugAndroidTest`: APK de testes compilado com sucesso.
- `lintDebug`: 0 erros. Três avisos informam que existem versões mais novas de dependências; as versões utilizadas estão fixadas.
- `apksigner verify`: assinatura do APK válida.
- `tools/verify_database.py`: todas as verificações passaram, incluindo comparação do esquema real com o JSON gerado pelo Room.
- Receitas originais preservadas: 35.516.
- Ingredientes originais preservados: 22.921.
- Relações originais preservadas: 333.552, incluindo repetições do mesmo ingrediente.
- Tabelas de aplicação: 6. Classes de entidade: 6. Atributos persistidos: 71.

Não foi executado um emulador ou dispositivo Android neste ambiente. O teste `RecipieDatabaseTest` está pronto para execução no Android Studio; ele ainda não foi executado em um dispositivo.

## Resultado da verificação do banco

```text
Esquema migrado compatível com o JSON gerado pelo compilador Room: OK
Migração: 6 tabelas; conteúdo original preservado: {'recipes': 35516, 'ingredientes': 22921, 'recipe_ingredients': 333552}
  favorites: 2 colunas
  fogao: 5 colunas
  recipes: 48 colunas
  ingredientes: 5 colunas
  recipe_ingredients: 6 colunas
  geladeira: 5 colunas
DAOs: 21 consultas SQL válidas.
Busca no catálogo real: 100 resultados em 0.09s
Busca pela geladeira, todos os ingredientes e exclusão de vencidos: OK
CRUD personalizado, favoritos, filtros e detalhes: OK
```
