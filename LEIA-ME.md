# Recipie — Android, Kotlin e Room

Aplicativo com Geladeira, Início e Fogão, em azul e branco. A navegação funciona por gestos laterais e pelos três ícones na base. O projeto inclui o banco `recipie.db` enviado, com 35.516 receitas, 22.921 ingredientes e 333.552 relações entre receitas e ingredientes.

## Abrir no Android Studio

1. Extraia o ZIP em uma pasta do computador.
2. No Android Studio, escolha **Open** e selecione a pasta **Recipie**, que contém `settings.gradle.kts`. Não abra somente a pasta `app`.
3. Aguarde o **Gradle Sync**. O primeiro sincronismo precisa de internet para baixar dependências.
4. Se solicitado, instale o **Android SDK 35**. Em **Settings > Build, Execution, Deployment > Build Tools > Gradle**, use um JDK 17 (ou o JDK compatível disponibilizado pelo Android Studio).
5. Selecione um emulador ou celular com Android 8.0 ou superior e clique em **Run ▶**.

O banco já está em `app/src/main/assets/database/recipie.db`. Na primeira execução, o Room copia a base e executa a migração de versão 1 para 2. Isso pode levar alguns segundos. Depois, todo o aplicativo funciona sem internet.

Também é possível usar o terminal do Android Studio:

```powershell
.\gradlew.bat assembleDebug
```

O APK será criado em `app/build/outputs/apk/debug/app-debug.apk`. Para instalar diretamente no celular, use o arquivo `Recipie-debug.apk` entregue junto com o projeto.

Se você quiser aproveitar seu projeto antigo, o caminho mais simples é abrir este projeto completo. Para integrar manualmente, substitua os fontes do pacote `com.example.recipie`, copie o asset, configure as dependências de `app/build.gradle.kts` e atualize o manifest. Não misture classes antigas e novas com os mesmos nomes.

## Usar o aplicativo

- **Início:** tela de boas-vindas. Deslize ou toque nos ícones para acessar as outras telas.
- **Geladeira:** toque em Adicionar ingrediente, busque no catálogo, selecione o alimento e informe a quantidade em gramas. Validade é opcional e usa AAAA-MM-DD. O lápis altera quantidade/validade; o ícone de retirada remove apenas da geladeira.
- **Ingrediente personalizado:** dentro de Adicionar ingrediente, toque em Criar ingrediente personalizado. Para apagar definitivamente, busque seu nome no catálogo e toque na lixeira. Se ele estiver em uma receita, remova a receita personalizada antes de excluí-lo. O catálogo original não pode ser apagado pela interface.
- **Fogão:** o filtro Minha geladeira mostra receitas com pelo menos um ingrediente disponível. As receitas são ordenadas pela quantidade de ingredientes disponíveis. Desative-o para consultar a base inteira, inclusive quando a geladeira estiver vazia.
- **Tenho todos:** mostra receitas em que todos os ingredientes cadastrados estão na geladeira e dentro da validade. Considera a presença dos ingredientes, não converte unidades nem calcula se os gramas bastam para o preparo.
- **Nova receita:** preencha nome, ingredientes com quantidade/unidade e modo de preparo. Categoria, descrição, tempos e porções também podem ser informados. Para usar um ingrediente novo, crie-o primeiro na Geladeira. Você pode selecionar ingredientes que não estão na sua geladeira.
- **Favoritos:** toque no coração de uma receita para adicionar/remover dos favoritos. Ative Favoritos no Fogão para filtrar. Esse filtro combina com os demais; desative Minha geladeira para ver todos os favoritos.
- **Detalhes:** toque no cartão da receita para ver lista original de ingredientes, disponibilidade na geladeira, preparo e dados nutricionais quando existirem. Uma receita personalizada mostra o botão de exclusão nessa tela.

A interface está em português. Os títulos, categorias, ingredientes e instruções do catálogo original continuam em inglês, como no banco enviado. Exemplos de busca: `rice` (arroz), `milk` (leite), `egg` (ovo). As buscas mostram até 100 resultados para não carregar toda a base na interface; refine o texto ou os filtros para encontrar outros itens. Não há limite de 100 registros no banco.

## Casos de uso implementados

