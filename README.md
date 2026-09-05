# Central de Avisos

Aplicativo JavaFX para administrar avisos, popups, histórico, mensagem do dia e acessos da Intranet.

## Executar

Requer JDK 17 ou superior. Configure JAVA_HOME para a pasta do JDK e execute no terminal:

    .\mvnw.cmd javafx:run

## Conexão

Em Configurações, informe o endereço da Intranet, teste e salve. O teste não altera o endereço salvo. A configuração é preservada entre inicializações.

A prioridade é: propriedade Java intranet.base.url, variável INTRANET_BASE_URL, configuração salva, http://localhost:3000. O endpoint de avisos pode ser substituído por intranet.avisos.url ou INTRANET_AVISOS_URL. A autenticação usa INTRANET_AVISOS_TOKEN quando definido.

## Verificação

    .\mvnw.cmd test

A classe src/test/java/CentralVerification.java contém uma verificação executável adicional com main: execute-a pela IDE usando o classpath de teste. Ela verifica a precedência dos endpoints, a persistência do histórico, a proteção de arquivos ilegíveis, a navegação e a renderização em 1280 × 820 e 1000 × 680. As imagens são geradas em target. Não publica dados e usa 127.0.0.1:9 para as consultas de rede.

O teste de renderização precisa de um ambiente gráfico JavaFX. A publicação real depende do servidor da Intranet e não é exercitada por essa verificação.