| Caso | Onde executar |
|---|---|
| UC01 — Inserir ingredientes personalizados | Geladeira → Adicionar ingrediente → Criar ingrediente personalizado |
| UC02 — Remover ingredientes personalizados | Geladeira → Adicionar ingrediente → buscar no catálogo → lixeira |
| UC03 — Inserir receitas personalizadas | Fogão → Criar receita personalizada |
| UC04 — Remover receitas personalizadas | Fogão → abrir receita própria → Excluir receita personalizada |
| UC05 — Adicionar receitas aos favoritos | Coração no cartão ou botão nos detalhes |
| UC06 — Buscar receitas | Campo de busca no Fogão: nome, descrição ou ingrediente |
| UC07 — Filtrar resultados | Minha geladeira, Tenho todos, Favoritos, Minhas receitas e categoria |
| UC08 — Visualizar receita detalhada | Toque no cartão de receita |

## Requisitos: classes, atributos e tabelas

Há **6 tabelas de aplicação**, **6 classes de entidade** e **71 atributos persistidos** nessas classes. Outras classes, como Repository, ViewModel e os modelos de formulário, não entram nessa contagem. `room_master_table` e `sqlite_sequence` são tabelas internas e não são usadas para completar o requisito.

| Classe | Tabela | Atributos |
|---|---|---:|
| IngredienteEntity | ingredientes | 5 |
| RecipeEntity | recipes | 48 |
| RecipeIngredientEntity | recipe_ingredients | 6 |
| GeladeiraEntity | geladeira | 5 |
| FogaoEntity | fogao | 5 |
| FavoriteEntity | favorites | 2 |
| **Total** | **6 tabelas** | **71** |

- `ingredientes`: catálogo de alimentos, incluindo a identificação dos personalizados, data de criação e observação.
- `recipes`: catálogo de receitas, incluindo receitas próprias e os campos nutricionais existentes.
- `recipe_ingredients`: associações entre receitas e ingredientes, com quantidade, unidade e observação de preparo.
- `geladeira`: ingredientes armazenados pelo usuário, quantidade em gramas e validade.
- `fogao`: identificação das receitas criadas pelo usuário, vinculadas pelo mesmo ID usado em `recipes`.
- `favorites`: receita favoritada e data em que foi adicionada.

O requisito é de pelo menos cinco **tabelas**, não de cinco colunas em todas elas.

## O que cada parte do código faz

Comparando com POO em C++: uma `data class` guarda os dados de um objeto; as anotações `@Entity` e `@ColumnInfo` dizem ao Room como armazená-los em tabelas e colunas. Um DAO é uma interface que declara as operações SQL. O Room gera a implementação durante a compilação. O Repository coordena essas operações e as transações. O ViewModel mantém os dados e os comandos da tela. As funções `@Composable` desenham a interface com base nesse estado.

Fluxo de uma inclusão: **tela → ViewModel → Repository → DAO → SQLite**. Quando o banco muda, os `Flow`s atualizam as listas na interface. O mesmo ViewModel é compartilhado pelas três telas.

Os IDs negativos das receitas e ingredientes personalizados são alocados dentro de transações para evitar colisões com os IDs positivos da base. A exclusão de receitas é um `DELETE` real. Somente registros personalizados podem ser excluídos do catálogo pelo aplicativo.

## Compatibilidade do banco

O asset original é preservado. A migração `MIGRATION_1_2`, em `RecipieDatabaseProvider.kt`, corrige o esquema na cópia instalada:

- corrige `geladeira.food_code`, que era texto e apontava para `ingredientes.codigo`, uma coluna inexistente;
- normaliza a chave `ingredient_id` e acrescenta metadados dos ingredientes personalizados;
- adiciona um ID próprio a cada relação receita/ingrediente, preservando os casos em que o mesmo alimento aparece em várias etapas;
- cria índices para agilizar as consultas;
- preserva as tabelas de receitas, favoritos e receitas próprias.

Não é utilizado `fallbackToDestructiveMigration`. Quantidades, textos, nutrientes e todos os registros do catálogo são preservados. O esquema Room da versão 2 é exportado durante a compilação para `app/schemas`.

## Verificações

O script abaixo executa a migração e todas as consultas SQL dos DAOs sobre uma cópia temporária da base real, verifica integridade, compara o conteúdo antes/depois e testa favoritos, busca, filtros, criação e exclusão:

```bash
python tools/verify_database.py
```

O teste Android `RecipieDatabaseTest` abre o asset pelo Room, valida o esquema, exerce as operações pelo Repository e verifica persistência após fechar/reabrir o banco. Execute-o com um emulador/celular conectado:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```
